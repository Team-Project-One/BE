package project.backend.mypage;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	// 회원 탈퇴
	@Transactional
	public void deleteUser(Long userId) {
		userService.deleteUser(userId);
	}
	
	// 나이 계산 (만 나이) 아직 사용 x
	private Integer calculateAge(LocalDate birthDate) {
		return (birthDate == null) ? null : Period.between(birthDate, LocalDate.now()).getYears();
	}
}