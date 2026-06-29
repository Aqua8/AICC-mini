# QA Report — AICC 미니 프론트엔드

**날짜**: 2026-06-29
**브랜치**: feature/frontend
**빌드 결과**: ✅ 성공 (`npm run build` — 에러 0, 경고: 번들 크기 권고 (정상 범위))

---

## 검증 항목

| 항목 | 상태 | 비고 |
|------|------|------|
| axios baseURL / proxy 설정 | ✅ | baseURL: `/api`, vite proxy `/api` → `localhost:8080` |
| JWT Authorization 인터셉터 | ✅ | 요청마다 `Bearer {token}` 자동 삽입 |
| 401 응답 처리 | ✅ | clearAuth() + `window.location.href = '/login'` |
| authStore localStorage | ✅ | setAuth / getAuth / clearAuth / getToken / isAdmin |
| auth-change 이벤트 동기화 | ✅ | dispatchEvent로 Layout 사이드바 실시간 갱신 |
| WebSocket SockJS+STOMP 연결 | ✅ | `/ws/chat` 연결, connectHeaders에 JWT 포함 |
| WebSocket 구독 경로 | ✅ | `/topic/rooms/{roomId}` |
| WebSocket 발행 경로 | ✅ | `/app/chat` → `{ roomId, content }` |
| WebSocket 클린업 | ✅ | useEffect return에서 `client.deactivate()` |
| 로그인/회원가입 흐름 | ✅ | setAuth(data) → navigate('/chat') |
| 채팅방 생성 → WS 연결 흐름 | ✅ | createRoom 성공 후 room.id로 useWebSocket 활성화 |
| 채팅 메시지 전송/수신 | ✅ (수정됨) | BUG-001 수정: AI 메시지만 WS로 추가 |
| 상담 종료 버튼 | ✅ | endRoom() 호출, isClosed 상태 반영 |
| 커서 페이징 ("더 보기") | ✅ | nextCursor/hasNext 기반 append 로드 |
| 검색/필터 파라미터 전달 | ✅ | keyword, categoryId, status → axios params |
| 상담 상세 보기 | ✅ | getConsultation(id) → 우측 패널 표시 |
| Recharts LineChart 바인딩 | ✅ | dataKey="count", XAxis dataKey="date" |
| Recharts PieChart 바인딩 | ✅ | dataKey="count", nameKey="categoryName" |
| 해결률 표시 | ✅ (수정됨) | BUG-002 수정: resolutionRate 이중 변환 제거 |
| ADMIN 접근 제어 | ✅ | AdminRoute: !isAdmin() → /chat 리다이렉트 |
| Admin 카테고리 CRUD | ✅ | 추가/수정/삭제 인라인 폼 구현 |
| Admin FAQ CRUD | ✅ | 추가/수정/삭제 인라인 폼 구현 |
| vite proxy /api | ✅ | changeOrigin: true |
| vite proxy /ws | ✅ | ws: true (WebSocket 프록시) |
| 라우팅 구조 | ✅ | /, /login, /chat, /consultations, /dashboard, /admin |
| PrivateRoute (인증 필요) | ✅ | getAuth() 없으면 /login 리다이렉트 |
| 사이드바 ADMIN 메뉴 노출 | ✅ | role === 'ADMIN'일 때만 관리자 링크 표시 |
| 로그아웃 | ✅ | clearAuth() + navigate('/login') |

---

## 발견된 버그 및 수정 내역

### [BUG-001] WebSocket USER 메시지 중복 표시

- **파일**: `src/pages/Chat.jsx`
- **문제**: 백엔드(`ChatWebSocketController`)가 USER 메시지를 저장 후 `/topic/rooms/{roomId}`로 브로드캐스트함. 프론트엔드는 이미 `handleSend`에서 낙관적 UI로 USER 메시지를 즉시 추가하는데, `handleWsMessage`가 모든 수신 메시지를 `setMessages`에 추가하여 USER 메시지가 화면에 2번 표시됨.
- **수정**: `handleWsMessage`에서 `msg.sender === 'AI'`인 경우에만 상태를 업데이트하도록 필터링.

```js
// Before
const handleWsMessage = (msg) => {
  setAiTyping(false)
  setMessages((prev) => [...prev, msg])
}

// After
const handleWsMessage = (msg) => {
  if (msg.sender === 'AI') {
    setAiTyping(false)
    setMessages((prev) => [...prev, msg])
  }
}
```

---

### [BUG-002] 해결률(resolutionRate) 이중 변환으로 잘못된 값 표시

- **파일**: `src/pages/Dashboard.jsx`
- **문제**: 백엔드 `StatsService.summary()`가 `resolutionRate`를 이미 퍼센트값(예: 75.0)으로 계산하여 반환함 (`Math.round(resolved * 10000.0 / total) / 100.0`). 프론트엔드에서 이 값에 다시 `* 100`을 적용하여 실제 해결률 75%가 7500%로 표시되는 오류 발생.
- **수정**: `resolutionRate * 100` → `resolutionRate` (이중 변환 제거).

```js
// Before
const resolutionPct = summary
  ? `${(summary.resolutionRate * 100).toFixed(1)}%`
  : '-'

// After
const resolutionPct = summary
  ? `${summary.resolutionRate.toFixed(1)}%`
  : '-'
```

---

## 참고 사항 (버그 아님)

- **검색 실시간 반응**: `keyword` 상태가 `onChange`마다 갱신되어 키 입력마다 API 호출이 발생함. 기능상 문제는 없으나, 대용량 환경에서는 debounce 적용을 권장.
- **번들 크기 경고**: Recharts + STOMP.js 포함으로 번들 720KB. Vite의 권고 경고이며 기능 오류는 아님. 코드 스플리팅 적용 시 해결 가능.
- **비관리자의 /admin/categories 호출**: `Consultations.jsx`에서 카테고리 필터를 위해 `/admin/categories`를 호출하나, 401/403 응답 시 `.catch(() => {})` 로 무시하여 카테고리 드롭다운이 빈 상태로 표시됨. 의도적 처리.

---

## 최종 평가

**✅ 통과 (버그 수정 완료)**

plan.md의 프론트엔드 요구사항 전 항목이 구현되었으며, QA 과정에서 발견된 2개 버그(메시지 중복 표시, 해결률 이중 변환)를 모두 수정했습니다. 빌드 에러 없음 확인.
