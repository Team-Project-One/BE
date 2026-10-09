package project.backend.kakaoLogin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import project.backend.kakaoLogin.dto.KakaoMobileLoginRequest;
import project.backend.kakaoLogin.dto.TestLoginRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller", description = "Kakao login")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code) {

        JwtTokenResponse tokens = kakaoOAuthService.loginWithKakao(code);

        // Build deep-link URL to hand tokens back to the app
        String redirectUrl = String.format(
                "divineapp://auth/kakao/callback?accessToken=%s&refreshToken=%s&newUser=%s",
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                tokens.isNewUser()
        );

        return new RedirectView(redirectUrl);
    }

    @PostMapping("/kakao/mobile")
    public ResponseEntity<JwtTokenResponse> kakaoMobileLogin(
            @RequestBody KakaoMobileLoginRequest request
    ) {
        JwtTokenResponse tokens = kakaoOAuthService.loginWithKakaoAccessToken(request.getAccessToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/test-login")
    public ResponseEntity<JwtTokenResponse> testLogin(@RequestBody(required = false) TestLoginRequest request) {
        JwtTokenResponse tokens = kakaoOAuthService.loginAsTestUser(request);
        return ResponseEntity.ok(tokens);
    }
}