package project.backend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.backend.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 모든 메시지 조회 (시간순)
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);
}