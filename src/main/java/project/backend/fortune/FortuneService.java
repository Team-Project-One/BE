package project.backend.fortune;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import project.backend.fortune.dto.FortuneDTO;
import project.backend.openai.OpenAiService;

@Service
@RequiredArgsConstructor
public class FortuneService {

    private final OpenAiService openAiService;

    @Cacheable(value = "fortunes", key = "T(java.time.LocalDate).now().toString() + ':' + #birthDate")
    public FortuneDTO getTodayFortune(String birthDate) {
        return openAiService.getTodayFortune(birthDate);
    }
}

