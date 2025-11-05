package project.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import project.backend.user.dto.UserEnums;
import project.backend.user.dto.UserProfileDTO;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	//private String profileImagePath;

	@Enumerated(EnumType.STRING)
	private UserEnums.SexualOrientation sexualOrientation;

	@Enumerated(EnumType.STRING)
	private UserEnums.Job job;

	@Enumerated(EnumType.STRING)
	private UserEnums.region region;

	@Enumerated(EnumType.STRING)
	private UserEnums.DrinkingFrequency drinkingFrequency;

	@Enumerated(EnumType.STRING)
	private UserEnums.SmokingStatus smokingStatus;

	private Integer height;

	@Enumerated(EnumType.STRING)
	private UserEnums.PetPreference petPreference;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.Religion religion;

	@Enumerated(EnumType.STRING)
	private UserEnums.ContactFrequency contactFrequency;

	@Enumerated(EnumType.STRING)
	private UserEnums.Mbti mbti;

	private String introduction;

	public void updateUserProfile(UserProfileDTO userProfileDTO) {
		if (userProfileDTO.getSexualorientation() != null) {
			this.sexualOrientation =  userProfileDTO.getSexualorientation();
		}
		if (userProfileDTO.getJob() != null) {
			this.job = userProfileDTO.getJob();
		}
		if (userProfileDTO.getRegion() != null) {
			this.region = userProfileDTO.getRegion();
		}
		if (userProfileDTO.getDrinkingFrequency() != null) {
			this.drinkingFrequency = userProfileDTO.getDrinkingFrequency();
		}
		if (userProfileDTO.getSmokingStatus() != null) {
			this.smokingStatus = userProfileDTO.getSmokingStatus();
		}
		if (userProfileDTO.getHeight() != null) {
			this.height = userProfileDTO.getHeight();
		}
		if (userProfileDTO.getPetPreference() != null) {
			this.petPreference = userProfileDTO.getPetPreference();
		}
		if (userProfileDTO.getReligion() != null) {
			this.religion = userProfileDTO.getReligion();
		}
		if (userProfileDTO.getContactFrequency() != null) {
			this.contactFrequency = userProfileDTO.getContactFrequency();
		}
		if (userProfileDTO.getMbti() != null) {
			this.mbti = userProfileDTO.getMbti();
		}
		if (userProfileDTO.getIntroduction() != null) {
			this.introduction = userProfileDTO.getIntroduction();
		}
	}
}