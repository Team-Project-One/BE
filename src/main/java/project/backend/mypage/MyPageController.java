package project.backend.mypage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/my-page")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {
	private final MyPageService myPageService;	
	
	// 마이페이지 정보 조회
	@GetMapping("/{userId}")
    public ResponseEntity<MyPageDisplayDTO> getMyPageInfo(@PathVariable("userId") Long userId) {
        log.info("마이페이지 조회 요청 - 사용자 ID: {}", userId);
        MyPageDisplayDTO myPageInfo = myPageService.getMyPageInfo(userId);
        
        return ResponseEntity.ok(myPageInfo);
    }
	
	// 프로필 수정 화면용 전체 정보 조회
	@GetMapping("/profile/{userId}")
	public ResponseEntity<ProfileEditDTO> getProfileForEdit(@PathVariable("userId") Long userId) {
		log.info("프로필 수정 화면 정보 요청 - 사용자 ID: {}", userId);
		ProfileEditDTO profileEdit = myPageService.getProfileForEdit(userId);
		
		return ResponseEntity.ok(profileEdit);
	}
	
	// 프로필 전체 수정 (기본 정보 + 상세 정보, 이미지 포함)
	@PutMapping(value = "/profile/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProfileEditDTO> updateProfile(
			@PathVariable(value = "userId") Long userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "gender", required = false) String gender,
	        @RequestParam(value = "birthDate", required = false) String birthDate,
	        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
	        @RequestParam(value = "place", required = false) String place,
	        @RequestParam(value = "drinkingFrequency", required = false) String drinkingFrequency,
	        @RequestParam(value = "smokingStatus", required = false) String smokingStatus,
	        @RequestParam(value = "height", required = false) Integer height,
	        @RequestParam(value = "pet", required = false) String pet,
	        @RequestParam(value = "religion", required = false) String religion,
	        @RequestParam(value = "childPlan", required = false) String childPlan,
	        @RequestParam(value = "mbti", required = false) String mbti
	    ) throws IOException {
		
		log.info("프로필 수정 요청 - 사용자 ID: {}", userId);
		
		ProfileUpdateDTO updateDTO = ProfileUpdateDTO.builder()
				.name(name)
				.gender(gender)
				.birthDate((birthDate != null && !birthDate.isEmpty()) ? LocalDate.parse(birthDate) : null)
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
		
		ProfileEditDTO updatedProfile = myPageService.updateProfile(userId, updateDTO);
		
		return ResponseEntity.ok(updatedProfile);
	}
	
	// 회원 탈퇴
	@DeleteMapping("/{userId}")
	public ResponseEntity<Map<String, String>> deleteUser(@PathVariable("userId") Long userId) {
		log.info("회원 탈퇴 요청 - 사용자 ID: {}", userId);
		myPageService.deleteUser(userId);
		
		Map<String, String> response = new HashMap<>();
		response.put("message", "회원 탈퇴가 완료되었습니다."); // 회원 탈퇴 성공 여부 확인하려고 작성함
		response.put("userId", userId.toString());
		
		return ResponseEntity.ok(response);
	}
}