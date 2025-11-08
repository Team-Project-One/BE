package project.backend.pythonapi;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SajuRequest {

    private PersonInfo person1;

    private PersonInfo person2;
}
