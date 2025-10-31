package project.backend.mypage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.backend.dto.UserInfoResponseDTO;
import project.backend.entity.BasicInfo;
import project.backend.entity.DetailInfo;
import project.backend.repository.BasicInfoRepository;
import project.backend.repository.DetailInfoRepository;
import project.backend.service.UserInfoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {
	private final UserInfoService userInfoService;
	private final BasicInfoRepository basicInfoRepository;
	private final DetailInfoRepository detailInfoRepository;
	
	// 마이페이지 조회
	public MyPageDisplayDTO getMyPageInfo(Long userId) {
		UserInfoResponseDTO userInfo = userInfoService.getUserInfo(userId);
		
		Integer age = calculateAge(userInfo.getBirthDate());
		
		return MyPageDisplayDTO.builder()
				.userId(userInfo.getBasicInfoId())
				.profileImagePath(userInfo.getProfileImagePath())
				.name(userInfo.getName())
				.age(age)
				.gender(userInfo.getGender())
				.place(userInfo.getPlace())
				.birthDate(userInfo.getBirthDate())
				.mbti(userInfo.getMbti())
				.build();
	}
	
	 // 프로필 수정 화면용 전체 정보 조회
    @Transactional(readOnly = true)
    public ProfileEditDTO getProfileForEdit(Long userId) {
        BasicInfo basicInfo = basicInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
        
        DetailInfo detailInfo = detailInfoRepository.findByBasicInfoId(userId)
                .orElse(null);
        
        ProfileEditDTO.ProfileEditDTOBuilder builder = ProfileEditDTO.builder()
                .basicInfoId(basicInfo.getId())
                .name(basicInfo.getName())
                .gender(basicInfo.getGender())
                .birthDate(basicInfo.getBirthDate());
        
        if (detailInfo != null) {
            builder.detailInfoId(detailInfo.getId())
                    .profileImagePath(detailInfo.getProfileImagePath())
                    .place(detailInfo.getPlace())
                    .drinkingFrequency(detailInfo.getDrinkingFrequency())
                    .smokingStatus(detailInfo.getSmokingStatus())
                    .height(detailInfo.getHeight())
                    .pet(detailInfo.getPet())
                    .religion(detailInfo.getReligion())
                    .childPlan(detailInfo.getChildPlan())
                    .mbti(detailInfo.getMbti());
        }
        
        return builder.build();
    }
	
    // 프로필 전체 수정 (기본 정보 + 상세 정보)
    @Transactional
    public ProfileEditDTO updateProfile(Long userId, ProfileUpdateDTO updateDTO) throws IOException {
        BasicInfo basicInfo = basicInfoRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 기본 정보 업데이트 (null 체크)
        if (updateDTO.getName() != null) basicInfo.setName(updateDTO.getName());
        if (updateDTO.getGender() != null) basicInfo.setGender(updateDTO.getGender());
        if (updateDTO.getBirthDate() != null) basicInfo.setBirthDate(updateDTO.getBirthDate());

        BasicInfo savedBasicInfo = basicInfoRepository.save(basicInfo);

        // 상세 정보
        DetailInfo detailInfo = detailInfoRepository.findByBasicInfoId(userId)
                .orElse(DetailInfo.builder()
                        .basicInfo(savedBasicInfo)
                        .build());

        // 프로필 이미지 처리
        if (updateDTO.getProfileImage() != null && !updateDTO.getProfileImage().isEmpty()) {
            String newProfileImagePath = userInfoService.updateProfileImage(userId, updateDTO.getProfileImage());
            detailInfo.setProfileImagePath(newProfileImagePath);
        }

        // null 체크 후 업데이트
        if (updateDTO.getPlace() != null) detailInfo.setPlace(updateDTO.getPlace());
        if (updateDTO.getDrinkingFrequency() != null) detailInfo.setDrinkingFrequency(updateDTO.getDrinkingFrequency());
        if (updateDTO.getSmokingStatus() != null) detailInfo.setSmokingStatus(updateDTO.getSmokingStatus());
        if (updateDTO.getHeight() != null) detailInfo.setHeight(updateDTO.getHeight());
        if (updateDTO.getPet() != null) detailInfo.setPet(updateDTO.getPet());
        if (updateDTO.getReligion() != null) detailInfo.setReligion(updateDTO.getReligion());
        if (updateDTO.getChildPlan() != null) detailInfo.setChildPlan(updateDTO.getChildPlan());
        if (updateDTO.getMbti() != null) detailInfo.setMbti(updateDTO.getMbti());

        DetailInfo savedDetailInfo = detailInfoRepository.save(detailInfo);
        
        // 응답 DTO 생성 (수정 완료 후 전체 사용자 정보 반환)
        return ProfileEditDTO.builder()
                .basicInfoId(savedBasicInfo.getId())
                .name(savedBasicInfo.getName())
                .gender(savedBasicInfo.getGender())
                .birthDate(savedBasicInfo.getBirthDate())
                .detailInfoId(savedDetailInfo.getId())
                .profileImagePath(savedDetailInfo.getProfileImagePath())
                .place(savedDetailInfo.getPlace())
                .drinkingFrequency(savedDetailInfo.getDrinkingFrequency())
                .smokingStatus(savedDetailInfo.getSmokingStatus())
                .height(savedDetailInfo.getHeight())
                .pet(savedDetailInfo.getPet())
                .religion(savedDetailInfo.getReligion())
                .childPlan(savedDetailInfo.getChildPlan())
                .mbti(savedDetailInfo.getMbti())
                .build();
    }
		
	// 회원 탈퇴
	public void deleteUser(Long userId) {
		userInfoService.deleteUser(userId);
		log.info("회원 탈퇴 완료 - 사용자 ID: {}", userId);
	}
	
	// 나이 계산 (만 나이)
	private Integer calculateAge(LocalDate birthDate) {
		return (birthDate == null) ? null : Period.between(birthDate, LocalDate.now()).getYears();
	}
}