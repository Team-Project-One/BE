package project.backend.pythonapi;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import project.backend.pythonapi.dto.SajuRequest;
import project.backend.pythonapi.dto.SajuResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SajuService {

    private final WebClient webClient;

    public Mono<SajuResponse> getMatchResult(SajuRequest request) {

        return webClient.post()
                .uri("/match")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SajuResponse.class);

    }
}
