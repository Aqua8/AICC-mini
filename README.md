# AICC-mini
AICC 공부용

## Gemini 연동
- `GEMINI_API_KEY`가 있으면 Gemini API(`gemini-3.5-flash-lite`)로 상담 응답과 상담 종료 요약을 만든다. 없으면 스텁 응답이 나온다.
- 무료 키는 https://aistudio.google.com/apikey 에서 발급한다. 환경변수로만 넣고 커밋하지 않는다.
- 호출이 실패(한도 초과 등)하면 안내 문구로 대체해 상담이 끊기지 않는다.
- 실제 호출 검증 테스트: `GEMINI_API_KEY=... ./gradlew test --tests '*GeminiApiServiceLiveTest'` (키가 없으면 건너뜀)

## 대화 맥락과 비동기 응답
- AI 응답은 해당 상담방의 최근 메시지 20개를 `user`/`model` 역할로 Gemini에 보내 만든다. (후속 질문 가능)
- 응답 생성은 WebSocket 메시지 처리 스레드가 아니라 `AiReplyDispatcher`의 레인(기본 4개, `chat.ai-reply.lanes`)에서 한다. 같은 방은 같은 레인이라 응답 순서가 지켜진다.
- 상담 종료는 `ACTIVE → ENDED` 조건부 UPDATE로 선점한 뒤, 트랜잭션 밖에서 Gemini 요약을 만들고, 이력 저장만 짧은 트랜잭션으로 한다. 종료 버튼과 자동 종료가 겹쳐도 이력은 한 번만 생긴다.
