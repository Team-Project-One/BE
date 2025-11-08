package project.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SignUpRequestDTO {

    // 기본정보
    private String name;
    private LocalDate birthdate;
    private UserEnums.Gender gender;

    // 상세정보
    private UserEnums.SexualOrientation sexualOrientation;
    private UserEnums.Job job;
    private UserEnums.region region;
    private UserEnums.DrinkingFrequency drinkingFrequency;
    private UserEnums.SmokingStatus smokingStatus;
    private Integer height;
    private UserEnums.PetPreference petPreference;
    private UserEnums.Religion religion;
    private UserEnums.ContactFrequency contactFrequency;
    private UserEnums.Mbti mbti;

    //자기소개서
    private String introduction;

    //이미지 url...?
    //private String profileImage;
}
