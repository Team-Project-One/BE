package project.backend.kakaoLogin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<JwtTokenResponse> kakaoCallback(@RequestParam("code") String code) {
        JwtTokenResponse tokens = kakaoOAuthService.loginWithKakao(code);
        return ResponseEntity.ok(tokens);
    }
}