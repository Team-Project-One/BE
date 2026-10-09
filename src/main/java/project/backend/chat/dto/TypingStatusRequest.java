package project.backend.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TypingStatusRequest {

    private Long roomId;
    private boolean typing;
}





