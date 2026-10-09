# Divine (디바인) - Backend

디바인은 사주 궁합으로 상대를 추천하고, 매칭된 사용자끼리 실시간으로 대화할 수 있는 데이팅 앱입니다. 이 저장소는 디바인의 백엔드 서버 코드입니다.

- 기간: 2025.09 ~ 2025.12
- 프론트엔드: [Team-Project-One/FE](https://github.com/Team-Project-One/FE)

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Security, Spring Data JPA |
| Database | MySQL |
| 인증 | Kakao OAuth 2.0, JWT |
| 실시간 통신 | WebSocket (STOMP, SockJS) |
| 외부 연동 | Spring AI (OpenAI), 사주 계산 Python 서버 (WebClient) |
| 문서화 | Swagger (springdoc-openapi) |

## 시스템 구성

```
┌────────────────┐   REST / STOMP   ┌──────────────────────┐
│  React Native  │ ───────────────▶ │  Spring Boot Server  │
│  App (FE)      │ ◀─────────────── │                      │
└────────────────┘                  └──────────┬───────────┘
                                               │
              ┌──────────────┬─────────────────┼────────────────┐
              ▼              ▼                 ▼                ▼
          ┌───────┐   ┌────────────┐   ┌──────────────┐   ┌──────────┐
          │ MySQL │   │ Kakao OAuth│   │ Python 사주  │   │  OpenAI  │
          └───────┘   └────────────┘   │ 계산 서버    │   └──────────┘
                                       └──────────────┘
```

## 주요 기능

- 카카오 로그인: 웹 콜백(딥링크로 앱에 토큰 전달)과 앱 전용 로그인(카카오 액세스 토큰 검증) 지원, JWT 발급
- 회원가입과 프로필: 생년월일, 성별, 지역, 자기소개 등 입력, 프로필 이미지 업로드와 삭제
- 사주 궁합 매칭: Python 사주 서버로 궁합 점수를 계산해 상대 추천, 특정 사용자 제외 및 상대 지정 조회
- 실시간 채팅: STOMP 기반 1:1 채팅, 입력 중 상태 전달, 채팅방 안에서 궁합 정보 조회
- AI 추천: 사용자 정보를 반영한 대화 주제, 데이트 코스 추천
- 오늘의 운세: 생년월일 기반 운세, 날짜와 생년월일 단위로 캐시

## API

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/auth/kakao/callback` | 카카오 웹 로그인 콜백 (앱 딥링크로 토큰 전달) |
| POST | `/auth/kakao/mobile` | 앱에서 받은 카카오 액세스 토큰으로 로그인 |
| POST | `/auth/test-login` | 시연용 테스트 로그인 (아래 주의 사항 참고) |
| POST | `/users/signup` | 회원가입 (multipart) |
| GET | `/users/status/{kakaoId}` | 가입 여부 등 회원 상태 조회 |
| GET | `/my-page/{userId}` | 마이페이지 조회 |
| PATCH | `/my-page/{userId}/profile` | 프로필 수정 |
| PATCH | `/my-page/{userId}/profile-image` | 프로필 이미지 변경 |
| DELETE | `/my-page/{userId}/profile-image` | 프로필 이미지 삭제 |
| DELETE | `/my-page/{userId}` | 회원 탈퇴 |
| GET | `/matching/{userId}` | 궁합 기반 매칭 |
| GET | `/matching/{myUserId}/{matchedUserId}` | 지정한 상대와의 매칭 결과 조회 |
| POST | `/api/match` | 사주 궁합 계산 |
| POST | `/api/chat/room` | 1:1 채팅방 생성 또는 조회 |
| GET | `/api/chat/rooms` | 내 채팅방 목록 |
| GET | `/api/chat/room/{roomId}/messages` | 채팅 메시지 목록 |
| GET | `/api/chat/room/{roomId}/saju` | 채팅 상대와의 궁합 정보 |
| DELETE | `/api/chat/room/{roomId}` | 채팅방 나가기 (대화 내역 삭제) |
| POST | `/ai/conversation-topics` | 대화 주제 추천 |
| POST | `/ai/dating-courses` | 데이트 코스 추천 |
| GET | `/fortune` | 오늘의 운세 |

### WebSocket (STOMP)

| 구분 | 경로 | 설명 |
|---|---|---|
| 연결 | `/ws/chat` | SockJS 지원, 연결 시 JWT 필요 |
| 전송 | `/app/chat/message` | 메시지 전송 |
| 전송 | `/app/chat/typing` | 입력 중 상태 전송 |
| 구독 | `/user/queue/chat` | 메시지 수신 |
| 구독 | `/user/queue/chat-typing` | 상대의 입력 중 상태 수신 |
| 구독 | `/user/queue/chat-room-event` | 채팅방 변경 알림 수신 |

전체 명세는 서버 실행 후 `/swagger-ui/index.html`에서 확인할 수 있습니다.

## 실행 방법

### 요구 사항

- Java 21
- MySQL 8
- 사주 계산 Python 서버 (`api.python.baseUrl`로 연결)

### 설정

`src/main/resources/application.properties`를 만들고 아래 항목을 채웁니다. 이 파일은 `.gitignore`에 포함되어 있습니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/{DB_NAME}
spring.datasource.username={DB_USER}
spring.datasource.password={DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

file.upload-dir=uploads

jwt.issuer={ISSUER}
jwt.secret-key={JWT_SECRET}

spring.security.oauth2.client.registration.kakao.client-id={KAKAO_REST_API_KEY}
spring.security.oauth2.client.registration.kakao.redirect-uri={REDIRECT_URI}

spring.ai.openai.api-key={OPENAI_API_KEY}
api.python.baseUrl={PYTHON_SAJU_SERVER_URL}
```

### 실행

```bash
./gradlew bootRun
```

## 주의 사항

이 저장소는 최종 시연 시점의 코드입니다. 시연 편의를 위해 아래 설정이 들어가 있으며, 실제 운영 시에는 제거하거나 수정해야 합니다.

- `/auth/test-login`: 카카오 로그인 없이 테스트 계정 토큰을 발급합니다
- `WebSecurityConfig`: 마이페이지, 매칭 등 일부 API가 인증 없이 열려 있습니다. 운영 시에는 인증된 사용자 본인의 데이터만 접근하도록 제한해야 합니다
