# AICC 미니 시스템 기획 및 계획

## 프로젝트 개요
- 이름: AICC 미니 시스템 (AI Contact Center)
- 목적: 취업용 풀스택 개발자 포트폴리오
- 설명: Gemini AI 기반 챗봇 상담 + 상담 이력 관리 + 통계 대시보드
- 특징: 100만건 대용량 데이터 처리 + 실시간 WebSocket 채팅

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 프론트엔드 | React.js, Tailwind CSS, Recharts |
| 실시간 채팅 | WebSocket (Spring + STOMP) |
| 백엔드 | Spring Boot 4.0.6, Spring Data JPA, Spring Security (Java 21) |
| AI 연동 | Gemini API (gemini-1.5-flash) |
| 인증 | JWT |
| 데이터베이스 | MariaDB |
| 대용량 처리 | Spring Batch (가짜 데이터 bulk insert) |
| 배포 | Google Cloud VM, Docker |
| 트래킹 | 웹 서비스 통계 대시보드 플랫폼 스크립트 연동 |

---

## 시스템 구성

### 1. 고객 채팅 화면
- Gemini AI와 실시간 채팅 상담
- WebSocket 기반 실시간 메시지 송수신
- 상담 종료 시 AI 자동 요약

### 2. 상담 이력 관리
- 전체 상담 목록 조회
- 키워드 검색 / 카테고리 필터 / 처리상태 필터
- 커서 기반 페이징 (대용량 최적화)
- 상담 상세 보기 (대화 내용 + 요약)

### 3. 통계 대시보드
- 일별/주별/월별 상담 건수 추이 (Recharts)
- 카테고리별 상담 분포 (파이차트)
- 처리 상태별 현황 (해결/미해결/진행중)
- 평균 응답시간, 해결률 지표

### 4. 관리자 페이지
- 상담 카테고리 관리
- FAQ 등록/수정/삭제

---

## DB 설계

### 테이블 목록

```sql
users            - 사용자 (고객 + 관리자)
chat_rooms       - 상담 채팅방
chat_messages    - 채팅 메시지 (대용량 핵심 테이블)
consultation     - 상담 이력 (요약, 카테고리, 처리상태)
categories       - 상담 카테고리 (배송/환불/기술지원 등)
faq              - 자주 묻는 질문
daily_stats      - 일별 상담 통계 (Spring Batch 생성)
```

### 테이블 관계

```
users (1) ──── (N) chat_rooms (1) ──── (N) chat_messages
                        │
                        └──── (1) consultation
                                      │
                                      └──── (N) categories

daily_stats ← Spring Batch 집계
```

### 인덱스 전략 (chat_messages - 대용량)

```sql
INDEX idx_chat_room_id (chat_room_id)
INDEX idx_created_at (created_at)
INDEX idx_room_created (chat_room_id, created_at)  -- 복합 인덱스
```

---

## AI 연동 흐름

> **개발 환경**: 로컬 개발 시 Gemini 연동 제외. Google Cloud VM 배포 후 연동 진행.

```
고객 메시지 입력
        ↓
Spring Boot → Gemini API 호출 (gemini-1.5-flash)
        ↓
AI 답변 반환 → WebSocket으로 실시간 전송
        ↓
chat_messages에 저장 (bulk insert)
        ↓
상담 종료 시 Gemini로 대화 내용 자동 요약
        ↓
consultation 테이블에 요약 저장
```

---

## 대용량 데이터 처리 전략

```
Spring Batch로 가짜 상담 데이터 100만건 생성
        ↓
chat_messages 테이블에 bulk insert
        ↓
복합 인덱스 + 커서 기반 페이징으로 빠른 조회
        ↓
Spring Batch 스케줄러로 일별 통계 집계
        ↓
daily_stats 테이블에 저장 → 대시보드 시각화
```

### 성능 최적화 포인트
- bulk insert로 100만건 빠르게 적재
- 복합 인덱스로 쿼리 튜닝
- 커서 기반 페이징 (offset 대신 cursor 방식)
- Redis 캐싱으로 통계 쿼리 속도 개선 (선택)

---

## API 설계

### 인증
- POST /api/auth/signup   - 회원가입
- POST /api/auth/login    - 로그인

### 채팅
- POST /api/chat/rooms           - 채팅방 생성 (상담 시작)
- GET  /api/chat/rooms/{id}      - 채팅방 상세
- POST /api/chat/rooms/{id}/end  - 상담 종료 (AI 요약 트리거)
- WS   /ws/chat                  - WebSocket 연결

