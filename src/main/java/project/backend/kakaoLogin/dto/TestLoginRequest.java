package project.backend.kakaoLogin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestLoginRequest {
    private Long testUserNumber;
    private String name;
    private String gender;
    private String birthDate;
}

