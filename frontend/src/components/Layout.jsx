import { useState, useEffect } from 'react'
import { NavLink, useNavigate, Outlet } from 'react-router-dom'
import { getAuth, clearAuth, isAdmin } from '../store/authStore'

const NAV_ITEMS = [
  { to: '/chat', label: '채팅 상담', icon: '💬' },
  { to: '/consultations', label: '상담 이력', icon: '📋' },
  { to: '/dashboard', label: '통계 대시보드', icon: '📊' },
]

export default function Layout() {
  const navigate = useNavigate()
  const [auth, setAuth] = useState(getAuth())

  useEffect(() => {
    const sync = () => setAuth(getAuth())
    window.addEventListener('auth-change', sync)
    return () => window.removeEventListener('auth-change', sync)
  }, [])

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  const navItems = auth?.role === 'ADMIN'
    ? [...NAV_ITEMS, { to: '/admin', label: '관리자', icon: '⚙️' }]
    : NAV_ITEMS

  return (
    <div className="bg-gray-50 min-h-screen">
      {/* 사이드바 */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="w-8 h-8 rounded-lg bg-primary-500 flex items-center justify-center text-white text-sm font-bold flex-shrink-0">
            AI
          </div>
          <div className="min-w-0">
            <h1 className="text-sm font-bold text-gray-900 leading-tight">AICC 미니</h1>
            <p className="text-xs text-gray-400">AI 상담 센터</p>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navItems.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                isActive ? 'sidebar-nav-item-active' : 'sidebar-nav-item'
              }
            >
              <span>{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <p className="text-xs text-gray-500 truncate mb-2">{auth?.email}</p>
          <button onClick={handleLogout} className="btn-ghost btn-sm w-full justify-start">
            로그아웃
          </button>
        </div>
      </aside>

      {/* 콘텐츠 영역 */}
      <main className="ml-60 h-screen overflow-auto flex flex-col">
        <Outlet />
      </main>
    </div>
  )
}
