# AICC-mini
AICC 공부용

## 기술 스택

| 영역 | 기술 |
|---|---|
| Backend | Java 21, Spring Boot 4.0.6, Spring Data JPA, Spring Security, Spring WebSocket(STOMP), Spring Batch, JJWT 0.12.6, Lombok, Gradle 8.14.3 |
| AI | Gemini API (`gemini-3.5-flash-lite`, 무료 등급) |
| Frontend | React 18.3, React Router 6.30, Vite 5.4, Tailwind CSS 3.4, Recharts 2.15, Axios 1.18, STOMP.js 7.3, SockJS 1.6, Node.js 24 (로컬 개발) |
| Database | MariaDB (테스트는 H2) |
| 배포 | Docker (`eclipse-temurin:21`), Docker Compose |

## Gemini 연동
- `GEMINI_API_KEY`가 있으면 Gemini API(`gemini-3.5-flash-lite`)로 상담 응답과 상담 종료 요약을 만든다. 없으면 스텁 응답이 나온다.
- 무료 키는 https://aistudio.google.com/apikey 에서 발급한다. 환경변수로만 넣고 커밋하지 않는다.
- 호출이 실패(한도 초과 등)하면 안내 문구로 대체해 상담이 끊기지 않는다.
- 실제 호출 검증 테스트: `GEMINI_API_KEY=... ./gradlew test --tests '*GeminiApiServiceLiveTest'` (키가 없으면 건너뜀)

## 상담 세션 유지와 자동 저장
- 채팅 상태와 WebSocket 연결은 앱 레이아웃(`ChatProvider`)이 들고 있어, 상담 이력·대시보드에 다녀와도 진행 중인 상담이 유지된다. 새로고침하면 localStorage의 방 번호로 복원한다.
- "상담 종료" 버튼 없이 창을 닫아도 백엔드(`ChatSessionTracker`)가 연결이 모두 끊긴 뒤 60초 뒤에 상담을 종료하고 요약을 상담 이력에 저장한다. 그 안에 다시 접속하면 취소된다.
- 연결 없이 10분 넘게 비활성인 상담(서버 재시작 등)은 1분마다 점검해 종료한다. 고객 메시지가 없는 상담은 이력을 남기지 않는다.
- 설정: `chat.auto-end.grace-seconds`, `chat.auto-end.idle-minutes` (`application.yml`)

## 해결/미해결 판정과 수정
- 상담이 종료되면(버튼·자동 종료 모두) Gemini가 요약과 함께 해결 여부를 판정해 상담 이력의 상태(`RESOLVED`/`UNRESOLVED`)로 저장한다. 요약·판정에 실패하면 확인할 수 없으므로 미해결로 둔다.
- 상담 이력 화면의 상세 패널에서 "상담 결과"를 해결됨/미해결로 수정할 수 있다. (`PATCH /api/consultations/{id}/status`)

## 상담 주제: AI·LLM
- 상담 범위는 LLM 개념, 프롬프트 작성, API 활용, RAG, 파인튜닝, 에이전트·도구 사용, 비용·성능, AI 안전·윤리다. 범위 밖 질문은 안내 후 AI·LLM 질문으로 유도한다. 정책은 `GeminiApiService`의 시스템 프롬프트에 있다.
- 카테고리(`DefaultCategories`)는 카테고리 테이블이 비어 있으면 서버 시작 시 자동으로 들어간다. 관리자가 만든 카테고리가 있으면 건드리지 않는다.
- 상담 종료 시 Gemini가 현재 카테고리 목록(이름·설명) 중에서 상담 분류를 골라 저장한다.

## 대화 맥락과 비동기 응답
- AI 응답은 해당 상담방의 최근 메시지 20개를 `user`/`model` 역할로 Gemini에 보내 만든다. (후속 질문 가능)
- 응답 생성은 WebSocket 메시지 처리 스레드가 아니라 `AiReplyDispatcher`의 레인(기본 4개, `chat.ai-reply.lanes`)에서 한다. 같은 방은 같은 레인이라 응답 순서가 지켜진다.
- 상담 종료는 `ACTIVE → ENDED` 조건부 UPDATE로 선점한 뒤, 트랜잭션 밖에서 Gemini 요약을 만들고, 이력 저장만 짧은 트랜잭션으로 한다. 종료 버튼과 자동 종료가 겹쳐도 이력은 한 번만 생긴다.

## 권한
- 채팅방 조회·종료·메시지 전송은 방 주인만 가능하다. (ADMIN은 조회 가능)
- WebSocket은 STOMP CONNECT 때 JWT를 검증한다. 구독은 본인 상담방(`/topic/rooms/{id}`)만 허용하고, 토큰이 없거나 남의 방이면 연결이 거부된다.
- 상담 이력은 ADMIN이 전체, 일반 사용자는 본인 상담만 조회·상태 수정할 수 있다. 남의 이력은 403.
- ADMIN 만들기(로컬): `UPDATE users SET role = 'ADMIN' WHERE email = '...';` 후 다시 로그인

## 운영 설정과 데이터 일관성
- CORS는 `cors.allowed-origins`(환경변수 `CORS_ALLOWED_ORIGINS`, 쉼표 구분)에 적은 주소만 허용한다. REST와 WebSocket 핸드셰이크에 모두 적용되고, 기본값은 로컬 개발 주소다. 배포 시 실제 프런트 주소로 바꾼다. 인증은 Authorization 헤더(JWT)만 쓰므로 credentials는 허용하지 않는다.
- 상담 이력의 카테고리는 상세 패널에서 수정할 수 있다. (`PATCH /api/consultations/{id}/category`, 본인 상담 또는 ADMIN) 카테고리 목록 조회는 로그인한 누구나(`GET /api/categories`), 등록·수정·삭제는 ADMIN(`/api/admin/categories`)이다.
- 일별 통계는 새벽 1시 배치가 전체를 재집계하는 것과 별개로, 상담 이력이 생기거나 상태가 바뀌면 그 날짜만 바로 다시 집계한다. (`DailyStatsRefresher`)
- 채팅 화면은 WebSocket이 끊겼다 다시 연결되면 서버 기준으로 대화를 다시 불러와 놓친 AI 응답을 채운다. AI 응답이 60초 넘게 없으면 "입력 중" 표시를 푼다.
- 알려진 한계: 자동 종료 추적(`ChatSessionTracker`)은 서버 메모리 기반이라 서버를 여러 대로 늘리면 쓸 수 없다. 서버가 하나일 때 재시작은 10분 점검이 보완한다.
