package project.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import project.backend.fortune.dto.FortuneDTO;
import project.backend.openai.dto.ConversationTopicDTO;
import project.backend.openai.dto.DatingCourseDTO;
import project.backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String getGptResponse(String prompt) {
        try {

            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Spring AI / OpenAI 쪽에서 에러를 JSON 형태로 반환하는 경우 방어적 처리
            try {
                JsonNode node = objectMapper.readTree(content);
                if (node.has("status")) {
                    int status = node.get("status").asInt(0);
                    if (status < 0 || status != 200) {
                        log.error("OpenAI에서 에러 payload를 반환했습니다: {}", content);
                        return "죄송합니다. 지금은 AI 응답을 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
                    }
                }
            } catch (Exception ignore) {
                // content가 JSON 형식이 아닐 경우 그대로 사용
            }

            return content;
        } catch (Exception e) {
            log.error("OpenAI 응답 생성 중 오류가 발생했습니다. 기본 메시지를 반환합니다.", e);
            e.printStackTrace();
            return "죄송합니다. 지금은 AI 응답을 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        }
    }

    //오늘의 운세(4가지)
    public FortuneDTO getTodayFortune(String birthDate) {
        String prompt;
        
        if (birthDate != null && !birthDate.isEmpty()) {
            prompt = String.format("""
                    생년월일: %s
                    
                    위 생년월일을 바탕으로 사주팔자에 기반한 오늘의 운세를 다음 JSON 형식으로 반환해주세요.
                    각 운세는 한 문단으로 작성해주세요 (100자 이내).
                    
                    {
                        "overallFortune": "총운 설명",
                        "loveFortune": "애정운 설명",
                        "moneyFortune": "금전운 설명",
                        "careerFortune": "직장운 설명"
                    }
                    
                    반환 형식은 반드시 JSON만 반환하고, 다른 텍스트는 포함하지 마세요.
                    """, birthDate);
        } else {
            prompt = """
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
        }

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
            log.error("OpenAI 운세 생성 실패 - 기본 메시지를 반환합니다.", e);
            return getFallbackFortune();
        }
    }

    private FortuneDTO getFallbackFortune() {
        return FortuneDTO.builder()
                .overallFortune("오늘은 새로운 기회가 찾아올 수 있는 날입니다. 마음을 열고 주변을 살펴보세요.")
                .loveFortune("진심 어린 대화가 좋은 결과를 만들어냅니다. 소중한 사람에게 마음을 표현해 보세요.")
                .moneyFortune("지출을 점검하고 필요한 부분에 집중하면 재정적으로 안정감을 느낄 수 있습니다.")
                .careerFortune("지금까지의 노력이 결실을 맺기 시작합니다. 자신감을 가지고 도전해 보세요.")
                .build();
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
                - 구체적인 장소명을 반드시 포함해주세요 (예: "호수공원", "영화관", "마라탕")
                - **중요: 장소명에 지역명(예: 광교, 판교, 강남, 이태원, 삼청동 등)을 포함하지 마세요. 장소명만 작성해주세요.**
                - 지역에 맞는 실제 존재하는 장소를 추천해주되, 지역명은 생략하고 장소명만 작성해주세요
                - 활동 설명도 함께 작성해주세요
                
                다음 JSON 형식으로 반환해주세요:
                {
                    "courses": ["📍 호수공원 - 저녁 산책과 야경 감상", "🎬 영화관 - 영화 관람 후 카페 투어", "🍽️ 마라탕 - 다양한 음식 체험", "🎨 갤러리 투어 - 예술적인 데이트", "🌳 타워 - 야경 감상"]
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
                    String courseText = course.asText();
                    // 지역명 제거 후처리
                    courseText = removeRegionNames(courseText);
                    courses.add(courseText);
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
    public String buildUserInfoString(User user) {
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

    // 지역명 제거 헬퍼 메서드
    private String removeRegionNames(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 주요 지역명 목록 (공백 포함 패턴)
        String[] regionNames = {
            "광교", "판교", "강남", "강북", "송파", "서초", "분당", "수원", "성남",
            "용인", "이천", "안양", "안산", "부천", "광명", "평택", "오산", "시흥",
            "군포", "의왕", "하남", "구리", "남양주", "의정부", "고양", "파주",
            "김포", "인천", "서울", "부산", "대구", "광주", "대전", "울산",
            "세종", "수원시", "성남시", "용인시", "이천시", "안양시", "안산시",
            "부천시", "광명시", "평택시", "오산시", "시흥시", "군포시", "의왕시",
            "하남시", "구리시", "남양주시", "의정부시", "고양시", "파주시", "김포시",
            "이태원", "삼청동", "홍대", "강남구", "강북구", "송파구", "서초구",
            "광교호수공원", "판교영화관", "판교마라탕"
        };
        
        String result = text;
        for (String region : regionNames) {
            // 지역명 뒤에 공백이 오는 경우 제거 (예: "광교 호수공원" -> "호수공원")
            result = result.replace(region + " ", "");
            // 지역명이 단독으로 있는 경우 제거 (예: "광교" -> "")
            result = result.replace(region, "");
            // 지역명 앞에 공백이 오는 경우 제거 (예: " 판교" -> "")
            result = result.replace(" " + region, "");
        }
        
        // 연속된 공백 제거 및 앞뒤 공백 제거
        result = result.replaceAll("\\s+", " ").trim();
        
        return result;
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