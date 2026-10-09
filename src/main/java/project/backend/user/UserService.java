package project.backend.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import project.backend.kakaoLogin.KakaoUser;
import project.backend.kakaoLogin.KakaoUserRepository;
import project.backend.kakaoLogin.RefreshTokenRepository;
import project.backend.chat.repository.ChatRoomRepository;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.dto.SignUpRequestDTO;
import project.backend.user.dto.UserEnums;
import project.backend.user.dto.UserProfileDTO;
import project.backend.user.dto.UserResponseDTO;
import project.backend.user.dto.UserStatusDTO;
import project.backend.user.entity.User;
import project.backend.user.entity.UserProfile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository repository;
	private final KakaoUserRepository kakaoUserRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${file.upload-dir:uploads/profile}")
	private String uploadDir;

	//회원가입
	@Transactional
	public UserResponseDTO registerNewUser(SignUpRequestDTO requestDTO, MultipartFile profileImage) throws IOException {
		if (requestDTO.getKakaoId() == null || requestDTO.getKakaoId().isBlank()) {
			throw new IllegalArgumentException("kakaoId is required for signup");
		}

		KakaoUser kakaoUser = kakaoUserRepository.findByKakaoId(requestDTO.getKakaoId())
				.orElseThrow(() -> new EntityNotFoundException("Kakao user not found for id " + requestDTO.getKakaoId()));

		if (requestDTO.getEmail() != null && !requestDTO.getEmail().isBlank()) {
			kakaoUser.setEmail(requestDTO.getEmail());
		}

		User user = kakaoUser.getUser();

		if (user == null) {
			UserProfile profile = UserProfile.builder().build();
			user = User.builder()
					.name(null)
					.gender(null)
					.birthDate(null)
					.userProfile(profile)
					.build();
			profile.setUser(user);
			user.setKakaoUser(kakaoUser);
		}

		user.updateBasicInfo(requestDTO.getName(), requestDTO.getGender(), requestDTO.getBirthdate());

		UserProfile profile = user.getUserProfile();
		if (profile == null) {
			profile = UserProfile.builder().build();
			user.setUserProfile(profile);
		}

		profile.setSexualOrientation(requestDTO.getSexualOrientation());
		profile.setJob(requestDTO.getJob());
		profile.setRegion(requestDTO.getRegion());
		profile.setDrinkingFrequency(requestDTO.getDrinkingFrequency());
		profile.setSmokingStatus(requestDTO.getSmokingStatus());
		profile.setHeight(requestDTO.getHeight());
		profile.setPetPreference(requestDTO.getPetPreference());
		profile.setReligion(requestDTO.getReligion());
		profile.setContactFrequency(requestDTO.getContactFrequency());
		// MBTI에서 UNKNOWN은 "설정 안 함" 의미로 사용하며, DB에는 null로 저장
		if (requestDTO.getMbti() == UserEnums.Mbti.UNKNOWN) {
			profile.setMbti(null);
		} else {
			profile.setMbti(requestDTO.getMbti());
		}
		profile.setIntroduction(requestDTO.getIntroduction());

		if (profileImage != null && !profileImage.isEmpty()) {
			String imagePath = saveProfileImage(profileImage);
			profile.setProfileImagePath(imagePath);
		}

		repository.save(user);

		return new UserResponseDTO(user.getId(), user.getName());
	}


	// 전체 사용자 정보 조회
	public MyPageDisplayDTO getUserInfo(Long userId) {
		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		return MyPageDisplayDTO.fromEntity(user);
	}

	public UserStatusDTO getUserStatusByKakaoId(String kakaoId) {
		return repository.findByKakaoIdWithProfile(kakaoId)
				.map(user -> new UserStatusDTO(
						user.getId(),
						user.getUserProfile() != null && user.getName() != null && user.getBirthDate() != null))
				.orElse(new UserStatusDTO(null, false));
	}

	//상세 정보 수정
	@Transactional
	public void updateUserProfileInfo(Long userId, UserProfileDTO userProfileDTO) {
		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		UserProfile userProfile = user.getUserProfile();
		userProfile.updateUserProfile(userProfileDTO);

	}

	//회원 탈퇴
	@Transactional
	public void deleteUser(Long userId) {
		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		// 프로필 이미지 삭제
		UserProfile userProfile = user.getUserProfile();
		if (userProfile != null && userProfile.getProfileImagePath() != null) {
			deleteProfileImage(userProfile.getProfileImagePath());
		}

		// 관련된 채팅방 삭제 (채팅방에 참여한 모든 방 삭제)
		java.util.List<project.backend.chat.entity.ChatRoom> chatRooms = chatRoomRepository.findAllByUser(user);
		chatRoomRepository.deleteAll(chatRooms);

		// RefreshToken 삭제
		KakaoUser kakaoUser = user.getKakaoUser();
		if (kakaoUser != null) {
			// kakaoId와 email로 RefreshToken 삭제 시도
			if (kakaoUser.getKakaoId() != null) {
				refreshTokenRepository.deleteByUserId(kakaoUser.getKakaoId());
			}
			if (kakaoUser.getEmail() != null && !kakaoUser.getEmail().equals(kakaoUser.getKakaoId())) {
				refreshTokenRepository.deleteByUserId(kakaoUser.getEmail());
			}
		}

		// User와 KakaoUser의 양방향 관계 해제
		if (kakaoUser != null) {
			kakaoUser.setUser(null);
		}
		user.setKakaoUser(null);

		// User 삭제 (UserProfile은 cascade로 자동 삭제됨)
		repository.delete(user);

		// KakaoUser 삭제 (User와의 관계가 끊어진 후)
		if (kakaoUser != null) {
			kakaoUserRepository.delete(kakaoUser);
		}
	}

	//프로필 이미지 수정
	@Transactional
	public String updateUserProfileImage(Long userId, MultipartFile newImage) throws IOException {
		// newImage가 null이거나 비어 있으면, 기존 이미지를 삭제하고 프로필 이미지를 제거한다.

		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		UserProfile userProfile = user.getUserProfile();
		if (userProfile == null) {
			throw new EntityNotFoundException("User profile not found for user id " + userId);
		}

		// 기존 이미지가 있으면 항상 삭제
		if (userProfile.getProfileImagePath() != null) {
			deleteProfileImage(userProfile.getProfileImagePath());
			userProfile.setProfileImagePath(null);
		}

		// 새 이미지가 없는 경우: 단순히 삭제만 하고 종료 (기본 이미지 상태)
		if (newImage == null || newImage.isEmpty()) {
			return null;
		}

		// 새 이미지가 있는 경우: 저장 후 경로 업데이트
		String newPath = saveProfileImage(newImage);
		userProfile.setProfileImagePath(newPath);

		return newPath;
	}

	// 프로필 이미지 저장
	private String saveProfileImage(MultipartFile file) throws IOException {
		Path uploadPath = Paths.get(uploadDir);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
			extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
		}
		String fileName = UUID.randomUUID().toString() + extension;

		Path filePath = uploadPath.resolve(fileName);
		Files.copy(file.getInputStream(), filePath);

		return "/uploads/profile/" + fileName;
	}

	// 프로필 이미지 삭제
	private void deleteProfileImage(String imagePath) {
		try {
			String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
			Path filePath = Paths.get(uploadDir).resolve(fileName);
			Files.deleteIfExists(filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
