package project.backend.openai;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.backend.openai.dto.ConversationTopicDTO;
import project.backend.openai.dto.DatingCourseDTO;
import project.backend.openai.dto.PromptRequest;
import project.backend.openai.dto.RecommendationRequest;
import project.backend.user.UserRepository;
import project.backend.user.entity.User;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Tag(name = "spring-ai-controller", description = "프런트에서 사용할 필요 X")
public class OpenAiController {

    private final OpenAiService openAiService;
    private final UserRepository userRepository;

    @PostMapping
    public String getGptResponse(@RequestBody PromptRequest request) {

        String finalPrompt = request.prompt();

        // myUserId, matchedUserId가 함께 넘어온 경우 두 사람의 기본/상세 정보를 프롬프트에 포함
        if (request.myUserId() != null && request.matchedUserId() != null) {
            Long myUserId = request.myUserId();
            Long matchedUserId = request.matchedUserId();

            try {
                Optional<User> myUserOptional = userRepository.findByIdWithProfile(myUserId);
                Optional<User> matchedUserOptional = userRepository.findByIdWithProfile(matchedUserId);

                if (myUserOptional.isPresent() && matchedUserOptional.isPresent()) {
                    User myUser = myUserOptional.get();
                    User matchedUser = matchedUserOptional.get();

                    String myInfo = openAiService.buildUserInfoString(myUser);
                    String matchedInfo = openAiService.buildUserInfoString(matchedUser);

                    finalPrompt = String.format("""
                            너는 연인/소개팅 대화를 도와주는 한국어 챗봇이야.
                            
                            아래 [내 정보], [상대방 정보]는 두 사람이 사전에 입력한 프로필이야.
                            여기에 적힌 내용(직업, 거주지, MBTI, 반려동물, 음주/흡연, 종교 등)은 모두 사실이라고 가정해.
                            
                            [내 정보]
                            %s
                            
                            [상대방 정보]
                            %s
                            
                            [사용자의 발화]
                            %s
                            
                            요구사항:
                            1. 사용자의 발화에 자연스럽게 이어지는 답장을 한글로 1~3문장 작성해라.
                            2. 사용자가 상대방의 정보(직업, 거주지, 반려동물, MBTI, 음주/흡연, 종교 등)를 물어보면,
                               - 해당 정보가 [상대방 정보]에 있으면, 그 값을 기반으로 직접 알려줘라.
                                 (예: 프로필에 반려동물이 고양이라고 되어 있으면 "고양이를 키우시는 걸로 알고 있어요"처럼 답해라)
                               - 없으면 "그 부분은 아직 프로필에 없어서 정확히는 모르겠어요"처럼 솔직하게 모른다고 말해라.
                               - 정보가 너무 포괄적이면(예: 직업이 EMPLOYEE로만 적혀 있음) "직장인으로 알고 있어요" 정도까지만 말하고,
                                 구체적인 회사 이름이나 직무 분야는 지어내지 마라.
                            3. 두 사람의 공통점이 있을 때(둘 다 고양이를 키움, 둘 다 비흡연자 등) 필요하면 자연스럽게 언급해라.
                            4. 1인칭 대명사("저", "나", "우리")를 절대 사용하지 마라.
                               항상 "테스트유저2님은...", "상대방은..."처럼 2·3인칭으로만 표현해라.
                            5. AI 자신(너)의 MBTI, 취향, 반려동물, 개인 경험 이야기는 하지 마라.
                            6. 최종 출력에는 위 요구사항에 맞는 실제 대화 문장만 포함하고, 추가 설명이나 메타 텍스트는 넣지 마라.
                            """, myInfo, matchedInfo, request.prompt());
                }
            } catch (Exception e) {
                // 사용자 정보 조회에 실패하면 그냥 원래 프롬프트만 사용
            }
        }

        String result = openAiService.getGptResponse(finalPrompt);
        return result;
    }

    //대화주제 추천
    @PostMapping("/conversation-topics")
    public ResponseEntity<ConversationTopicDTO> getConversationTopics(
            @RequestBody RecommendationRequest request) throws Exception {
        
        Long myUserId = request.myUserId();
        Long matchedUserId = request.matchedUserId();
        
        Optional<User> myUserOptional = userRepository.findByIdWithProfile(myUserId);
        if (myUserOptional.isEmpty()) {
            throw new Exception("내 사용자 정보를 찾을 수 없습니다. userId: " + myUserId);
        }
        
        Optional<User> matchedUserOptional = userRepository.findByIdWithProfile(matchedUserId);
        if (matchedUserOptional.isEmpty()) {
            throw new Exception("매칭된 사용자 정보를 찾을 수 없습니다. userId: " + matchedUserId);
        }
        
        User myUser = myUserOptional.get();
        User matchedUser = matchedUserOptional.get();
        
        // UserProfile이 없는 경우 예외 처리
        if (myUser.getUserProfile() == null) {
            throw new Exception("내 사용자 프로필 정보가 없습니다. userId: " + myUserId);
        }
        if (matchedUser.getUserProfile() == null) {
            throw new Exception("매칭된 사용자 프로필 정보가 없습니다. userId: " + matchedUserId);
        }
        
        ConversationTopicDTO conversationTopics = openAiService.getConversationTopics(myUser, matchedUser);
        return ResponseEntity.ok(conversationTopics);
    }

    //데이트 코스 추천
    @PostMapping("/dating-courses")
    public ResponseEntity<DatingCourseDTO> getDatingCourses(
            @RequestBody RecommendationRequest request) throws Exception {
        
        Long myUserId = request.myUserId();
        Long matchedUserId = request.matchedUserId();
        
        Optional<User> myUserOptional = userRepository.findByIdWithProfile(myUserId);
        if (myUserOptional.isEmpty()) {
            throw new Exception("내 사용자 정보를 찾을 수 없습니다. userId: " + myUserId);
        }
        
        Optional<User> matchedUserOptional = userRepository.findByIdWithProfile(matchedUserId);
        if (matchedUserOptional.isEmpty()) {
            throw new Exception("매칭된 사용자 정보를 찾을 수 없습니다. userId: " + matchedUserId);
        }
        
        User myUser = myUserOptional.get();
        User matchedUser = matchedUserOptional.get();
        
        // UserProfile이 없는 경우 예외 처리
        if (myUser.getUserProfile() == null) {
            throw new Exception("내 사용자 프로필 정보가 없습니다. userId: " + myUserId);
        }
        if (matchedUser.getUserProfile() == null) {
            throw new Exception("매칭된 사용자 프로필 정보가 없습니다. userId: " + matchedUserId);
        }
        
        DatingCourseDTO datingCourses = openAiService.getDatingCourseRecommendation(myUser, matchedUser);
        return ResponseEntity.ok(datingCourses);
    }
}
