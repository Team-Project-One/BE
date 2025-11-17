package project.backend.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 메시지 브로커가 처리할 prefix 설정
        // "/queue" -> 1:1 메시징
        // "/topic" -> 브로드캐스팅 (공지 등)
        registry.enableSimpleBroker("/queue", "/topic");

        // 2. 클라이언트가 서버로 메시지를 보낼 때 사용할 prefix
        // (예: /app/chat/message)
        registry.setApplicationDestinationPrefixes("/app");

        // 3. 1:1 메시징을 위한 prefix
        // (컨트롤러에서 @SendToUser 또는 SimpMessagingTemplate.convertAndSendToUser 사용 시)
        // 클라이언트는 /user/queue/chat 와 같은 주소를 구독합니다.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결을 위한 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP CONNECT 시 JWT 인증을 처리할 인터셉터 등록
        registration.interceptors(stompAuthChannelInterceptor);
    }
}