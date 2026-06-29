# AICC 미니 — 디자인 시스템

> frontend-developer가 바로 구현할 수 있는 수준의 Tailwind CSS 클래스 명세서  
> 스택: React + Tailwind CSS v3 + Recharts  
> 테마: 라이트 테마 전용 (B2B SaaS, AI 전문 서비스 느낌)

---

## 1. 색상 팔레트

### 1-1. Primary (브랜드 컬러)
AI Contact Center 특성에 맞는 **인디고-블루 계열**. 신뢰감 + 기술적 전문성.

| Token | Hex | 용도 |
|-------|-----|------|
| `primary-50`  | `#F0F1FF` | 배경 하이라이트, 활성 nav 배경 |
| `primary-100` | `#E1E4FF` | hover 배경, 배지 배경 |
| `primary-200` | `#C3C9FF` | 비활성 요소 강조 |
| `primary-300` | `#9CA6FF` | 아이콘 보조색 |
| `primary-400` | `#7B84FF` | 포커스 링 보조 |
| `primary-500` | `#5B6CF9` | 메인 브랜드색 (로고, 강조 텍스트) |
| `primary-600` | `#3D4FE8` | **버튼 기본 상태** |
| `primary-700` | `#2D3CC9` | **버튼 hover/active** |
| `primary-800` | `#2432A8` | 다크 강조 |
| `primary-900` | `#1B2785` | 아주 어두운 브랜드색 |
| `primary-950` | `#111654` | 거의 사용 안 함 |

### 1-2. Gray Scale (Slate 기반)
살짝 파란 기조가 섞여 primary와 자연스럽게 어우러짐.

| Token | Hex | 용도 |
|-------|-----|------|
| `gray-50`  | `#F8FAFC` | 페이지 배경 (`body`) |
| `gray-100` | `#F1F5F9` | 테이블 헤더 배경, 사이드바 hover |
| `gray-200` | `#E2E8F0` | 보더 기본 |
| `gray-300` | `#CBD5E1` | Divider, 비활성 보더 |
| `gray-400` | `#94A3B8` | 플레이스홀더, 아이콘 비활성 |
| `gray-500` | `#64748B` | 보조 텍스트 |
| `gray-600` | `#475569` | 본문 보조, 라벨 |
| `gray-700` | `#334155` | 본문 기본 |
| `gray-800` | `#1E293B` | **본문 메인** (`body` text color) |
| `gray-900` | `#0F172A` | 헤딩, 강조 텍스트 |
| `gray-950` | `#020617` | 거의 사용 안 함 |

### 1-3. 상태 컬러 (처리 상태 배지)

| 상태 | 배지 클래스 | 배경 | 텍스트 |
|------|------------|------|--------|
| RESOLVED (해결됨) | `.badge-resolved` | `#ECFDF5` (success-50) | `#047857` (success-700) |
| UNRESOLVED (미해결) | `.badge-unresolved` | `#FFF1F2` (error-50) | `#E11D48` (error-600) |
| IN_PROGRESS (진행중) | `.badge-in-progress` | `#FFFBEB` (warning-50) | `#B45309` (warning-700) |

```jsx
// 사용 예시
<span className="badge-resolved">해결됨</span>
<span className="badge-unresolved">미해결</span>
<span className="badge-in-progress">진행중</span>
```

---

## 2. 타이포그래피

### 폰트
- **영문**: `Inter` (Google Fonts)
- **한글**: `Noto Sans KR` (Google Fonts)
- **코드/모노**: `JetBrains Mono` (선택)

`globals.css`에서 Google Fonts import 처리됨.

### 계층 구조

| 역할 | 태그/클래스 | Tailwind | 크기 / 굵기 |
|------|------------|----------|------------|
| Page Title | `h1`, `.page-title` | `text-xl font-semibold text-gray-900` | 20px / 600 |
| Section Title | `h2` | `text-2xl font-semibold text-gray-900 tracking-tight` | 24px / 600 |
| Card Title | `h3`, `.card-title` | `text-sm font-semibold text-gray-800` | 14px / 600 |
| Body Default | `p`, `span` | `text-sm text-gray-700 leading-relaxed` | 14px / 400 |
| Body Small | `small` | `text-xs text-gray-500` | 12px / 400 |
| Label | `.input-label` | `text-sm font-medium text-gray-700` | 14px / 500 |
| Caption | — | `text-xs text-gray-400` | 12px / 400 |
| Stat Value | `.stat-card-value` | `text-3xl font-bold text-gray-900` | 30px / 700 |

