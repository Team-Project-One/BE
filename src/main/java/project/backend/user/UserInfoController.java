package project.backend.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.backend.user.dto.BasicInfoDTO;
import project.backend.user.dto.DetailInfoDTO;
import project.backend.user.dto.UserInfoResponseDTO;

@RestController
@RequestMapping("/api/user-info")
@RequiredArgsConstructor
@Slf4j
public class UserInfoController {
	private final UserInfoService userInfoService;
	
	// 기본 정보 저장
	@PostMapping("/basic")
	public ResponseEntity<BasicInfoDTO>	saveBasicInfo(@Valid @RequestBody BasicInfoDTO basicInfoDTO) {
		log.info("기본 정보 저장 요청: {}", basicInfoDTO.getName());
		BasicInfoDTO savedBasicInfo = userInfoService.saveBasicInfo(basicInfoDTO);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(savedBasicInfo);
	}
	
	// 상세 정보 저장(multipart/form-data)
	@PostMapping(value = "/detail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DetailInfoDTO> saveDetailInfo(
			@RequestParam("basicInfoId") Long basicInfoId,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
			@RequestParam(value = "place", required = false) String place,
			@RequestParam(value = "drinkingFrequency", required = false) String drinkingFrequency,
			@RequestParam(value = "smokingStatus", required = false) String smokingStatus,
			@RequestParam(value = "height", required = false) Integer height,
			@RequestParam(value = "pet", required  = false) String pet,
			@RequestParam(value = "religion", required = false) String religion,
			@RequestParam(value = "childPlan", required = false) String childPlan,
			@RequestParam(value = "mbti", required = false) String mbti) {
		
		log.info("상세 정보 저장 요청 - 기본정보 ID: {}", basicInfoId);
		
		DetailInfoDTO detailInfoDTO = DetailInfoDTO.builder()
				.basicInfoId(basicInfoId)
				.profileImage(profileImage)
				.place(place)
				.drinkingFrequency(drinkingFrequency)
				.smokingStatus(smokingStatus)
				.height(height)
				.pet(pet)
				.religion(religion)
				.childPlan(childPlan)
				.mbti(mbti)
				.build();
		
		try {
			DetailInfoDTO savedDetailInfo = userInfoService.saveDetailInfo(detailInfoDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedDetailInfo);
		} catch(Exception e) {
			log.error("상세 정보 저장 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	// 테스트용 API
	@GetMapping("/{userId}")
	public ResponseEntity<UserInfoResponseDTO> getUserInfo(@PathVariable("userId") Long userId) {
	    log.info("사용자 정보 조회 요청 - 사용자 ID: {}", userId);
	    UserInfoResponseDTO userInfo = userInfoService.getUserInfo(userId);
	    return ResponseEntity.ok(userInfo);
	}
	
	@GetMapping("/same-place")
	public ResponseEntity<List<UserInfoResponseDTO>> getUsersBySamePlace(@RequestParam("basicInfoId") Long basicInfoId) {
		List<UserInfoResponseDTO> users = userInfoService.getUsersBySamePlace(basicInfoId);
		return ResponseEntity.ok(users);
	}
}