package project.backend.chat.dto;

import lombok.Builder;
import lombok.Getter;
import project.backend.chat.entity.ChatRoom;
import project.backend.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 채팅방 목록 조회용 DTO
@Getter
@Builder
public class ChatRoomDTO {
    private Long roomId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;
    private String otherUserGender;
    private LocalDate otherUserBirthDate;
    private String lastMessage;
    private LocalDateTime lastMessageTimestamp;

    public static ChatRoomDTO fromEntity(ChatRoom room, User currentUser) {
        User otherUser = room.getOtherParticipant(currentUser);
        if (otherUser == null) {
            // 참여자가 1명뿐이거나 데이터가 잘못된 경우 방어적으로 처리
            return ChatRoomDTO.builder()
                    .roomId(room.getId())
                    .otherUserId(null)
                    .otherUserName("알 수 없는 사용자")
                    .otherUserProfileImage(null)
                    .otherUserGender(null)
                    .otherUserBirthDate(null)
                    .lastMessage(room.getLastMessage())
                    .lastMessageTimestamp(room.getLastMessageTimestamp())
                    .build();
        }

        String profileImage = (otherUser.getUserProfile() != null) ? otherUser.getUserProfile().getProfileImagePath() : null;

        return ChatRoomDTO.builder()
                .roomId(room.getId())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getName())
                .otherUserProfileImage(profileImage)
                .otherUserGender(otherUser.getGender() != null ? otherUser.getGender().name() : null)
                .otherUserBirthDate(otherUser.getBirthDate())
                .lastMessage(room.getLastMessage())
                .lastMessageTimestamp(room.getLastMessageTimestamp())
                .build();
    }
}