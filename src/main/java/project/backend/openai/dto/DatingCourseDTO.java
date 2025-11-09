package project.backend.openai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DatingCourseDTO {

    private final List<String> courses;  // 데이트 코스 리스트 (3-5개)
}

