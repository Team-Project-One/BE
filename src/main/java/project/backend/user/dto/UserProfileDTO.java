package project.backend.user.dto;

import lombok.Getter;

@Getter
public class UserProfileDTO {

    private UserEnums.SexualOrientation  sexualorientation;
    private UserEnums.Job job;
    private UserEnums.region region;
    private UserEnums.DrinkingFrequency drinkingFrequency;
    private UserEnums.SmokingStatus smokingStatus;
    private Integer height;
    private UserEnums.PetPreference petPreference;
    private UserEnums.Religion religion;
    private UserEnums.ContactFrequency contactFrequency;
    private UserEnums.Mbti mbti;
    private String introduction;
}
