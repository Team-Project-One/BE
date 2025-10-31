// 프로필 수정 화면용 - 전체 정보

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
public class ProfileEditDTO {
	// 기본 정보
	private Long basicInfoId;
	private String name;
	private String gender;
	private LocalDate birthDate;
	
	// 상세 정보
	private Long detailInfoId;
	private String profileImagePath;
	private String place;
	private String drinkingFrequency;
	private String smokingStatus;
	private Integer height;
	private String pet;
	private String religion;
	private String childPlan;
	private String mbti;
}