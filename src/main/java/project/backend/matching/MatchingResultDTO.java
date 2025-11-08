package project.backend.matching;

import lombok.Builder;
import lombok.Getter;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.pythonapi.SajuResponse;

@Getter
@Builder
public class MatchingResultDTO {

    private final SajuResponse sajuResponse;

    private final MyPageDisplayDTO personInfo;
}
