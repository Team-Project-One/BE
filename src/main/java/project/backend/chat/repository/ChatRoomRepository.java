package project.backend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.backend.chat.entity.ChatRoom;
import project.backend.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 특정 유저가 참여하고 있는 모든 채팅방 목록 조회 (최신 메시지 순 정렬)
    @Query("SELECT r FROM ChatRoom r WHERE :user MEMBER OF r.participants ORDER BY r.lastMessageTimestamp DESC")
    List<ChatRoom> findAllByUser(@Param("user") User user);

    // 두 명의 유저로 채팅방 찾기
    @Query("SELECT r FROM ChatRoom r WHERE :user1 MEMBER OF r.participants AND :user2 MEMBER OF r.participants")
    Optional<ChatRoom> findByParticipants(@Param("user1") User user1, @Param("user2") User user2);
}