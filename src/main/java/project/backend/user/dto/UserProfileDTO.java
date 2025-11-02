package project.backend.user.dto;

import lombok.Getter;

@Getter
public class UserProfileDTO {

    private UserEnums.Job job;
    private String region;
    private UserEnums.DrinkingFrequency drinkingFrequency;
    private UserEnums.SmokingStatus smokingStatus;
    private Integer height;
    private UserEnums.PetPreference petPreference;
    private UserEnums.Religion religion;
    private UserEnums.ContactFrequency contactFrequency;
    private UserEnums.Mbti mbti;
}