---

## 3. 스페이싱 규칙

Tailwind 4px 기본 스케일 사용. 주요 기준값:

| 역할 | 값 | Tailwind |
|------|-----|----------|
| 컴포넌트 내부 패딩 (작은) | 8px | `p-2` |
| 컴포넌트 내부 패딩 (기본) | 12px | `p-3` |
| 카드 내부 패딩 | 20px | `p-5` |
| 페이지 좌우 패딩 | 32px | `px-8` |
| 페이지 상하 패딩 | 24px | `py-6` |
| 섹션 간 간격 | 24px | `space-y-6` |
| 카드 그리드 갭 | 20px | `gap-5` |
| 사이드바 너비 | 240px | `w-60` |

---

## 4. 그림자 & 라운딩

### 그림자 (커스텀)
| 토큰 | 용도 |
|------|------|
| `shadow-card` | 카드 기본 (`0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)`) |
| `shadow-card-hover` | 카드 hover (`0 4px 16px rgba(91,108,249,0.12), 0 1px 4px rgba(0,0,0,0.06)`) |
| `shadow-dropdown` | 드롭다운, 모달 (`0 8px 24px rgba(0,0,0,0.10)`) |
| `shadow-button-pri` | Primary 버튼 (`0 1px 3px rgba(61,79,232,0.25)`) |
| `shadow-xs` | 말풍선, 인풋 (`0 1px 2px rgba(0,0,0,0.04)`) |

### 라운딩
| 토큰 | px | 용도 |
|------|----|------|
| `rounded-xs` | 4px | 배지 |
| `rounded-sm` | 6px | 소형 버튼 |
| `rounded-md` | 8px | 버튼, 인풋 |
| `rounded-lg` | 12px | 카드, 모달 |
| `rounded-xl` | 16px | 큰 카드 |
| `rounded-2xl` | 20px | 채팅 말풍선 |
| `rounded-pill` | 9999px | 배지, 스크롤바 |

---

## 5. 컴포넌트 클래스 가이드

### 5-1. 버튼

모든 버튼은 `globals.css`에 `@layer components`로 정의됨.

```jsx
// Primary — 주요 액션 (전송, 저장, 상담 시작)
<button className="btn-primary">상담 시작</button>

// Secondary — 보조 액션 (취소, 뒤로, 필터 초기화)
<button className="btn-secondary">취소</button>

// Danger — destructive 액션 (상담 종료, 삭제)
<button className="btn-danger">상담 종료</button>

// Ghost — 아이콘 버튼, 조용한 액션
<button className="btn-ghost">
  <SearchIcon className="w-4 h-4" />
</button>

// 크기 modifier (btn-primary에 btn-sm/btn-lg 추가)
<button className="btn-primary btn-sm">작은 버튼</button>
<button className="btn-primary btn-lg">큰 버튼</button>
```

**클래스 조합 (직접 작성 시)**

```
Primary:   bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800
           px-4 py-2 text-sm font-medium rounded-md
           shadow-button-pri transition-all duration-150
           focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-1

Secondary: bg-white text-gray-700 border border-gray-200
           hover:bg-gray-50 hover:border-gray-300
           px-4 py-2 text-sm font-medium rounded-md transition-all duration-150

Danger:    bg-error-600 text-white hover:bg-error-700
           px-4 py-2 text-sm font-medium rounded-md transition-all duration-150
```

---

### 5-2. 인풋 필드

```jsx
// 기본 인풋
<div>
  <label className="input-label">이메일</label>
  <input
    type="email"
    className="input-field"
    placeholder="example@email.com"
  />
  <span className="input-hint">로그인에 사용된 이메일을 입력하세요.</span>
</div>

// 에러 상태
<div>
  <label className="input-label">비밀번호</label>
  <input type="password" className="input-field-error" />
  <span className="input-error-msg">비밀번호가 일치하지 않습니다.</span>
</div>
```

**직접 작성 시:**
```
기본:  w-full px-3.5 py-2.5 text-sm text-gray-800 bg-white
       border border-gray-200 rounded-md placeholder:text-gray-400
       transition-all duration-150
       focus:outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20

에러:  border-error-400 focus:border-error-500 focus:ring-error-500/20
```

---

### 5-3. 카드

```jsx
// 기본 카드
<div className="card">
  <div className="card-header">
    <span className="card-title">제목</span>
    <button className="btn-ghost btn-sm">더보기</button>
  </div>
  {/* 내용 */}
</div>

// hover 효과 있는 카드 (클릭 가능한 목록 아이템)
<div className="card-hover" onClick={...}>
  내용
</div>
```

