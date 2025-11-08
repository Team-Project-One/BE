package project.backend.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.backend.openai.dto.PromptRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/prompt")
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping
    public String getGptResponse(@RequestBody PromptRequest request) {
        return openAiService.getGptResponse(request.prompt());
    }
}
