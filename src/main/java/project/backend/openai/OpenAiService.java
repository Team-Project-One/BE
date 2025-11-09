package project.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import project.backend.fortune.dto.FortuneDTO;
import project.backend.openai.dto.ConversationTopicDTO;
import project.backend.openai.dto.DatingCourseDTO;
import project.backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

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

    //대화주제 추천
    public ConversationTopicDTO getConversationTopics(User myUser, User matchedUser) {
        String userInfo = buildUserInfoString(myUser);
        String matchedUserInfo = buildUserInfoString(matchedUser);
        
        String prompt = String.format("""
                다음 두 사람의 정보를 바탕으로 대화주제를 추천해주세요.
                
                [내 정보]
                %s
                
                [상대방 정보]
                %s
                
                두 사람의 공통 관심사, 성격, 취미 등을 고려하여 대화하기 좋은 주제 5개를 추천해주세요.
                각 주제는 간단명료하게 한 문장으로 작성해주세요.
                각 주제 앞에 내용과 어울리는 이모지를 하나씩 붙여주세요.
                
                다음 JSON 형식으로 반환해주세요:
                {
                    "topics": ["😊 주제1", "💡 주제2", "💬 주제3", "🤔 주제4", "💖 주제5"]
                }
                
                이모지 예시: 😊 💡 💬 🤔 💖 🎯 🌟 🎨 🎵 🎮 📚 🎬 🍔 🎪 🎭
                반환 형식은 반드시 JSON만 반환하고, 다른 텍스트는 포함하지 마세요.
                """, userInfo, matchedUserInfo);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String jsonResponse = extractJsonFromResponse(response);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            
            List<String> topics = new ArrayList<>();
            JsonNode topicsArray = jsonNode.get("topics");
            if (topicsArray != null && topicsArray.isArray()) {
                for (JsonNode topic : topicsArray) {
                    topics.add(topic.asText());
                }
            }

            return ConversationTopicDTO.builder()
                    .topics(topics)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("대화주제를 가져오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //데이트 코스 추천
    public DatingCourseDTO getDatingCourseRecommendation(User myUser, User matchedUser) {
        String userInfo = buildUserInfoString(myUser);
        String matchedUserInfo = buildUserInfoString(matchedUser);
        String region = myUser.getUserProfile().getRegion() != null 
                ? myUser.getUserProfile().getRegion().name() 
                : "서울";
        
        String prompt = String.format("""
                다음 두 사람의 정보를 바탕으로 데이트 코스를 추천해주세요.
                
                [내 정보]
                %s
                
                [상대방 정보]
                %s
                
                [지역]
                %s
                
                두 사람의 성격, 취미, 지역을 고려하여 데이트하기 좋은 코스 5개를 추천해주세요.
                각 코스는 다음 형식으로 작성해주세요: "이모지 장소명 - 활동 설명"
                - 각 코스 앞에 내용과 어울리는 이모지를 하나씩 붙여주세요
                - 구체적인 장소명을 반드시 포함해주세요 (예: "한강 공원", "CGV 강남", "이태원 맛집 거리")
                - 지역에 맞는 실제 존재하는 장소를 추천해주세요
                - 활동 설명도 함께 작성해주세요
                
                다음 JSON 형식으로 반환해주세요:
                {
                    "courses": ["📍 한강 공원 - 저녁 산책과 야경 감상", "🎬 CGV 강남 - 영화 관람 후 카페 투어", "🍽️ 이태원 맛집 거리 - 다양한 음식 체험", "🎨 삼청동 갤러리 투어 - 예술적인 데이트", "🌳 남산타워 - 야경 감상"]
                }
                
                이모지 예시: 📍 🎬 🍽️ 🎨 🌳 🎯 🎪 🎭 🏛️ 🎵 🎮 📚 🍔 ☕ 🌸 🏖️
                반환 형식은 반드시 JSON만 반환하고, 다른 텍스트는 포함하지 마세요.
                """, userInfo, matchedUserInfo, region);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String jsonResponse = extractJsonFromResponse(response);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            
            List<String> courses = new ArrayList<>();
            JsonNode coursesArray = jsonNode.get("courses");
            if (coursesArray != null && coursesArray.isArray()) {
                for (JsonNode course : coursesArray) {
                    courses.add(course.asText());
                }
            }

            return DatingCourseDTO.builder()
                    .courses(courses)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("데이트 코스를 가져오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    //사용자 정보를 문자열로 변환하는 헬퍼 메서드
    private String buildUserInfoString(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("이름: ").append(user.getName()).append("\n");
        sb.append("성별: ").append(user.getGender()).append("\n");
        
        if (user.getUserProfile() != null) {
            var profile = user.getUserProfile();
            if (profile.getJob() != null) {
                sb.append("직업: ").append(profile.getJob()).append("\n");
            }
            if (profile.getMbti() != null) {
                sb.append("MBTI: ").append(profile.getMbti()).append("\n");
            }
            if (profile.getRegion() != null) {
                sb.append("지역: ").append(profile.getRegion()).append("\n");
            }
            if (profile.getPetPreference() != null) {
                sb.append("반려동물 선호도: ").append(profile.getPetPreference()).append("\n");
            }
            if (profile.getDrinkingFrequency() != null) {
                sb.append("음주 빈도: ").append(profile.getDrinkingFrequency()).append("\n");
            }
            if (profile.getSmokingStatus() != null) {
                sb.append("흡연 여부: ").append(profile.getSmokingStatus()).append("\n");
            }
            if (profile.getReligion() != null) {
                sb.append("종교: ").append(profile.getReligion()).append("\n");
            }
            if (profile.getIntroduction() != null && !profile.getIntroduction().isEmpty()) {
                sb.append("자기소개: ").append(profile.getIntroduction()).append("\n");
            }
        }
        
        return sb.toString();
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