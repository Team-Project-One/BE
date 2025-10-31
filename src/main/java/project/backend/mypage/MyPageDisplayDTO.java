package project.backend.mypage;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageDisplayDTO {
	private Long userId; //basicInfoId
	private String profileImagePath; // 프로필 사진
	private String name; // 이름
	private Integer age; // 만 나이 (계산값)
	private String gender; // 성별
	// 여기에 직업 추가
	private String place; // 거주 지역
	private LocalDate birthDate; // 생년월일
	private String mbti; // MBTI
	// 여기에 자기소개 추가	
}