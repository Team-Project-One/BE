package project.backend.dto;

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
public class UserInfoResponseDTO {
	private Long basicInfoId;
	private String name;
	private String gender;
	private LocalDate birthDate;
	private Long detailInfoId;
	private String profileImagePath;
	private String drinkingFrequency;
	private String smokingStatus;
	private Integer height;
	private String pet;
	private String religion;
	private String childPlan;
	private String mbti;
}