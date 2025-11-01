package project.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import project.backend.user.dto.UserEnums;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private String profileImagePath;

	@Enumerated(EnumType.STRING)
	private UserEnums.Job job;

	private String region;

	@Enumerated(EnumType.STRING)
	private UserEnums.DrinkingFrequency drinkingFrequency;

	@Enumerated(EnumType.STRING)
	private UserEnums.SmokingStatus smokingStatus;

	private Integer height;

	@Enumerated(EnumType.STRING)
	private UserEnums.PetPreference petPreference;

	@Enumerated(EnumType.STRING)
	private UserEnums.Religion religion;

	@Enumerated(EnumType.STRING)
	private UserEnums.ContactFrequency contactFrequency;

	@Enumerated(EnumType.STRING)
	private UserEnums.Mbti mbti;

}