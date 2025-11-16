package project.backend.chat.dto;

import lombok.Builder;
import lombok.Getter;
import project.backend.chat.entity.ChatMessage;

import java.time.LocalDateTime;

// 메시지 조회 및 실시간 전송용 DTO
@Getter
@Builder
public class ChatMessageDTO {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;

    public static ChatMessageDTO fromEntity(ChatMessage message) {
        return ChatMessageDTO.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}