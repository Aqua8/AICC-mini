# AICC-mini
AICC 공부용

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

## 상담 주제: AI·LLM
- 상담 범위는 LLM 개념, 프롬프트 작성, API 활용, RAG, 파인튜닝, 에이전트·도구 사용, 비용·성능, AI 안전·윤리다. 범위 밖 질문은 안내 후 AI·LLM 질문으로 유도한다. 정책은 `GeminiApiService`의 시스템 프롬프트에 있다.
- 카테고리(`DefaultCategories`)는 카테고리 테이블이 비어 있으면 서버 시작 시 자동으로 들어간다. 관리자가 만든 카테고리가 있으면 건드리지 않는다.
- 상담 종료 시 Gemini가 현재 카테고리 목록(이름·설명) 중에서 상담 분류를 골라 저장한다.

## 대화 맥락과 비동기 응답
- AI 응답은 해당 상담방의 최근 메시지 20개를 `user`/`model` 역할로 Gemini에 보내 만든다. (후속 질문 가능)
- 응답 생성은 WebSocket 메시지 처리 스레드가 아니라 `AiReplyDispatcher`의 레인(기본 4개, `chat.ai-reply.lanes`)에서 한다. 같은 방은 같은 레인이라 응답 순서가 지켜진다.
- 상담 종료는 `ACTIVE → ENDED` 조건부 UPDATE로 선점한 뒤, 트랜잭션 밖에서 Gemini 요약을 만들고, 이력 저장만 짧은 트랜잭션으로 한다. 종료 버튼과 자동 종료가 겹쳐도 이력은 한 번만 생긴다.
