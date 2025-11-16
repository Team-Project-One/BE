package project.backend.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendChatMessageRequest {
    private Long roomId;
    private String content;
}