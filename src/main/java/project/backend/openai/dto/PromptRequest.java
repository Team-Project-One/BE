package project.backend.openai.dto;

public record PromptRequest(
        String prompt,
        Long myUserId,
        Long matchedUserId
) {
}