---

### 5-4. 배지/태그

```jsx
// 처리 상태 배지
<span className="badge-resolved">해결됨</span>
<span className="badge-unresolved">미해결</span>
<span className="badge-in-progress">진행중</span>

// 기타
<span className="badge-primary">AI 상담</span>
<span className="badge-gray">카테고리명</span>

// 필터 칩 (상담 이력 필터 UI)
<button className="chip">전체</button>
<button className="chip-active">해결됨</button>
```

---

### 5-5. 채팅 말풍선

```jsx
// 채팅 메시지 컨테이너 구조
<div className="flex flex-col gap-4 p-4">

  {/* AI 메시지 — 왼쪽 정렬 */}
  <div className="flex items-start gap-2.5">
    {/* AI 아바타 */}
    <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center shrink-0">
      <span className="text-primary-600 text-xs font-bold">AI</span>
    </div>
    <div className="flex flex-col gap-1">
      <span className="text-xs text-gray-400 ml-1">AI 상담원</span>
      <div className="chat-bubble-ai">
        안녕하세요! 무엇을 도와드릴까요?
      </div>
    </div>
  </div>

  {/* 사용자 메시지 — 오른쪽 정렬 */}
  <div className="flex items-start gap-2.5 flex-row-reverse">
    <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center shrink-0">
      <span className="text-gray-600 text-xs font-bold">나</span>
    </div>
    <div className="flex flex-col items-end gap-1">
      <span className="text-xs text-gray-400 mr-1">나</span>
      <div className="chat-bubble-user">
        배송 조회를 하고 싶어요.
      </div>
    </div>
  </div>

  {/* AI 타이핑 인디케이터 */}
  <div className="flex items-start gap-2.5">
    <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center shrink-0">
      <span className="text-primary-600 text-xs font-bold">AI</span>
    </div>
    <div className="chat-typing-indicator">
      <span className="chat-typing-dot animate-dot-1" />
      <span className="chat-typing-dot animate-dot-2" />
      <span className="chat-typing-dot animate-dot-3" />
    </div>
  </div>

</div>
```

---

## 6. 전체 레이아웃 구조

### 6-1. 공통 레이아웃 (사이드바 + 콘텐츠)

```
┌──────────────────────────────────────────────────────────┐
│ sidebar (w-60 = 240px, fixed)  │  page-layout (ml-60)   │
│                                │                         │
│  sidebar-logo                  │  page-header            │
│  ─────────────────────────     │    page-title           │
│  sidebar-nav                   │    page-subtitle        │
│    sidebar-nav-item-active     │  ────────────────────   │
│    sidebar-nav-item            │  page-content           │
│    sidebar-nav-item            │    (각 페이지 내용)     │
│    sidebar-nav-item (ADMIN)    │                         │
│                                │                         │
│  sidebar-footer                │                         │
│    [사용자 정보]               │                         │
└──────────────────────────────────────────────────────────┘
```

```jsx
// App.jsx 레이아웃 구조 예시
<div className="flex">
  <Sidebar />
  <div className="page-layout">
    <div className="page-header">
      <h1 className="page-title">페이지 제목</h1>
      <p className="page-subtitle">부제목 설명</p>
    </div>
    <div className="page-content">
      {/* 페이지 컨텐츠 */}
    </div>
  </div>
</div>
```

### 6-2. 사이드바 내부 구조

```jsx
<aside className="sidebar">
  {/* 로고 영역 */}
  <div className="sidebar-logo">
    <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
      <span className="text-white text-xs font-bold">AI</span>
    </div>
    <div>
      <p className="text-sm font-bold text-gray-900">AICC 미니</p>
      <p className="text-xs text-gray-400">AI 상담 센터</p>
    </div>
  </div>

  {/* 네비게이션 */}
  <nav className="sidebar-nav">
    <p className="sidebar-section-label">메뉴</p>
    <NavLink to="/chat"          className={isActive => isActive ? "sidebar-nav-item-active" : "sidebar-nav-item"}>
      <ChatIcon className="w-4 h-4" /> 채팅 상담
    </NavLink>
    <NavLink to="/consultations" className={...}>
      <ListIcon className="w-4 h-4" /> 상담 이력
    </NavLink>
    <NavLink to="/dashboard"     className={...}>
      <ChartIcon className="w-4 h-4" /> 대시보드
    </NavLink>

    {isAdmin && (
      <>
        <p className="sidebar-section-label mt-3">관리자</p>
        <NavLink to="/admin" className={...}>
          <SettingsIcon className="w-4 h-4" /> 관리자
        </NavLink>
      </>
    )}
  </nav>

  {/* 사용자 정보 */}
  <div className="sidebar-footer">
    <div className="flex items-center gap-3">
      <div className="w-8 h-8 rounded-full bg-gray-200" />
      <div>
        <p className="text-sm font-medium text-gray-800">{user.name}</p>
        <p className="text-xs text-gray-400">{user.role}</p>
      </div>
    </div>
  </div>
</aside>
```

