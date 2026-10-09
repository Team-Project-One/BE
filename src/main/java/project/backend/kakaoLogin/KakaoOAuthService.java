package project.backend.kakaoLogin;

import java.time.LocalDate;
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
import project.backend.kakaoLogin.dto.TestLoginRequest;
import project.backend.user.entity.User;
import project.backend.user.entity.UserProfile;
import project.backend.user.dto.UserEnums;

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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUri, HttpMethod.POST, tokenRequest, String.class);

        Map<String, Object> tokenMap;
        try {
            tokenMap = objectMapper.readValue(tokenResponse.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve Kakao access token", e);
        }

        String kakaoAccessToken = (String) tokenMap.get("access_token");

        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new IllegalStateException("Kakao access token is missing");
        }

        return processKakaoAccessToken(kakaoAccessToken);
    }

    public JwtTokenResponse loginWithKakaoAccessToken(String kakaoAccessToken) {
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new IllegalArgumentException("Kakao access token is required");
        }
        return processKakaoAccessToken(kakaoAccessToken);
    }

    private JwtTokenResponse processKakaoAccessToken(String kakaoAccessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<Void> userInfoReq = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userInfoResp = restTemplate.exchange(userInfoUri, HttpMethod.GET, userInfoReq, String.class);

        Map<String, Object> userMap;
        try {
            userMap = objectMapper.readValue(userInfoResp.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Kakao user info", e);
        }

        String kakaoId = String.valueOf(userMap.get("id"));
        Map<String, Object> kakaoAccount = (Map<String, Object>) userMap.get("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        Optional<KakaoUser> existingUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = existingUser.isEmpty();

        KakaoUser user = existingUser.orElse(null);

        if (user == null) {
            UserProfile profile = UserProfile.builder().build();
            User newUser = User.builder()
                    .name(null)
                    .gender(null)
                    .birthDate(null)
                    .userProfile(profile)
                    .build();
            profile.setUser(newUser);

            user = KakaoUser.builder()
                    .kakaoId(kakaoId)
                    .email(email)
                    .role(Role.USER)
                    .user(newUser)
                    .build();

            newUser.setKakaoUser(user);

            userRepository.save(user);
        } else if (email != null && !email.isBlank()) {
            if (user.getEmail() == null || !user.getEmail().equals(email)) {
                user.setEmail(email);
                userRepository.save(user);
            }
        }

        return buildTokenResponse(user, isNewUser);
    }

    public JwtTokenResponse loginAsTestUser(TestLoginRequest request) {
        String userNumber = request != null && request.getTestUserNumber() != null
                ? String.valueOf(request.getTestUserNumber())
                : "2";

        String testKakaoId = "TEST_USER_" + userNumber;

        KakaoUser user = userRepository.findByKakaoId(testKakaoId)
                .orElseGet(() -> createTestUser(testKakaoId, request));

        if (user != null) {
            updateTestUserProfile(user, request);
        }

        return buildTokenResponse(user, false);
    }

    private KakaoUser createTestUser(String testKakaoId, TestLoginRequest request) {
        String userNumber = testKakaoId.substring(testKakaoId.lastIndexOf("_") + 1);
        String name = (request != null && request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : userNumber.equals("2") ? "유지민" : userNumber.equals("3") ? "김민정" : "테스트유저" + userNumber;

        String birthDate = (request != null && request.getBirthDate() != null && !request.getBirthDate().isBlank())
                ? request.getBirthDate()
                : userNumber.equals("2") ? "2003-08-30" : userNumber.equals("3") ? "2001-12-12" : "1995-01-01";

        UserEnums.Gender gender = UserEnums.Gender.FEMALE;
        if (request != null && request.getGender() != null) {
            try {
                gender = UserEnums.Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        } else if (userNumber.equals("3")) {
            gender = UserEnums.Gender.FEMALE;
        }

        LocalDate parsedBirthDate;
        try {
            parsedBirthDate = LocalDate.parse(birthDate);
        } catch (Exception e) {
            parsedBirthDate = userNumber.equals("2") ? LocalDate.of(2003, 8, 30) 
                    : userNumber.equals("3") ? LocalDate.of(2001, 12, 12) 
                    : LocalDate.of(1995, 1, 1);
        }

        String introduction = userNumber.equals("2") 
                ? "저는 마라탕을 좋아하고 노래부르는 걸 좋아해요. 또한, 식물원 가는 걸 좋아해요."
                : userNumber.equals("3")
                ? "저는 마라탕을 싫어하고 떡볶이를 좋아해요."
                : "테스트용 계정입니다. 매칭 기능 테스트를 위해 생성된 사용자입니다.";

        UserProfile profile = UserProfile.builder()
                .region(UserEnums.region.GYEONGGI_DO)
                .job(UserEnums.Job.EMPLOYEE)
                .drinkingFrequency(UserEnums.DrinkingFrequency.ONCE_OR_TWICE_PER_WEEK)
                .smokingStatus(UserEnums.SmokingStatus.NON_SMOKER)
                .height(168)
                .petPreference(UserEnums.PetPreference.DOG)
                .religion(UserEnums.Religion.NONE)
                .contactFrequency(UserEnums.ContactFrequency.IMPORTANT)
                .mbti(UserEnums.Mbti.ENFP)
                .sexualOrientation(UserEnums.SexualOrientation.STRAIGHT)
                .introduction(introduction)
                .build();

        User newUser = User.builder()
                .name(name)
                .gender(gender)
                .birthDate(parsedBirthDate)
                .userProfile(profile)
                .build();

        KakaoUser kakaoUser = KakaoUser.builder()
                .kakaoId(testKakaoId)
                .email(testKakaoId.toLowerCase() + "@example.com")
                .role(Role.USER)
                .user(newUser)
                .build();

        newUser.setKakaoUser(kakaoUser);
        profile.setUser(newUser);

        return userRepository.save(kakaoUser);
    }

    private void updateTestUserProfile(KakaoUser kakaoUser, TestLoginRequest request) {
        String userNumber = kakaoUser.getKakaoId().replace("TEST_USER_", "");
        String name = (request != null && request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : userNumber.equals("2") ? "유지민" : userNumber.equals("3") ? "김민정" : "테스트유저" + userNumber;
        LocalDate birthDate;
        try {
            birthDate =
                    (request != null && request.getBirthDate() != null && !request.getBirthDate().isBlank())
                            ? LocalDate.parse(request.getBirthDate())
                            : userNumber.equals("2") ? LocalDate.of(2003, 8, 30) 
                            : userNumber.equals("3") ? LocalDate.of(2001, 12, 12) 
                            : LocalDate.of(1995, 1, 1);
        } catch (Exception e) {
            birthDate = userNumber.equals("2") ? LocalDate.of(2003, 8, 30) 
                    : userNumber.equals("3") ? LocalDate.of(2001, 12, 12) 
                    : LocalDate.of(1995, 1, 1);
        }

        UserEnums.Gender gender = UserEnums.Gender.FEMALE;
        if (request != null && request.getGender() != null) {
            try {
                gender = UserEnums.Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        } else if (userNumber.equals("3")) {
            gender = UserEnums.Gender.FEMALE;
        }

        User user = kakaoUser.getUser();
        if (user == null) {
            UserProfile profile = UserProfile.builder().build();
            user = User.builder()
                    .name(name)
                    .gender(gender)
                    .birthDate(birthDate)
                    .userProfile(profile)
                    .build();
            kakaoUser.setUser(user);
        } else {
            user.updateBasicInfo(name, gender, birthDate);
        }

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().build();
            user.setUserProfile(profile);
        }

        String introduction = userNumber.equals("2") 
                ? "저는 마라탕을 좋아하고 노래부르는 걸 좋아해요. 또한, 식물원 가는 걸 좋아해요."
                : userNumber.equals("3")
                ? "저는 마라탕을 싫어하고 떡볶이를 좋아해요."
                : "테스트용 계정입니다. 매칭 기능 테스트를 위해 생성된 사용자입니다.";

        profile.setSexualOrientation(UserEnums.SexualOrientation.STRAIGHT);
        profile.setRegion(UserEnums.region.GYEONGGI_DO);
        profile.setJob(UserEnums.Job.EMPLOYEE);
        profile.setDrinkingFrequency(UserEnums.DrinkingFrequency.ONCE_OR_TWICE_PER_WEEK);
        profile.setSmokingStatus(UserEnums.SmokingStatus.NON_SMOKER);
        profile.setPetPreference(UserEnums.PetPreference.DOG);
        profile.setReligion(UserEnums.Religion.NONE);
        profile.setContactFrequency(UserEnums.ContactFrequency.IMPORTANT);
        profile.setMbti(UserEnums.Mbti.ENFP);
        profile.setHeight(168);
        profile.setIntroduction(introduction);

        userRepository.save(kakaoUser);
    }

    private JwtTokenResponse buildTokenResponse(KakaoUser user, boolean isNewUser) {
        String identifier = (user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : user.getKakaoId();

        String accessToken = jwtTokenProvider.createAccessToken(identifier, user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(identifier, user.getRole().name());

        refreshTokenRepository.findByUserId(user.getKakaoId())
                .ifPresentOrElse(existing -> {
                    existing.setToken(refreshToken);
                    refreshTokenRepository.save(existing);
                }, () -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(user.getKakaoId())
                                .token(refreshToken)
                                .build()
                ));

        JwtTokenResponse resp = new JwtTokenResponse();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setNewUser(isNewUser);
        resp.setKakaoId(user.getKakaoId());
        resp.setEmail(user.getEmail());

        return resp;
    }
}
