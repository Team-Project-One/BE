package project.backend.kakaoLogin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponse {
	private String accessToken;
	private String refreshToken;
	private boolean isNewUser; // 기본 정보 없음 -> 신규 가입자
}