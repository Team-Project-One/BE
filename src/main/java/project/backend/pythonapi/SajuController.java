package project.backend.pythonapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class SajuController {

    private final SajuService matchService;

    /*
    1.정보 하나는 db에 있는 내 자신 정보, 하나는 db속 random 정보 받아서 넘기기
    2.return 값에 상대방 프로필 정보도 같이 넘기기
    3.사주 정보를 바탕으로한 openAi 응답값(string)도 넘기기
     */
    @PostMapping
    public Mono<SajuResponse> getMatchScore(@RequestBody SajuRequest matchRequest) {
        return matchService.getMatchResult(matchRequest);
    }
}
