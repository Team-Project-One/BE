package project.backend.matching;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.matching.dto.MatchingResultDTO;
import project.backend.matching.entity.MatchSajuInfo;
import project.backend.mypage.dto.MyPageDisplayDTO;
import project.backend.openai.OpenAiService;
import project.backend.pythonapi.dto.PersonInfo;
import project.backend.pythonapi.dto.SajuRequest;
import project.backend.pythonapi.dto.SajuResponse;
import project.backend.pythonapi.SajuService;
import project.backend.user.UserRepository;
import project.backend.user.dto.UserEnums;
import project.backend.user.entity.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final SajuService sajuService;
    private final UserRepository userRepository;
    private final MatchSajuInfoRepository matchSajuInfoRepository;

    //전체 매칭하기 기능 반환
    public MatchingResultDTO getMatchingResult(Long userId, Long excludeUserId) throws Exception {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new Exception("User not found");
        }
        User user = userOptional.get();
        int userGender = 0;
        if (user.getGender() == UserEnums.Gender.MALE) {
            userGender = 1;
        }

        User randomUser = randomUser(user, excludeUserId);
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

        MatchSajuInfo matchInfo = MatchSajuInfo.builder()
                .user(user)
                .matchedUser(randomUser)
                .originalScore(sajuResponse.originalScore())
                .finalScore(sajuResponse.finalScore())
                .stressScore(sajuResponse.stressScore())
                .person1SalAnalysis(sajuResponse.person1SalAnalysis())
                .person2SalAnalysis(sajuResponse.person2SalAnalysis())
                .matchAnalysis(sajuResponse.matchAnalysis())
                .build();

        matchSajuInfoRepository.save(matchInfo);

        MyPageDisplayDTO myPageDisplayDTO = new MyPageDisplayDTO(randomUser, randomUser.getUserProfile());

        return MatchingResultDTO.builder().sajuResponse(sajuResponse).personInfo(myPageDisplayDTO).build();
    }

    //특정 상대방과의 매칭 결과 반환
    public MatchingResultDTO getMatchingResultWithMatchedUser(Long myUserId, Long matchedUserId) throws Exception {
        Optional<User> myUserOptional = userRepository.findById(myUserId);
        if (myUserOptional.isEmpty()) {
            throw new Exception("내 사용자 정보를 찾을 수 없습니다. userId: " + myUserId);
        }
        User myUser = myUserOptional.get();

        Optional<User> matchedUserOptional = userRepository.findById(matchedUserId);
        if (matchedUserOptional.isEmpty()) {
            throw new Exception("매칭된 사용자 정보를 찾을 수 없습니다. userId: " + matchedUserId);
        }
        User matchedUser = matchedUserOptional.get();

        // 항상 같은 순서로 정렬하기 위해 userId가 작은 쪽을 person1, 큰 쪽을 person2로 설정
        User person1User, person2User;
        boolean isMyUserPerson1;
        if (myUserId <= matchedUserId) {
            person1User = myUser;
            person2User = matchedUser;
            isMyUserPerson1 = true;
        } else {
            person1User = matchedUser;
            person2User = myUser;
            isMyUserPerson1 = false;
        }

        int person1Gender = 0;
        if (person1User.getGender() == UserEnums.Gender.MALE) {
            person1Gender = 1;
        }

        int person2Gender = 0;
        if (person2User.getGender() == UserEnums.Gender.MALE) {
            person2Gender = 1;
        }

        SajuRequest sajuRequest = new SajuRequest(
                new PersonInfo(
                        person1User.getBirthDate().getYear(),
                        person1User.getBirthDate().getMonthValue(),
                        person1User.getBirthDate().getDayOfMonth(),
                        person1Gender),
                new PersonInfo(
                        person2User.getBirthDate().getYear(),
                        person2User.getBirthDate().getMonthValue(),
                        person2User.getBirthDate().getDayOfMonth(),
                        person2Gender)
        );
        Mono<SajuResponse> sajuResponseMono = getSajuResponse(sajuRequest);
        SajuResponse sajuResponse = sajuResponseMono.block();

        // 응답의 personInfo는 항상 상대방(matchedUser) 정보를 반환
        MyPageDisplayDTO myPageDisplayDTO = new MyPageDisplayDTO(matchedUser, matchedUser.getUserProfile());

        return MatchingResultDTO.builder().sajuResponse(sajuResponse).personInfo(myPageDisplayDTO).build();
    }

    //랜덤으로 상대방 불러오기(지역 + 씹게이 판별)
    private User randomUser(User myUser, Long excludeUserId) {
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

        // excludeUserId가 있으면 해당 사용자 제외
        if (excludeUserId != null) {
            matchingUsers = matchingUsers.stream()
                    .filter(u -> !u.getId().equals(excludeUserId))
                    .collect(Collectors.toList());
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