---

## 7. 페이지별 레이아웃 가이드

### 7-1. 로그인 페이지 (`/login`)

```
┌──────────────────────────────────────────┐
│                                          │
│   [AICC 로고]                            │
│   AI 고객 상담 센터                      │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │  이메일                            │  │
│  │  [input-field           ]          │  │
│  │                                    │  │
│  │  비밀번호                          │  │
│  │  [input-field           ]          │  │
│  │                                    │  │
│  │  [btn-primary: 로그인    ]         │  │
│  │                                    │  │
│  │  계정이 없으신가요? 회원가입        │  │
│  └────────────────────────────────────┘  │
│                                          │
└──────────────────────────────────────────┘
```

```jsx
// 로그인 페이지 구조
<div className="min-h-screen bg-gray-50 flex items-center justify-center">
  <div className="w-full max-w-md">
    {/* 헤더 */}
    <div className="text-center mb-8">
      <div className="inline-flex items-center justify-center w-12 h-12 bg-primary-600 rounded-xl mb-4">
        <span className="text-white font-bold">AI</span>
      </div>
      <h1 className="text-2xl font-bold text-gray-900">AICC 미니</h1>
      <p className="text-gray-500 mt-1 text-sm">AI 고객 상담 센터</p>
    </div>

    {/* 폼 카드 */}
    <div className="card p-7">
      <form className="space-y-4">
        <div>
          <label className="input-label">이메일</label>
          <input type="email" className="input-field" placeholder="example@email.com" />
        </div>
        <div>
          <label className="input-label">비밀번호</label>
          <input type="password" className="input-field" placeholder="••••••••" />
        </div>
        <button type="submit" className="btn-primary w-full mt-2">로그인</button>
      </form>
      <p className="text-center text-sm text-gray-500 mt-5">
        계정이 없으신가요? <a href="/signup">회원가입</a>
      </p>
    </div>
  </div>
</div>
```

---

### 7-2. 채팅 페이지 (`/chat`)

```
┌──────────────────────────────────────────┐
│ page-header                              │
│  "AI 상담"    [badge-in-progress: 상담중] │
│               [btn-danger: 상담 종료]    │
├──────────────────────────────────────────┤
│                                          │
│  [AI 아바타] chat-bubble-ai              │
│              안녕하세요! 무엇을...       │
│                                          │
│                  chat-bubble-user [아바타]│
│                  배송 조회하고 싶어요.   │
│                                          │
│  [AI 아바타] [타이핑 인디케이터 ···]    │
│                                          │
├──────────────────────────────────────────┤
│  [input-field 메시지 입력...]  [전송 ▶] │
└──────────────────────────────────────────┘
```

```jsx
// 채팅 페이지 전체 구조
<div className="page-layout flex flex-col" style={{ height: '100vh' }}>
  {/* 헤더 */}
  <div className="page-header flex items-center justify-between shrink-0">
    <div className="flex items-center gap-3">
      <h1 className="page-title">AI 상담</h1>
      <span className="badge-in-progress">상담중</span>
    </div>
    <button className="btn-danger btn-sm">상담 종료</button>
  </div>

  {/* 메시지 영역 (스크롤) */}
  <div className="flex-1 overflow-y-auto px-8 py-6 space-y-4">
    {/* 메시지 목록 */}
  </div>

  {/* 입력창 */}
  <div className="shrink-0 px-8 py-4 bg-white border-t border-gray-100">
    <div className="flex items-center gap-3">
      <input
        className="input-field flex-1"
        placeholder="메시지를 입력하세요..."
      />
      <button className="btn-primary shrink-0">
        <SendIcon className="w-4 h-4" />
        전송
      </button>
    </div>
  </div>
</div>
```

---

### 7-3. 상담 이력 페이지 (`/consultations`)

