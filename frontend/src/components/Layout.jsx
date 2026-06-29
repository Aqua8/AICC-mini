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
    <div className="flex h-screen bg-gray-100">
      {/* 사이드바 */}
      <aside className="w-56 bg-indigo-800 text-white flex flex-col shadow-lg">
        <div className="px-6 py-5 border-b border-indigo-700">
          <h1 className="text-lg font-bold tracking-wide">AICC 미니</h1>
          <p className="text-indigo-300 text-xs mt-0.5">AI 상담 센터</p>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {navItems.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-600 text-white'
                    : 'text-indigo-200 hover:bg-indigo-700 hover:text-white'
                }`
              }
            >
              <span>{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-indigo-700">
          <p className="text-xs text-indigo-300 truncate mb-2">{auth?.email}</p>
          <button
            onClick={handleLogout}
            className="w-full text-xs text-indigo-200 hover:text-white hover:bg-indigo-700 rounded-md px-3 py-1.5 text-left transition-colors"
          >
            로그아웃
          </button>
        </div>
      </aside>

      {/* 콘텐츠 영역 */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
