package project.backend.matching;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.openai.OpenAiService;
import project.backend.pythonapi.PersonInfo;
import project.backend.pythonapi.SajuRequest;
import project.backend.pythonapi.SajuResponse;
import project.backend.pythonapi.SajuService;
import project.backend.user.UserRepository;
import project.backend.user.dto.UserEnums;
import project.backend.user.entity.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final SajuService sajuService;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;

    //전체 매칭하기 기능 반환
    public MatchingResultDTO getMatchingResult(Long userId) throws Exception {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();
        int userGender = 0;
        if (user.getGender() == UserEnums.Gender.MALE) {
            userGender = 1;
        }

        User randomUser = randomUser(user);
        int randomUserGender = 0;
        if (randomUser.getGender() == UserEnums.Gender.MALE) {
            randomUserGender = 1;
        }

        SajuRequest sajuRequest = new SajuRequest(
                new PersonInfo(
                        user.getBirthDate().getYear(),
                        user.getBirthDate().getMonthValue(),
                        user.getBirthDate().getDayOfMonth(),
                        userGender),
                new PersonInfo(
                        randomUser.getBirthDate().getYear(),
                        randomUser.getBirthDate().getMonthValue(),
                        randomUser.getBirthDate().getDayOfMonth(),
                        randomUserGender)
                );
        Mono<SajuResponse> sajuResponseMono = getSajuResponse(sajuRequest);
        SajuResponse sajuResponse = sajuResponseMono.block();

        MyPageDisplayDTO myPageDisplayDTO = new MyPageDisplayDTO(randomUser, randomUser.getUserProfile());

        return MatchingResultDTO.builder().sajuResponse(sajuResponse).personInfo(myPageDisplayDTO).build();
    }

    //랜덤으로 상대방 불러오기(지역 + 씹게이 판별)
    private User randomUser(User myUser) {
        UserEnums.region region = myUser.getUserProfile().getRegion();
        UserEnums.SexualOrientation sexualOrientation = myUser.getUserProfile().getSexualOrientation();
        UserEnums.Gender myGender = myUser.getGender();

        List<User> matchingUsers;

        // STRAIGHT인 경우 성별이 반대인 사용자들만 조회
        if (sexualOrientation == UserEnums.SexualOrientation.STRAIGHT) {
            matchingUsers = userRepository.findMatchingUsersForStraight(
                    region,
                    sexualOrientation,
                    myUser.getId(),
                    myGender
            );
        } else {
            // HOMOSEXUAL인 경우 같은 sexualOrientation을 가진 모든 사용자 조회
            matchingUsers = userRepository.findMatchingUsersByRegionAndOrientation(
                    region,
                    sexualOrientation,
                    myUser.getId()
            );
        }

        if (matchingUsers.isEmpty()) {
            throw new RuntimeException("매칭 가능한 사용자가 없습니다.");
        }

        // 랜덤으로 하나 선택
        Random random = new Random();
        int randomIndex = random.nextInt(matchingUsers.size());
        return matchingUsers.get(randomIndex);
    }

    //pythonApi 에서 궁합점수 반환
    private Mono<SajuResponse> getSajuResponse(SajuRequest sajuRequest) {
        return sajuService.getMatchResult(sajuRequest);
    }
}
