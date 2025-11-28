package project.backend.kakaoLogin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller", description = "카카오 로그인")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code) {

        JwtTokenResponse tokens = kakaoOAuthService.loginWithKakao(code);

        // 딥링크 URL 구성 (앱으로 반환)
        String redirectUrl = String.format(
                "divineapp://auth/kakao/callback?accessToken=%s&refreshToken=%s&newUser=%s",
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                tokens.isNewUser()
        );

        return new RedirectView(redirectUrl);
    }
}

/*
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller" ,description = "카카오 로그인")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<JwtTokenResponse> kakaoCallback(@RequestParam("code") String code) {
        JwtTokenResponse tokens = kakaoOAuthService.loginWithKakao(code);
        return ResponseEntity.ok(tokens);
    }
}
*/