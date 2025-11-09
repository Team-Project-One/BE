package project.backend.openai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ConversationTopicDTO {

    private final List<String> topics;  // 대화주제 리스트 (3-5개)
}

