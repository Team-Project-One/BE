package project.backend.user.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;

		if (userProfile != null) {
			userProfile.setUser(this);
		}
	}

	public void setKakaoUser(KakaoUser kakaoUser) {
		this.kakaoUser = kakaoUser;

		if (kakaoUser != null) {
			kakaoUser.setUser(this);
		}
	}

	public void updateBasicInfo(String name, UserEnums.Gender gender, LocalDate birthDate) {
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
	}
}