### 상담 이력
- GET /api/consultations         - 상담 이력 목록 (검색/필터/페이징)
- GET /api/consultations/{id}    - 상담 이력 상세

### 통계
- GET /api/stats/daily           - 일별 통계
- GET /api/stats/category        - 카테고리별 통계
- GET /api/stats/summary         - 전체 요약 지표

### 관리자
- GET/POST/PUT/DELETE /api/admin/categories  - 카테고리 관리
- GET/POST/PUT/DELETE /api/admin/faq         - FAQ 관리

### 배치
- POST /api/admin/batch/generate  - 가짜 데이터 생성 트리거
- POST /api/admin/batch/stats     - 통계 집계 트리거

---

## 패키지 구조

### Backend
```
com.aicc.backend
├── controller
├── service
├── repository
├── domain
├── dto
├── config        # Security, JWT, WebSocket, Gemini 설정
├── batch         # Spring Batch Job/Step (데이터 생성, 통계 집계)
├── websocket     # WebSocket 핸들러
└── util
```

### Frontend
```
aicc-frontend/
├── public/
└── src/
    ├── components/
    │   ├── ChatWindow.jsx       # 채팅 화면
    │   ├── MessageBubble.jsx    # 메시지 말풍선
    │   ├── ConsultationList.jsx # 상담 이력 목록
    │   └── StatsChart.jsx       # 통계 차트
    ├── pages/
    │   ├── Chat.jsx             # 고객 채팅 페이지
    │   ├── Consultations.jsx    # 상담 이력 페이지
    │   ├── Dashboard.jsx        # 통계 대시보드
    │   └── Admin.jsx            # 관리자 페이지
    ├── hooks/
    │   └── useWebSocket.js      # WebSocket 훅
    ├── api/
    ├── store/
    ├── App.jsx
    └── main.jsx
```

---

## 개발 일정 (6주 기준)

| 주차 | 내용 |
|------|------|
| 1주차 | DB 설계, Spring Boot 세팅, JWT 인증 구현 |
| 2주차 | Gemini API 연동, 채팅 API 구현 |
| 3주차 | WebSocket 실시간 채팅 구현 |
| 4주차 | 상담 이력 관리, Spring Batch 100만건 데이터 생성 |
| 5주차 | React UI 구현 (채팅/이력/대시보드/관리자) |
| 6주차 | 성능 최적화, 인덱스 튜닝, Google Cloud VM 배포 |

---

## 진행 현황

- [ ] DB 생성 및 테이블 설계
- [ ] Spring Boot 프로젝트 세팅
- [ ] application.yml 설정
- [ ] JWT 인증 구현
- [ ] Gemini API 연동
- [ ] WebSocket 실시간 채팅 구현
- [ ] 상담 이력 API 구현
- [ ] Spring Batch 가짜 데이터 100만건 생성
- [ ] Spring Batch 통계 집계 Job
- [ ] 통계 API 구현
- [ ] 관리자 API 구현
- [ ] React 프로젝트 생성
- [ ] 고객 채팅 화면
- [ ] 상담 이력 페이지
- [ ] 통계 대시보드 페이지
- [ ] 관리자 페이지
- [ ] 인덱스 튜닝 + 성능 최적화
- [ ] 트래킹 스크립트 연동
- [ ] Google Cloud VM 배포

---

## 면접 어필 포인트

- Gemini API 연동 AI 챗봇 구현 (무료 플랜 활용)
- WebSocket + STOMP 실시간 채팅 구현
- Spring Batch로 100만건 가짜 데이터 bulk insert
- 복합 인덱스 + 커서 기반 페이징으로 대용량 조회 최적화
- Spring Batch 스케줄러로 일별 통계 자동 집계
- 기존 대시보드 플랫폼과 연동해 트래픽 분석 가능
- Google Cloud 생태계 통일 (VM + Gemini API)
- 4개 프로젝트가 유기적으로 연결되는 포트폴리오 스토리

---

## 전체 포트폴리오 연결 구조

```
1. 웹 서비스 통계 대시보드  ← 모든 프로젝트의 트래픽 분석
        ↑ 트래킹 스크립트 연동
2. 개인 포트폴리오 사이트   ← 면접관에게 공유하는 링크
3. 맛집 리뷰 플랫폼         ← 카카오맵 외부 API + GCS
4. AICC 미니 시스템         ← AI + 대용량 데이터 + 실시간
```