```
┌──────────────────────────────────────────────────────────┐
│ page-header                                              │
│  "상담 이력"                                             │
├──────────────────────────────────────────────────────────┤
│ page-content                                             │
│                                                          │
│  [input-field 검색 🔍]  [chip: 전체] [chip-active: 해결] │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 번호 │ 제목(요약) │ 카테고리 │ 상태 배지 │ 날짜   │  │
│  ├──────┼───────────┼──────────┼──────────┼─────────┤  │
│  │  1   │ 배송 관련  │ 배송     │ 해결됨   │ 06/29  │  │
│  │  2   │ 환불 문의  │ 환불     │ 진행중   │ 06/28  │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  [더 보기 버튼] (커서 기반 페이징)                        │
└──────────────────────────────────────────────────────────┘
```

```jsx
// 상담 이력 페이지 핵심 구조
<div className="page-content space-y-4">
  {/* 검색 & 필터 */}
  <div className="flex items-center gap-3 flex-wrap">
    <div className="relative flex-1 max-w-sm">
      <input className="input-field pl-9" placeholder="상담 내용 검색..." />
      <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
    </div>
    <div className="flex gap-2">
      <button className="chip-active">전체</button>
      <button className="chip">해결됨</button>
      <button className="chip">미해결</button>
      <button className="chip">진행중</button>
    </div>
  </div>

  {/* 테이블 */}
  <div className="table-wrapper">
    <table className="table">
      <thead>
        <tr>
          <th>번호</th>
          <th>상담 요약</th>
          <th>카테고리</th>
          <th>처리 상태</th>
          <th>상담 일시</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td className="text-gray-400">#1024</td>
          <td className="font-medium text-gray-800">배송 조회 문의</td>
          <td><span className="badge-gray">배송</span></td>
          <td><span className="badge-resolved">해결됨</span></td>
          <td className="text-gray-500">2026-06-29</td>
        </tr>
      </tbody>
    </table>
  </div>

  {/* 더 보기 (커서 기반 페이징) */}
  <div className="flex justify-center pt-2">
    <button className="btn-secondary">더 보기</button>
  </div>
</div>
```

---

### 7-4. 통계 대시보드 (`/dashboard`)

