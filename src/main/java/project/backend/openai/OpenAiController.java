package project.backend.openai;

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
public class OpenAiController {

    private final OpenAiService openAiService;
    private final UserRepository userRepository;

    @PostMapping
    public String getGptResponse(@RequestBody PromptRequest request) {
        return openAiService.getGptResponse(request.prompt());
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
