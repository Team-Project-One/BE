package project.backend.matching;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.backend.matching.dto.MatchingResultDTO;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/{userId}")
    public MatchingResultDTO getMatchingResult(@PathVariable("userId") Long userId) throws Exception {
        return matchingService.getMatchingResult(userId);
    }
}