```
┌─────────────────────────────────────────────────────────┐
│ page-header                                             │
│  "대시보드"    [chip: 일별] [chip-active: 주별] [chip: 월별]│
├─────────────────────────────────────────────────────────┤
│ page-content                                            │
│                                                         │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────┐ │
│  │ stat-card  │ │ stat-card  │ │ stat-card  │ │ stat │ │
│  │ 총 상담수  │ │ 해결률(%)  │ │ 해결 건수  │ │ 진행 │ │
│  │  12,847   │ │   87.3%   │ │  11,215   │ │ 1,632│ │
│  │ ↑ 12.5%  │ │ ↑ 2.1%   │ │           │ │      │ │
│  └────────────┘ └────────────┘ └────────────┘ └──────┘ │
│                                                         │
│  ┌───────────────────────────┐ ┌──────────────────────┐ │
│  │  카드: 일별 상담 추이      │ │ 카드: 카테고리 분포  │ │
│  │  (Recharts LineChart)     │ │ (Recharts PieChart)  │ │
│  │                           │ │                      │ │
│  └───────────────────────────┘ └──────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

```jsx
// 대시보드 핵심 구조
<div className="page-content space-y-6">
  {/* 기간 필터 */}
  <div className="flex gap-2">
    <button className="chip">일별</button>
    <button className="chip-active">주별</button>
    <button className="chip">월별</button>
  </div>

  {/* 요약 지표 카드 4개 */}
  <div className="grid grid-cols-4 gap-5">
    <div className="stat-card">
      <p className="stat-card-label">총 상담수</p>
      <p className="stat-card-value">12,847</p>
      <p className="stat-card-change-up">↑ 12.5% 지난 기간 대비</p>
    </div>
    <div className="stat-card">
      <p className="stat-card-label">해결률</p>
      <p className="stat-card-value">87.3%</p>
      <p className="stat-card-change-up">↑ 2.1%</p>
    </div>
    <div className="stat-card">
      <p className="stat-card-label">해결 건수</p>
      <p className="stat-card-value">11,215</p>
    </div>
    <div className="stat-card">
      <p className="stat-card-label">진행중</p>
      <p className="stat-card-value">1,632</p>
      <p className="stat-card-change-down">↓ 4.3%</p>
    </div>
  </div>

  {/* 차트 2개 */}
  <div className="grid grid-cols-5 gap-5">
    {/* 라인 차트: col-span-3 */}
    <div className="card col-span-3">
      <div className="card-header">
        <span className="card-title">일별 상담 추이</span>
      </div>
      <ResponsiveContainer width="100%" height={240}>
        <LineChart data={dailyData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
          <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#94A3B8' }} />
          <YAxis tick={{ fontSize: 12, fill: '#94A3B8' }} />
          <Tooltip />
          <Line
            type="monotone"
            dataKey="count"
            stroke="#3D4FE8"  /* primary-600 */
            strokeWidth={2}
            dot={{ r: 3, fill: '#3D4FE8' }}
            activeDot={{ r: 5 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>

    {/* 파이 차트: col-span-2 */}
    <div className="card col-span-2">
      <div className="card-header">
        <span className="card-title">카테고리별 분포</span>
      </div>
      <ResponsiveContainer width="100%" height={240}>
        <PieChart>
          <Pie
            data={categoryData}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="50%"
            outerRadius={80}
            label
          >
            {categoryData.map((_, i) => (
              <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  </div>
</div>

// 파이 차트 컬러 팔레트 (Recharts용)
const PIE_COLORS = ['#3D4FE8', '#10B981', '#F59E0B', '#F43F5E', '#94A3B8', '#7B84FF'];
```

---

### 7-5. 관리자 페이지 (`/admin`)

```
┌──────────────────────────────────────────────────────────┐
│ page-header                                              │
│  "관리자"                                                │
├──────────────────────────────────────────────────────────┤
│ page-content                                             │
│                                                          │
│  ┌──────────────────────────┐  ┌────────────────────┐   │
│  │ card: 카테고리 관리      │  │ card: FAQ 관리     │   │
│  │                          │  │                    │   │
│  │ [배송] [환불] [기술지원] │  │ Q. 배송은 얼마나? │   │
│  │ [+ 추가]                 │  │ Q. 환불 방법은?   │   │
│  │                          │  │ [+ FAQ 등록]      │   │
│  └──────────────────────────┘  └────────────────────┘   │
│                                                          │
│  card: Spring Batch 제어                                 │
│  [btn-secondary: 가짜 데이터 100만건 생성]               │
│  [btn-secondary: 통계 집계 실행]                         │
└──────────────────────────────────────────────────────────┘
```

---

## 8. Recharts 스타일 가이드

```jsx
// 공통 Recharts 스타일 상수 (constants/chartConfig.js에 정의 권장)
export const CHART_COLORS = {
  primary:  '#3D4FE8',  // primary-600
  success:  '#10B981',  // success-500
  warning:  '#F59E0B',  // warning-500
  error:    '#F43F5E',  // error-500
  gray:     '#94A3B8',  // gray-400
};

export const CHART_STYLE = {
  grid: {
    strokeDasharray: '3 3',
    stroke: '#F1F5F9',  // gray-100
  },
  tick: {
    fontSize: 12,
    fill: '#94A3B8',    // gray-400
  },
  tooltip: {
    contentStyle: {
      borderRadius: '8px',
      border: '1px solid #E2E8F0',
      boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
      fontSize: '13px',
    },
  },
};
```

---

## 9. 반응형 고려사항

현재 포트폴리오 목적상 데스크탑(1280px+) 기준 고정 레이아웃.  
모바일 대응이 필요하다면 추후 추가.

| 중단점 | 적용 |
|--------|------|
| `lg` (1024px+) | 대시보드 카드 4열 그리드 |
| `md` (768px+) | 대시보드 카드 2열 |
| 기본 | 단열 (사이드바 숨김 처리 필요) |

---

## 10. 체크리스트 (frontend-developer용)

- [ ] `globals.css`를 `main.jsx`에서 import (`import './styles/globals.css'`)
- [ ] Google Fonts 로드 확인 (Inter + Noto Sans KR)
- [ ] `tailwind.config.js` content 경로 확인 (`./src/**/*.{js,ts,jsx,tsx}`)
- [ ] `postcss.config.js`에 `tailwindcss`, `autoprefixer` 플러그인 등록 확인
- [ ] Recharts `PIE_COLORS` 배열을 `chartConfig.js`로 분리해 재사용
- [ ] 채팅 메시지 스크롤: `useEffect`로 최신 메시지에 `scrollIntoView()` 적용
- [ ] 커서 기반 페이징: `cursor` 쿼리 파라미터로 API 호출, 응답의 `nextCursor` 저장
