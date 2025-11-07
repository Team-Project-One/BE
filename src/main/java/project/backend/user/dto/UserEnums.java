package project.backend.user.dto;

public class UserEnums {

    public enum Gender {
        MALE,
        FEMALE
    }

    public enum SexualOrientation {
        STRAIGHT, HOMOSEXUAL
    }

    public enum Job {
        UNEMPLOYED,
        STUDENT,
        EMPLOYEE
    }

    public enum region {
        SEOUL,
        GYEONGGI_DO,
        INCHEON,
        BUSAN,
        DAEGU,
        GWANGJU,
        DAEJEON,
        ULSAN,
        SEJONG,
        GANGWON_DO,
        CHUNGCHEONGBUK_DO,
        CHUNGCHEONGNAM_DO,
        JEOLLABUK_DO,
        JEOLLANAM_DO,
        GYEONGSANGBUK_DO,
        GYEONGSANGNAM_DO,
        JEJU_DO,
    }

    public enum DrinkingFrequency {
        NONE,
        ONCE_OR_TWICE_PER_WEEK,
        THREE_TIMES_OR_MORE_PER_WEEK
    }

    public enum SmokingStatus {
        NON_SMOKER,
        SMOKER
    }

    public enum PetPreference {
        NONE,
        DOG,
        CAT,
        OTHER
    }

    public enum Religion {
        NONE,       // 무교
        CATHOLIC,   // 천주교
        CHRISTIAN,  // 기독교
        BUDDHIST,   // 불교
        OTHER       // 기타
    }

    public enum ContactFrequency {
        IMPORTANT,
        NOT_IMPORTANT
    }

    public enum Mbti {
        INTJ, INTP, ENTJ, ENTP,
        INFJ, INFP, ENFJ, ENFP,
        ISTJ, ISFJ, ESTJ, ESFJ,
        ISTP, ISFP, ESTP, ESFP
    }
}
