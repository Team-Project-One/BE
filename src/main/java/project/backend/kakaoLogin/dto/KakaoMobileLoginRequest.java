package project.backend.kakaoLogin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoMobileLoginRequest {
    private String accessToken;
}
