package project.backend.pythonapi;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final WebClient webClient;

    public Mono<MatchResponse> getMatchResult(MatchRequest request) {

        return webClient.post()
                .uri("/match")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MatchResponse.class);

    }
}
