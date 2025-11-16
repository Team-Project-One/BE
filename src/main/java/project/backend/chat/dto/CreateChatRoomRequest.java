package project.backend.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChatRoomRequest {
    // 매칭된 상대방의 User ID
    private Long matchedUserId;
}