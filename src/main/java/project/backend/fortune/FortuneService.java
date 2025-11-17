package project.backend.fortune;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.backend.fortune.dto.FortuneDTO;
import project.backend.openai.OpenAiService;

@Service
@RequiredArgsConstructor
public class FortuneService {

    private final OpenAiService openAiService;

    public FortuneDTO getTodayFortune() {
        return openAiService.getTodayFortune();
    }
}

