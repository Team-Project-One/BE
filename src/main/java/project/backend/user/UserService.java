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
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.dto.SignUpRequestDTO;
import project.backend.user.dto.UserProfileDTO;
import project.backend.user.dto.UserResponseDTO;
import project.backend.user.entity.User;
import project.backend.user.entity.UserProfile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository repository;

	@Value("${file.upload-dir:uploads/profile}")
	private String uploadDir;

	//회원가입
	@Transactional
	public UserResponseDTO registerNewUser(SignUpRequestDTO requestDTO, MultipartFile profileImage) throws IOException {
		UserProfile userProfile = UserProfile.builder()
				.sexualOrientation(requestDTO.getSexualorientation())
				.job(requestDTO.getJob())
				.region(requestDTO.getRegion())
				.drinkingFrequency(requestDTO.getDrinkingFrequency())
				.smokingStatus(requestDTO.getSmokingStatus())
				.height(requestDTO.getHeight())
				.petPreference(requestDTO.getPetPreference())
				.religion(requestDTO.getReligion())
				.contactFrequency(requestDTO.getContactFrequency())
				.mbti(requestDTO.getMbti())
				.introduction(requestDTO.getIntroduction())
				.build();

		// 프로필 이미지 저장
		if (profileImage != null && !profileImage.isEmpty()) {
			String imagePath = saveProfileImage(profileImage);
			userProfile.setProfileImagePath(imagePath);
		}

		User user = User.builder()
				.name(requestDTO.getName())
				.gender(requestDTO.getGender())
				.birthDate(requestDTO.getBirthdate())
				.userProfile(userProfile)
				.build();

		repository.save(user);

		return new UserResponseDTO(user.getId(), user.getName()); //과연 필요할까..??일단 넣음
	}


	// 전체 사용자 정보 조회
	public MyPageDisplayDTO getUserInfo(Long userId) {
		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		return MyPageDisplayDTO.fromEntity(user);
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

		UserProfile userProfile = user.getUserProfile();
		if (userProfile != null && userProfile.getProfileImagePath() != null) {
			deleteProfileImage(userProfile.getProfileImagePath());
		}

		repository.delete(user);
	}

	//프로필 이미지 수정
	@Transactional
	public String updateUserProfileImage(Long userId, MultipartFile newImage) throws IOException {
		if (newImage == null || newImage.isEmpty()) {
			throw new IllegalArgumentException("Profile image file must not be empty");
		}

		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		UserProfile userProfile = user.getUserProfile();
		if (userProfile == null) {
			throw new EntityNotFoundException("User profile not found for user id " + userId);
		}

		if (userProfile.getProfileImagePath() != null) {
			deleteProfileImage(userProfile.getProfileImagePath());
		}

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