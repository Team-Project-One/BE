package project.backend.mypage.dto;

import lombok.Builder;
import lombok.Getter;
import project.backend.user.dto.UserEnums;
import project.backend.user.entity.User;
import project.backend.user.entity.UserProfile;

import java.time.LocalDate;

@Getter
public class MyPageDisplayDTO {

    // User 정보
    private Long userId;
    private String name;
    private UserEnums.Gender gender;
    private LocalDate birthDate;

    // UserProfile 정보
    private UserEnums.Job job;
    private String region;
    private UserEnums.DrinkingFrequency drinkingFrequency;
    private UserEnums.SmokingStatus smokingStatus;
    private Integer height;
    private UserEnums.PetPreference petPreference;
    private UserEnums.Religion religion;
    private UserEnums.ContactFrequency contactFrequency;
    private UserEnums.Mbti mbti;

    @Builder
    public MyPageDisplayDTO(User user, UserProfile profile) {
        this.userId = user.getId();
        this.name = user.getName();
        this.gender = user.getGender();
        this.birthDate = user.getBirthDate();

        if (profile != null) {
            this.job = profile.getJob();
            this.region = profile.getRegion();
            this.drinkingFrequency = profile.getDrinkingFrequency();
            this.smokingStatus = profile.getSmokingStatus();
            this.height = profile.getHeight();
            this.petPreference = profile.getPetPreference();
            this.religion = profile.getReligion();
            this.contactFrequency = profile.getContactFrequency();
            this.mbti = profile.getMbti();
        }
    }

    public static MyPageDisplayDTO fromEntity(User user) {
        return new MyPageDisplayDTO(user, user.getUserProfile());
    }

}
