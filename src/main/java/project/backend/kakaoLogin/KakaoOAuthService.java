package project.backend.kakaoLogin;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

	private final KakaoUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public JwtTokenResponse loginWithKakao(String code) {
        // access token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String,String>> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUri, HttpMethod.POST, tokenRequest, String.class);

        Map<String,Object> tokenMap;
        try {
            tokenMap = objectMapper.readValue(tokenResponse.getBody(), new TypeReference<Map<String,Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 요청 실패", e);
        }

        String kakaoAccessToken = (String) tokenMap.get("access_token");

        // 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<Void> userInfoReq = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userInfoResp = restTemplate.exchange(userInfoUri, HttpMethod.GET, userInfoReq, String.class);

        Map<String,Object> userMap;
        try {
            userMap = objectMapper.readValue(userInfoResp.getBody(), new TypeReference<Map<String,Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }

        // 사용자 정보 파싱
        String kakaoId = String.valueOf(userMap.get("id"));
        Map<String,Object> kakaoAccount = (Map<String,Object>) userMap.get("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        // DB 사용자 확인/저장
        Optional<KakaoUser> existingUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = existingUser.isEmpty();

        KakaoUser user = existingUser.orElseGet(() -> {
            KakaoUser newUser = KakaoUser.builder()
                    .kakaoId(kakaoId)
                    .email(email)
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                (user.getEmail() != null) ? user.getEmail() : user.getKakaoId(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                (user.getEmail() != null) ? user.getEmail() : user.getKakaoId(),
                user.getRole().name()
        );

        // RefreshToken 저장 (기존 있으면 갱신)
        refreshTokenRepository.findByUserId(user.getKakaoId())
                .ifPresentOrElse(existing -> {
                    existing.setToken(refreshToken);
                    refreshTokenRepository.save(existing);
                }, () -> {
                    refreshTokenRepository.save(
                            RefreshToken.builder()
                                    .userId(user.getKakaoId())
                                    .token(refreshToken)
                                    .build()
                    );
                });

        // 반환
        JwtTokenResponse resp = new JwtTokenResponse();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setNewUser(isNewUser);

        return resp;
    }
}