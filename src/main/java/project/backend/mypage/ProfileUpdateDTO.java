// 프로필 수정 요청용

package project.backend.mypage;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
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
public class ProfileUpdateDTO {
	// 기본 정보
	@NotBlank(message = "이름은 필수 입력 값입니다.")
	private String name;
	
	@NotBlank(message = "성별은 필수 입력 값입니다.")
	private String gender;
	
	@NotBlank(message = "생년월일은 필수 입력 값입니다.")
	private LocalDate birthDate;
	
	// 상세 정보
	private Long detailInfoId;
	
	@JsonIgnore
	private MultipartFile profileImage;
	
	private String place;
	private String drinkingFrequency;
	private String smokingStatus;
	private Integer height;
	private String pet;
	private String religion;
	private String childPlan;
	private String mbti;
	
}