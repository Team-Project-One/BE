package project.backend.matching.dto;

import lombok.Builder;
import lombok.Getter;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.pythonapi.dto.SajuResponse;

@Getter
@Builder
public class MatchingResultDTO {

    private final SajuResponse sajuResponse;

    private final MyPageDisplayDTO personInfo;
}
