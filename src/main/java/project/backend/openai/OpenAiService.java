package project.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import project.backend.fortune.dto.FortuneDTO;

@Service
public class OpenAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String getGptResponse(String prompt) {

            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
    }

    //오늘의 운세(4가지)
    public FortuneDTO getTodayFortune() {
        String prompt = """
                오늘의 운세를 다음 JSON 형식으로 반환해주세요.
                각 운세는 한 문단으로 작성해주세요 (100자 이내).
                
                {
                    "overallFortune": "총운 설명",
                    "loveFortune": "애정운 설명",
                    "moneyFortune": "금전운 설명",
                    "careerFortune": "직장운 설명"
                }
                
                반환 형식은 반드시 JSON만 반환하고, 다른 텍스트는 포함하지 마세요.
                """;

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String jsonResponse = extractJsonFromResponse(response);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            return FortuneDTO.builder()
                    .overallFortune(jsonNode.get("overallFortune").asText())
                    .loveFortune(jsonNode.get("loveFortune").asText())
                    .moneyFortune(jsonNode.get("moneyFortune").asText())
                    .careerFortune(jsonNode.get("careerFortune").asText())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("운세를 가져오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //ai 응답에서 json 만 추출
    private String extractJsonFromResponse(String response) {
        String trimmed = response.trim();
        
        if (trimmed.startsWith("```json")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}") + 1;
            return trimmed.substring(start, end);
        }
        
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}") + 1;
            return trimmed.substring(start, end);
        }
        
        if (trimmed.startsWith("{")) {
            int end = trimmed.lastIndexOf("}") + 1;
            return trimmed.substring(0, end);
        }
        
        int startIndex = trimmed.indexOf("{");
        int endIndex = trimmed.lastIndexOf("}");
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1);
        }
        
        return trimmed;
    }
}