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

	@Setter
	private String profileImagePath;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.SexualOrientation sexualOrientation;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.Job job;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.region region;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.DrinkingFrequency drinkingFrequency;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.SmokingStatus smokingStatus;

	@Setter
	private Integer height;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.PetPreference petPreference;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.Religion religion;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.ContactFrequency contactFrequency;

	@Setter
	@Enumerated(EnumType.STRING)
	private UserEnums.Mbti mbti;

	@Setter
	private String introduction;

	public void updateUserProfile(UserProfileDTO userProfileDTO) {
		if (userProfileDTO.getSexualOrientation() != null) {
			this.sexualOrientation =  userProfileDTO.getSexualOrientation();
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
			// UNKNOWN은 "설정 안 됨" 의미로 사용하고, DB에는 null로 저장
			if (userProfileDTO.getMbti() == UserEnums.Mbti.UNKNOWN) {
				this.mbti = null;
			} else {
				this.mbti = userProfileDTO.getMbti();
			}
		}
		if (userProfileDTO.getIntroduction() != null) {
			this.introduction = userProfileDTO.getIntroduction();
		}
	}
}