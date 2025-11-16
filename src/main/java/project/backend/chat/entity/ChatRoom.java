package project.backend.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 두 사용자를 저장. ManyToMany를 사용해 유연하게 관리
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chatroom_participants",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    // 가장 마지막 메시지 (채팅방 목록 정렬용)
    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    public ChatRoom(User user1, User user2) {
        this.participants.add(user1);
        this.participants.add(user2);
        this.lastMessageTimestamp = LocalDateTime.now();
    }

    public void setLastMessage(String lastMessage, LocalDateTime timestamp) {
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = timestamp;
    }

    // 채팅방에서 상대방 유저 찾기
    public User getOtherParticipant(User currentUser) {
        return participants.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);
    }
}