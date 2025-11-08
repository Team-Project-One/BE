package project.backend.fortune.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FortuneDTO {

    private final String overallFortune;    // 총운
    private final String loveFortune;       // 애정운
    private final String moneyFortune;      // 금전운
    private final String careerFortune;     // 직장운
}

