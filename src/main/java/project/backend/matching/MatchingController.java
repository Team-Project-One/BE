package project.backend.matching;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.backend.matching.dto.MatchingResultDTO;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@Tag(name = "매칭하기" , description = "매칭하기 기능(자신의 id(유저 id)만 넣으면 됨)")
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/{userId}")
    public MatchingResultDTO getMatchingResult(@PathVariable("userId") Long userId) throws Exception {
        return matchingService.getMatchingResult(userId);
    }
}
