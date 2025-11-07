package project.backend.mypage;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.user.UserService;
import project.backend.user.dto.UserProfileDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final UserService userService;
	
	// 마이페이지 조회
	public MyPageDisplayDTO getMyPageInfo(Long userId) {
		return userService.getUserInfo(userId);
	}

    // user 정보 수정
	@Transactional
    public void editProfile(Long userId, UserProfileDTO userProfileDTO) {
        userService.updateUserProfileInfo(userId, userProfileDTO);
    }

	// user 프로필 사진 수정
	@Transactional
	public String updateProfileImage(Long userId, MultipartFile profileImage) throws IOException {
		return userService.updateUserProfileImage(userId, profileImage);
	}

	// 회원 탈퇴
	@Transactional
	public void deleteUser(Long userId) {
		userService.deleteUser(userId);
	}
	
}