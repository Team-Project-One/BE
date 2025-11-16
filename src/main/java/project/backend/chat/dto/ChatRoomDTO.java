package project.backend.chat.dto;

import lombok.Builder;
import lombok.Getter;
import project.backend.chat.entity.ChatRoom;
import project.backend.user.entity.User;

import java.time.LocalDateTime;

// 채팅방 목록 조회용 DTO
@Getter
@Builder
public class ChatRoomDTO {
    private Long roomId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;
    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;

    public static ChatRoomDTO fromEntity(ChatRoom room, User currentUser) {
        User otherUser = room.getOtherParticipant(currentUser);
        String profileImage = (otherUser.getUserProfile() != null) ? otherUser.getUserProfile().getProfileImagePath() : null;

        return ChatRoomDTO.builder()
                .roomId(room.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getName())
                .otherUserProfileImage(profileImage)
                .lastMessage(room.getLastMessage())
                .lastMessageTimestamp(room.getLastMessageTimestamp())
                .build();
    }
}