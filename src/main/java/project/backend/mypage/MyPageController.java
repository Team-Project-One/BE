package project.backend.mypage;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.dto.UserProfileDTO;

@RestController
@RequestMapping("/my-page")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "마이페이지 관련 컨트롤러(정보 조회, 프로필 수정, 프로필 사진 수정, 회원탈퇴")
public class MyPageController {

	private final MyPageService myPageService;	
	
	// 마이페이지 정보 조회
	@GetMapping("/{userId}")
	@Operation(summary = "내 모든 정보 조회" , description = "기본,상세 정보 + 프로필 사진까지 받는 메서드")
    public ResponseEntity<MyPageDisplayDTO> getMyPageInfo(@PathVariable("userId") Long userId) {
        MyPageDisplayDTO myPageInfo = myPageService.getMyPageInfo(userId);
        
        return ResponseEntity.ok(myPageInfo);
    }

	// 마이페이지 프로필 정보 수정
	@PatchMapping("/{userId}/profile")
	@Operation(summary = "프로필 정보 수정" ,description = "상세 정보 + 자기소개 글만 수정 가능(프로필 사진은 수정 X)")
	public ResponseEntity<Void> updateProfile(
			@PathVariable("userId") Long userId,
			@RequestBody UserProfileDTO userProfileDTO) {
		myPageService.editProfile(userId, userProfileDTO);

		return ResponseEntity.ok().build();
	}

	// 마이페이지 프로필 이미지 수정
	@PatchMapping(value = "/{userId}/profile-image", consumes = "multipart/form-data")
	@Operation(summary = "프로필 이미지 수정")
	public ResponseEntity<String> updateProfileImage(
			@PathVariable("userId") Long userId,
			@RequestPart("profileImage") MultipartFile profileImage) throws IOException {
		String imagePath = myPageService.updateProfileImage(userId, profileImage);

		return ResponseEntity.ok(imagePath);
	}

	// 회원 탈퇴
	@DeleteMapping("/{userId}")
	@Operation(summary = "회원 탈퇴")
	public ResponseEntity<Long> deleteUser(@PathVariable("userId") Long userId) {
		myPageService.deleteUser(userId);

		return ResponseEntity.ok(userId);
	}
}