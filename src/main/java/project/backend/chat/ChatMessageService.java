package project.backend.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.chat.dto.ChatMessageDTO;
import project.backend.chat.dto.SendChatMessageRequest;
import project.backend.chat.entity.ChatMessage;
import project.backend.chat.entity.ChatRoom;
import project.backend.chat.repository.ChatMessageRepository;
import project.backend.chat.repository.ChatRoomRepository;
import project.backend.user.UserRepository; // KakaoUserRepository 제거
import project.backend.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository; // User 리포지토리 사용
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 메시지 전송 및 저장
     * (이제 KakaoUser 관련 로직이 완전히 제거되었습니다)
     */
    @Transactional
    public void sendMessage(Long senderUserId, SendChatMessageRequest request) {

        // 1. 발신자(Sender) 조회 (User.id로)
        User senderUser = userRepository.findById(senderUserId)
                .orElseThrow(() -> new EntityNotFoundException("Sender User not found: " + senderUserId));

        // 2. 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + request.getRoomId()));

        // 3. 수신자(Receiver) 조회
        User receiverUser = room.getOtherParticipant(senderUser);
        if (receiverUser == null) {
            // 이 경우는 채팅방에 참여자가 1명이거나 잘못된 경우
            log.error("Receiver not found in room: {}", request.getRoomId());
            throw new EntityNotFoundException("Receiver not found in room");
        }

        // 4. 메시지 엔티티 생성 및 저장
        ChatMessage message = new ChatMessage(room, senderUser, request.getContent());
        chatMessageRepository.save(message);

        // 5. 채팅방 마지막 메시지 업데이트 (목록 정렬용)
        room.setLastMessage(message.getContent(), message.getTimestamp());
        // (트랜잭션 종료 시 자동 저장됨)

        // 6. DTO 변환
        ChatMessageDTO messageDTO = ChatMessageDTO.fromEntity(message);

        // 7. WebSocket으로 메시지 전송
        // StompAuthChannelInterceptor에서 User.id를 String으로 Principal에 저장했으므로,
        // .convertAndSendToUser의 첫 번째 인자(user)는 User.id의 String 값이어야 합니다.

        // 수신자에게 전송
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverUser.getId()), // 수신자의 User.id (String)
                "/queue/chat",                         // 구독 주소
                messageDTO                             // 전송할 메시지
        );

        // 발신자에게도 전송 (본인 화면 업데이트용)
        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderUser.getId()),   // 발신자의 User.id (String)
                "/queue/chat",
                messageDTO
        );

        log.info("Message sent from User {} to User {}", senderUser.getId(), receiverUser.getId());
    }

    /**
     * 특정 채팅방의 메시지 내역 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatMessages(Long roomId) {
        // TODO: (선택) roomId에 현재 로그인한 유저가 포함되어 있는지 확인하는 인가 로직 추가
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
}