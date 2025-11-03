package project.backend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.dto.SignUpRequestDTO;
import project.backend.user.dto.UserEnums;
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
	public UserResponseDTO registerNewUser(SignUpRequestDTO requestDTO) {
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
				.build();

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

	@Transactional
	public void deleteUser(Long userId) {
		User user = repository.findByIdWithProfile(userId)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

		repository.delete(user);
	}
	
	/*
	
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