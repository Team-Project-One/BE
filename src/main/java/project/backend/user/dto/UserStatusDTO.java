package project.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserStatusDTO {

    private Long userId;
    private boolean profileCompleted;
}

