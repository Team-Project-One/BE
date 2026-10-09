package project.backend.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.backend.user.entity.User;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_saju_info")
public class MatchSajuInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매칭을 요청한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 매칭된 상대방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_user_id")
    private User matchedUser;

    // SajuResponse 데이터 필드들
    private double originalScore;

    private double finalScore;

    private double stressScore;

    private String person1SalAnalysis;

    private String person2SalAnalysis;

    private String matchAnalysis;
}