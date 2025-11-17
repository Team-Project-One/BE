package project.backend.pythonapi;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.backend.pythonapi.dto.SajuRequest;
import project.backend.pythonapi.dto.SajuResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Tag(name = "사주팔자 궁합점수 받기",description = "프런트에서 사용할 필요 X")
public class SajuController {

    private final SajuService matchService;

    @PostMapping
    public Mono<SajuResponse> getMatchScore(@RequestBody SajuRequest matchRequest) {
        return matchService.getMatchResult(matchRequest);
    }
}
