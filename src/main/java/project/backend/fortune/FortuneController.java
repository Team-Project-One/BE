package project.backend.fortune;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.backend.fortune.dto.FortuneDTO;

@RestController
@RequestMapping("/fortune")
@RequiredArgsConstructor
@Tag(name = "오늘의 운세",description = "오늘의 운세 기능(포춘쿠키)")
public class FortuneController {

    private final FortuneService fortuneService;

    @GetMapping
    public ResponseEntity<FortuneDTO> getTodayFortune() {
        FortuneDTO fortune = fortuneService.getTodayFortune();
        return ResponseEntity.ok(fortune);
    }
}

