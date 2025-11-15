package project.backend.user.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;
import project.backend.kakaoLogin.KakaoUser;
import project.backend.user.dto.UserEnums;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Enumerated(EnumType.STRING)
	private UserEnums.Gender gender;
	
	private LocalDate birthDate;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private UserProfile userProfile;

	@OneToOne
	@JoinColumn(name = "kakao_user_id")
	private KakaoUser kakaoUser;

	@Builder
	public User(String name, UserEnums.Gender gender, LocalDate birthDate, UserProfile userProfile) {
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;

		this.setUserProfile(userProfile);
	}

	private void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;

		if (userProfile != null) {
			userProfile.setUser(this);
		}
	}
}