package project.backend.pythonapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchResponse(
        @JsonProperty("original_score")
        double originalScore,

        @JsonProperty("final_score")
        double finalScore,

        @JsonProperty("stress_score")
        double stressScore,

        @JsonProperty("person1_sal_analysis")
        String person1SalAnalysis,

        @JsonProperty("person2_sal_analysis")
        String person2SalAnalysis,

        @JsonProperty("match_analysis")
        String matchAnalysis,

        String error
) {
}