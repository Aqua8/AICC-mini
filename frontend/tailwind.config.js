/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      // ── 브랜드 컬러 팔레트 ──────────────────────────────────
      // AI 제품 느낌의 블루-바이올렛 계열
      // 기준색: #5B6CF9 (Indigo-Blue, 신뢰감 있는 AI 전문 서비스 톤)
      colors: {
        primary: {
          50:  '#F0F1FF',
          100: '#E1E4FF',
          200: '#C3C9FF',
          300: '#9CA6FF',
          400: '#7B84FF',
          500: '#5B6CF9', // 메인 브랜드 컬러
          600: '#3D4FE8', // 버튼 기본
          700: '#2D3CC9', // 버튼 hover
          800: '#2432A8',
          900: '#1B2785',
          950: '#111654',
        },
        // Slate-gray: 파란 기조가 살짝 섞여 primary와 어우러짐
        gray: {
          50:  '#F8FAFC',
          100: '#F1F5F9',
          200: '#E2E8F0',
          300: '#CBD5E1',
          400: '#94A3B8',
          500: '#64748B',
          600: '#475569',
          700: '#334155',
          800: '#1E293B',
          900: '#0F172A',
          950: '#020617',
        },
        // 상태 컬러 (처리상태 배지에 사용)
        success: {
          50:  '#ECFDF5',
          100: '#D1FAE5',
          200: '#A7F3D0',
          500: '#10B981',
          600: '#059669',
          700: '#047857',
        },
        warning: {
          50:  '#FFFBEB',
          100: '#FEF3C7',
          200: '#FDE68A',
          500: '#F59E0B',
          600: '#D97706',
          700: '#B45309',
        },
        error: {
          50:  '#FFF1F2',
          100: '#FFE4E6',
          200: '#FECDD3',
          500: '#F43F5E',
          600: '#E11D48',
          700: '#BE123C',
        },
      },

      // ── 폰트 ────────────────────────────────────────────────
      fontFamily: {
        sans: ['Inter', 'Noto Sans KR', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },

      // ── 그림자 ──────────────────────────────────────────────
      boxShadow: {
        'xs':          '0 1px 2px 0 rgba(0,0,0,0.04)',
        'card':        '0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)',
        'card-hover':  '0 4px 16px rgba(91,108,249,0.12), 0 1px 4px rgba(0,0,0,0.06)',
        'dropdown':    '0 8px 24px rgba(0,0,0,0.10), 0 2px 6px rgba(0,0,0,0.06)',
        'focus':       '0 0 0 3px rgba(91,108,249,0.18)',
        'button-pri':  '0 1px 3px rgba(61,79,232,0.25)',
      },

      // ── 라운딩 ──────────────────────────────────────────────
      borderRadius: {
        'xs':   '0.25rem',   // 4px  - 작은 배지
        'sm':   '0.375rem',  // 6px  - 소형 버튼
        'md':   '0.5rem',    // 8px  - 기본 버튼, 인풋
        'lg':   '0.75rem',   // 12px - 카드
        'xl':   '1rem',      // 16px - 큰 카드
        '2xl':  '1.25rem',   // 20px - 말풍선
        'pill': '9999px',    // 완전 원형 - 배지, 스크롤바
      },

      // ── 애니메이션 ──────────────────────────────────────────
      keyframes: {
        'fade-in': {
          '0%':   { opacity: '0', transform: 'translateY(6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'slide-in-left': {
          '0%':   { opacity: '0', transform: 'translateX(-10px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        'slide-in-right': {
          '0%':   { opacity: '0', transform: 'translateX(10px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        // AI 타이핑 인디케이터 점 3개 애니메이션
        'dot-bounce': {
          '0%, 80%, 100%': { transform: 'scale(0.7)', opacity: '0.4' },
          '40%':           { transform: 'scale(1)',   opacity: '1'   },
        },
      },
      animation: {
        'fade-in':        'fade-in 0.2s ease-out',
        'slide-in-left':  'slide-in-left 0.2s ease-out',
        'slide-in-right': 'slide-in-right 0.2s ease-out',
        'dot-1':          'dot-bounce 1.2s ease-in-out infinite',
        'dot-2':          'dot-bounce 1.2s ease-in-out 0.2s infinite',
        'dot-3':          'dot-bounce 1.2s ease-in-out 0.4s infinite',
      },

      // ── 스페이싱 확장 ────────────────────────────────────────
      spacing: {
        '4.5': '1.125rem',
        '13':  '3.25rem',
        '15':  '3.75rem',
        '18':  '4.5rem',
        '60':  '15rem',  // 사이드바 너비 (w-60 = 240px)
      },
    },
  },
  plugins: [],
}
