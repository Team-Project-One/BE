package project.backend.openai.dto;

public record RecommendationRequest(
        Long myUserId,
        Long matchedUserId
) {
}

