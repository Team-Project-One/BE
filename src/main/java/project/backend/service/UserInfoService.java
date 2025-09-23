package project.backend.service;

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
import lombok.extern.slf4j.Slf4j;
import project.backend.dto.BasicInfoDTO;
import project.backend.dto.DetailInfoDTO;
import project.backend.dto.UserInfoResponseDTO;
import project.backend.entity.BasicInfo;
import project.backend.entity.DetailInfo;
import project.backend.repository.BasicInfoRepository;
import project.backend.repository.DetailInfoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserInfoService {
	
	private final BasicInfoRepository basicInfoRepository;
	private final DetailInfoRepository detailInfoRepository;
	
	@Value("${file.upload-dir:uploads/profile}")
	private String uploadDir;
	
	// 기본 정보 저장
	public BasicInfoDTO saveBasicInfo(BasicInfoDTO basicInfoDTO) {
		BasicInfo basicInfo = BasicInfo.builder()
				.name(basicInfoDTO.getName())
				.gender(basicInfoDTO.getGender())
				.birthDate(basicInfoDTO.getBirthDate())
				.build();
		
		BasicInfo savedBasicInfo = basicInfoRepository.save(basicInfo);
		
		return BasicInfoDTO.builder()
				.id(savedBasicInfo.getId())
				.name(savedBasicInfo.getName())
				.gender(savedBasicInfo.getGender())
				.birthDate(savedBasicInfo.getBirthDate())
				.build();
	}
	
	// 상세 정보 저장
	public DetailInfoDTO saveDetailInfo(DetailInfoDTO detailInfoDTO) throws IOException {
		// 기본 정보 조회
		BasicInfo basicInfo = basicInfoRepository.findById(detailInfoDTO.getBasicInfoId())
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + detailInfoDTO.getBasicInfoId()));
		
		// 프로필 이미지 처리
		String profileImagePath = null;
		if (detailInfoDTO.getProfileImage() != null && !detailInfoDTO.getProfileImage().isEmpty()) {
			profileImagePath = saveProfileImage(detailInfoDTO.getProfileImage());
		}
		
		DetailInfo detailInfo = DetailInfo.builder()
				.basicInfo(basicInfo)
				.profileImagePath(profileImagePath)
				.drinkingFrequency(detailInfoDTO.getDrinkingFrequency())
				.smokingStatus(detailInfoDTO.getSmokingStatus())
				.height(detailInfoDTO.getHeight())
				.pet(detailInfoDTO.getPet())
				.religion(detailInfoDTO.getReligion())
				.childPlan(detailInfoDTO.getChildPlan())
				.mbti(detailInfoDTO.getMbti())
				.build();
		
		DetailInfo savedDetailInfo = detailInfoRepository.save(detailInfo);
		
		return DetailInfoDTO.builder()
				.id(savedDetailInfo.getId())
				.basicInfoId(savedDetailInfo.getBasicInfo().getId())
				.profileImagePath(savedDetailInfo.getProfileImagePath())
				.drinkingFrequency(savedDetailInfo.getDrinkingFrequency())
				.smokingStatus(savedDetailInfo.getSmokingStatus())
				.height(savedDetailInfo.getHeight())
				.pet(savedDetailInfo.getPet())
				.religion(savedDetailInfo.getReligion())
				.childPlan(savedDetailInfo.getChildPlan())
				.mbti(savedDetailInfo.getMbti())
				.build();
	}
	
	// 전체 사용자 정보 조회
	@Transactional(readOnly = true)
	public UserInfoResponseDTO getUserInfo(Long basicInfoId) {
		BasicInfo basicInfo = basicInfoRepository.findById(basicInfoId)
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + basicInfoId));
		
		DetailInfo detailInfo = detailInfoRepository.findByBasicInfoId(basicInfoId)
				.orElse(null);
		
		UserInfoResponseDTO.UserInfoResponseDTOBuilder builder = UserInfoResponseDTO.builder()
				.basicInfoId(basicInfo.getId())
				.name(basicInfo.getName())
				.gender(basicInfo.getGender())
				.birthDate(basicInfo.getBirthDate());
		
		if (detailInfo != null) {
			builder.detailInfoId(basicInfo.getId())
				.profileImagePath(detailInfo.getProfileImagePath())
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
	
	// 기본 정보 수정
	public BasicInfoDTO updateBasicInfo(Long id, BasicInfoDTO basicInfoDTO) {
		BasicInfo basicInfo = basicInfoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("기본 정보를 찾을 수 없습니다. ID: " + id));
		
		basicInfo.setName(basicInfoDTO.getName());
		basicInfo.setGender(basicInfoDTO.getGender());
		basicInfo.setBirthDate(basicInfoDTO.getBirthDate());
		
		BasicInfo updatedBasicInfo = basicInfoRepository.save(basicInfo);
		
		return BasicInfoDTO.builder()
				.id(updatedBasicInfo.getId())
				.name(updatedBasicInfo.getName())
				.gender(updatedBasicInfo.getGender())
				.birthDate(updatedBasicInfo.getBirthDate())
				.build();
	}
	
	// 상세 정보 수정
	public DetailInfoDTO updateDetailInfo(Long id, DetailInfoDTO detailInfoDTO) throws IOException {
		DetailInfo detailInfo = detailInfoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("상세 정보를 찾을 수 없습니다. ID: " + id));
		
		// 새로운 프로필 이미지가 있으면 처리
		if (detailInfoDTO.getProfileImage() != null && !detailInfoDTO.getProfileImage().isEmpty()) {
			// 기존 이미지 삭제(선택사항)
			if (detailInfo.getProfileImagePath() != null) {
				deleteProfileImage(detailInfo.getProfileImagePath());
			}
			String newProfileImagePath = saveProfileImage(detailInfoDTO.getProfileImage());
			detailInfo.setProfileImagePath(newProfileImagePath);
		}
		
		detailInfo.setDrinkingFrequency(detailInfoDTO.getDrinkingFrequency());
		detailInfo.setSmokingStatus(detailInfoDTO.getSmokingStatus());
		detailInfo.setHeight(detailInfoDTO.getHeight());
		detailInfo.setPet(detailInfoDTO.getPet());
		detailInfo.setReligion(detailInfoDTO.getReligion());
		detailInfo.setChildPlan(detailInfoDTO.getChildPlan());
		detailInfo.setMbti(detailInfoDTO.getMbti());
		
		DetailInfo updatedDetailInfo = detailInfoRepository.save(detailInfo);
		
		return DetailInfoDTO.builder()
				.id(updatedDetailInfo.getId())
				.drinkingFrequency(updatedDetailInfo.getDrinkingFrequency())
				.smokingStatus(updatedDetailInfo.getSmokingStatus())
				.height(updatedDetailInfo.getHeight())
				.pet(updatedDetailInfo.getPet())
				.religion(updatedDetailInfo.getReligion())
				.childPlan(updatedDetailInfo.getChildPlan())
				.mbti(updatedDetailInfo.getMbti())
				.build();
	}
	
	// 회원 탈퇴 (MyPageService에서 위임 호출)
	public void deleteUser(Long userId) {
		BasicInfo basicInfo = basicInfoRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
		
		// 프로필 이미지 삭제
		DetailInfo detailInfo = detailInfoRepository.findByBasicInfoId(userId).orElse(null);
		if (detailInfo != null && detailInfo.getProfileImagePath() != null) {
			deleteProfileImage(detailInfo.getProfileImagePath());
		}
		
		// DB에서 삭제 (Cascade 설정으로 DetailInfo도 함께 삭제됨)
		basicInfoRepository.delete(basicInfo);
		
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
}