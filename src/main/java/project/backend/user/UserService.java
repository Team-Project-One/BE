package project.backend.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import project.backend.user.dto.SignUpRequestDTO;
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

	@Transactional
	public UserResponseDTO registerNewUser(SignUpRequestDTO requestDTO) {
		UserProfile userProfile = UserProfile.builder()
				.job(requestDTO.getJob())
				.region(requestDTO.getRegion())
				.drinkingFrequency(requestDTO.getDrinkingFrequency())
				.smokingStatus(requestDTO.getSmokingStatus())
				.height(requestDTO.getHeight())
				.petPreference(requestDTO.getPetPreference())
				.religion(requestDTO.getReligion())
				.contactFrequency(requestDTO.getContactFrequency())
				.mbti(requestDTO.getMbti())
				.build();

		User user = User.builder()
				.name(requestDTO.getName())
				.gender(requestDTO.getGender())
				.birthDate(requestDTO.getBirthdate())
				.userProfile(userProfile)
				.build();

		repository.save(user);

		return UserResponseDTO.builder().id(user.getId()).name(user.getName()).build();
	}

	/*// 기본 정보 저장
	public BasicInfoDTO saveBasicInfo(BasicInfoDTO basicInfoDTO) {
		User user = User.builder()
				.name(basicInfoDTO.getName())
				.gender(basicInfoDTO.getGender())
				.birthDate(basicInfoDTO.getBirthDate())
				.build();
		
		User savedUser = repository.save(user);
		
		return BasicInfoDTO.builder()
				.id(savedUser.getId())
				.name(savedUser.getName())
				.gender(savedUser.getGender())
				.birthDate(savedUser.getBirthDate())
				.build();
	}
	
	// 상세 정보 저장
	public DetailInfoDTO saveDetailInfo(DetailInfoDTO detailInfoDTO) throws IOException {
		// 기본 정보 조회
		User user = repository.findById(detailInfoDTO.getBasicInfoId())
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + detailInfoDTO.getBasicInfoId()));
		
		// 프로필 이미지 처리
		String profileImagePath = null;
		if (detailInfoDTO.getProfileImage() != null && !detailInfoDTO.getProfileImage().isEmpty()) {
			profileImagePath = saveProfileImage(detailInfoDTO.getProfileImage());
		}
		
		UserProfile userProfile = UserProfile.builder()
				.user(user)
				.profileImagePath(profileImagePath)
				.place(detailInfoDTO.getPlace())
				.drinkingFrequency(detailInfoDTO.getDrinkingFrequency())
				.smokingStatus(detailInfoDTO.getSmokingStatus())
				.height(detailInfoDTO.getHeight())
				.pet(detailInfoDTO.getPet())
				.religion(detailInfoDTO.getReligion())
				.childPlan(detailInfoDTO.getChildPlan())
				.mbti(detailInfoDTO.getMbti())
				.build();
		
		UserProfile savedUserProfile = detailInfoRepository.save(userProfile);
		
		return DetailInfoDTO.builder()
				.id(savedUserProfile.getId())
				.basicInfoId(savedUserProfile.getUser().getId())
				.profileImagePath(savedUserProfile.getProfileImagePath())
				.place(savedUserProfile.getPlace())
				.drinkingFrequency(savedUserProfile.getDrinkingFrequency())
				.smokingStatus(savedUserProfile.getSmokingStatus())
				.height(savedUserProfile.getHeight())
				.pet(savedUserProfile.getPet())
				.religion(savedUserProfile.getReligion())
				.childPlan(savedUserProfile.getChildPlan())
				.mbti(savedUserProfile.getMbti())
				.build();
	}
	
	// 전체 사용자 정보 조회
	@Transactional(readOnly = true)
	public UserInfoResponseDTO getUserInfo(Long basicInfoId) {
		User user = repository.findById(basicInfoId)
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + basicInfoId));
		
		UserProfile userProfile = detailInfoRepository.findByBasicInfoId(basicInfoId)
				.orElse(null);
		
		UserInfoResponseDTO.UserInfoResponseDTOBuilder builder = UserInfoResponseDTO.builder()
				.basicInfoId(user.getId())
				.name(user.getName())
				.gender(user.getGender())
				.birthDate(user.getBirthDate());
		
		if (userProfile != null) {
			builder.detailInfoId(user.getId())
				.profileImagePath(userProfile.getProfileImagePath())
				.place(userProfile.getPlace())
				.drinkingFrequency(userProfile.getDrinkingFrequency())
				.smokingStatus(userProfile.getSmokingStatus())
				.height(userProfile.getHeight())
				.pet(userProfile.getPet())
				.religion(userProfile.getReligion())
				.childPlan(userProfile.getChildPlan())
				.mbti(userProfile.getMbti());
		}
		
		return builder.build();
	}
	
	// 기본 정보 수정
	public BasicInfoDTO updateBasicInfo(Long id, BasicInfoDTO basicInfoDTO) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + id));
		
		user.setName(basicInfoDTO.getName());
		user.setGender(basicInfoDTO.getGender());
		user.setBirthDate(basicInfoDTO.getBirthDate());
		
		User updatedUser = repository.save(user);
		
		return BasicInfoDTO.builder()
				.id(updatedUser.getId())
				.name(updatedUser.getName())
				.gender(updatedUser.getGender())
				.birthDate(updatedUser.getBirthDate())
				.build();
	}
	
	// 상세 정보 수정
	public DetailInfoDTO updateDetailInfo(Long id, DetailInfoDTO detailInfoDTO) throws IOException {
		UserProfile userProfile = detailInfoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("상세 정보를 찾을 수 없습니다. ID: " + id));
		
		// 새로운 프로필 이미지가 있으면 처리
		if (detailInfoDTO.getProfileImage() != null && !detailInfoDTO.getProfileImage().isEmpty()) {
			// 기존 이미지 삭제(선택사항)
			if (userProfile.getProfileImagePath() != null) {
				deleteProfileImage(userProfile.getProfileImagePath());
			}
			String newProfileImagePath = saveProfileImage(detailInfoDTO.getProfileImage());
			userProfile.setProfileImagePath(newProfileImagePath);
		}
		
		userProfile.setPlace(detailInfoDTO.getPlace());
		userProfile.setDrinkingFrequency(detailInfoDTO.getDrinkingFrequency());
		userProfile.setSmokingStatus(detailInfoDTO.getSmokingStatus());
		userProfile.setHeight(detailInfoDTO.getHeight());
		userProfile.setPet(detailInfoDTO.getPet());
		userProfile.setReligion(detailInfoDTO.getReligion());
		userProfile.setChildPlan(detailInfoDTO.getChildPlan());
		userProfile.setMbti(detailInfoDTO.getMbti());
		
		UserProfile updatedUserProfile = detailInfoRepository.save(userProfile);
		
		return DetailInfoDTO.builder()
				.id(updatedUserProfile.getId())
				.place(updatedUserProfile.getPlace())
				.drinkingFrequency(updatedUserProfile.getDrinkingFrequency())
				.smokingStatus(updatedUserProfile.getSmokingStatus())
				.height(updatedUserProfile.getHeight())
				.pet(updatedUserProfile.getPet())
				.religion(updatedUserProfile.getReligion())
				.childPlan(updatedUserProfile.getChildPlan())
				.mbti(updatedUserProfile.getMbti())
				.build();
	}
	
	// 현재 사용자의 거주 지역을 기준으로 같은 지역 사용자 찾기
	@Transactional(readOnly = true)
	public List<UserInfoResponseDTO> getUsersBySamePlace(Long myBasicInfoId) {
		// 내 상세정보 가져오기
		UserProfile myDetail = detailInfoRepository.findByBasicInfoId(myBasicInfoId)
				.orElseThrow(() -> new IllegalArgumentException("상세정보를 찾을 수 없습니다."));
			 
		String myPlace = myDetail.getPlace();
			 
		// 같은 지역 사용자 전체 조회
		List<UserProfile> usersInSamePlace = detailInfoRepository.findByPlace(myPlace);
			 
		return usersInSamePlace.stream()
			.filter(detail -> !detail.getUser().getId().equals(myBasicInfoId)) // 자신 제외
			.map(detail -> {
				User basic = detail.getUser();
						 
				return UserInfoResponseDTO.builder()
						.basicInfoId(basic.getId())
						.name(basic.getName())
						.gender(basic.getGender())
						.birthDate(basic.getBirthDate())
						.detailInfoId(detail.getId())
						.profileImagePath(detail.getProfileImagePath())
						.place(detail.getPlace())
						.drinkingFrequency(detail.getDrinkingFrequency())
						.smokingStatus(detail.getSmokingStatus())
						.height(detail.getHeight())
						.pet(detail.getPet())
						.religion(detail.getReligion())
						.childPlan(detail.getChildPlan())
						.mbti(detail.getMbti())
						.build();
			})
			.toList();
	}
	
	// 회원 탈퇴 (MyPageService에서 위임 호출)
	public void deleteUser(Long userId) {
		User user = repository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
		
		// 프로필 이미지 삭제
		UserProfile userProfile = detailInfoRepository.findByBasicInfoId(userId).orElse(null);
		if (userProfile != null && userProfile.getProfileImagePath() != null) {
			deleteProfileImage(userProfile.getProfileImagePath());
		}
		
		// DB에서 삭제 (Cascade 설정으로 DetailInfo도 함께 삭제됨)
		repository.delete(user);
		
		log.info("회원 탈퇴 완료 - 사용자 ID: {}", userId);
	}
	
	// 프로필 이미지 저장
	private String saveProfileImage(MultipartFile file) throws IOException {
		// 업로드 디렉터리 생성
		Path uploadPath = Paths.get(uploadDir);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		
		// 고유한 파일명 생성
		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String fileName = UUID.randomUUID().toString() + extension;
		
		// 파일 저장
		Path filePath = uploadPath.resolve(fileName);
		Files.copy(file.getInputStream(), filePath);
		
		return "/uploads/profile/" + fileName;
	}
	
	// 프로필 이미지 삭제
	private void deleteProfileImage(String imagePath) {
		try {
			String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
			Path filePath = Paths.get(uploadDir).resolve(fileName);
			Files.deleteIfExists(filePath);
		} catch (Exception e) {
			log.error("프로필 이미지 삭제 실패: " + imagePath, e); 
		}
	}
	
	// MyPageService 위임 호출용
	// 기존 프로필 이미지를 삭제하고 새로운 프로필 이미지 저장 후 경로 반환
    public String updateProfileImage(Long userId, MultipartFile newImage) throws IOException {
        UserProfile userProfile = detailInfoRepository.findByBasicInfoId(userId)
                .orElseThrow(() -> new EntityNotFoundException("상세 정보를 찾을 수 없습니다. ID: " + userId));

        // 기존 이미지 삭제
        if (userProfile.getProfileImagePath() != null) {
            deleteProfileImage(userProfile.getProfileImagePath());
        }

        // 새 이미지 저장
        String newPath = saveProfileImage(newImage);
        userProfile.setProfileImagePath(newPath);
        detailInfoRepository.save(userProfile);

        return newPath;
    }*/
}