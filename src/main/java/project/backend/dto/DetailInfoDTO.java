package project.backend.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class DetailInfoDTO {
	private Long id;
	private Long basicInfoId;
	
	@JsonIgnore
	private MultipartFile profileImage;
	
	private String profileImagePath;
	private String drinkingFrequency;
	private String smokingStatus;
	private Integer height;
	private String pet;
	private String religion;
	private String childPlan;
	private String mbti;
}