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

    @PostMapping
    public Mono<SajuResponse> getMatchScore(@RequestBody SajuRequest matchRequest) {
        return matchService.getMatchResult(matchRequest);
    }
}
