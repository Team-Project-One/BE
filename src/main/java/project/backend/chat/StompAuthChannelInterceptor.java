package project.backend.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import project.backend.kakaoLogin.JwtTokenProvider;
import project.backend.kakaoLogin.KakaoUser;
import project.backend.kakaoLogin.KakaoUserRepository;
import project.backend.user.entity.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoUserRepository kakaoUserRepository; // User.id를 찾기 위해 필요

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");

            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    // 1. JWT에서 'kakaoId' 또는 'email' (String) 추출
                    String principalIdentifier = jwtTokenProvider.getUserIdFromToken(token);

                    if (principalIdentifier != null) {
                        try {
                            // 2. DB에서 KakaoUser 조회
                            KakaoUser kakaoUser = kakaoUserRepository.findByEmail(principalIdentifier)
                                    .or(() -> kakaoUserRepository.findByKakaoId(principalIdentifier))
                                    .orElseThrow(() -> new EntityNotFoundException("KakaoUser not found: " + principalIdentifier));

                            // 3. KakaoUser에 연결된 'User' 엔티티 조회
                            User user = kakaoUser.getUser();
                            if (user == null) {
                                throw new EntityNotFoundException("User entity not linked for KakaoUser: " + principalIdentifier + ". (Signup not completed)");
                            }

                            // 4. 'User.id' (Long)를 String으로 변환
                            String userId = String.valueOf(user.getId());

                            // 5. WebSocket 세션의 Principal로 'User.id' (String)를 설정
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userId, null, null);
                            accessor.setUser(authentication);
                            log.info("STOMP user authenticated. Principal name set to User.id: {}", userId);

                        } catch (EntityNotFoundException e) {
                            log.warn("STOMP connection failed: {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("STOMP connection failed: Invalid JWT token");
                }
            } else {
                log.warn("STOMP connection failed: Missing or invalid Authorization header");
            }
        }
        return message;
    }
}