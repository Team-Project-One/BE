package project.backend.mypage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.dto.UserProfileDTO;

@RestController
@RequestMapping("/my-page")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageService myPageService;	
	
	// 마이페이지 정보 조회
	@GetMapping("/{userId}")
    public ResponseEntity<MyPageDisplayDTO> getMyPageInfo(@PathVariable("userId") Long userId) {
        MyPageDisplayDTO myPageInfo = myPageService.getMyPageInfo(userId);
        
        return ResponseEntity.ok(myPageInfo);
    }

	// 마이페이지 프로필 정보 수정
	@PatchMapping("/{userId}/profile")
	public ResponseEntity<Void> updateProfile(
			@PathVariable("userId") Long userId,
			@RequestBody UserProfileDTO userProfileDTO) {
		myPageService.editProfile(userId, userProfileDTO);

		return ResponseEntity.ok().build();
	}

	
	// 회원 탈퇴
	@DeleteMapping("/{userId}")
	public ResponseEntity<Long> deleteUser(@PathVariable("userId") Long userId) {
		myPageService.deleteUser(userId);

		return ResponseEntity.ok(userId);
	}
}