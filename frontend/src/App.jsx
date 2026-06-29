import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { getAuth, isAdmin } from './store/authStore'

import Layout from './components/Layout'
import Login from './pages/Login'
import Chat from './pages/Chat'
import Consultations from './pages/Consultations'
import Dashboard from './pages/Dashboard'
import Admin from './pages/Admin'

/** 로그인이 필요한 라우트 */
function PrivateRoute({ children }) {
  return getAuth() ? children : <Navigate to="/login" replace />
}

/** ADMIN 전용 라우트 */
function AdminRoute({ children }) {
  if (!getAuth()) return <Navigate to="/login" replace />
  if (!isAdmin()) return <Navigate to="/chat" replace />
  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        {/* 공통 레이아웃 (사이드바) */}
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Layout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/chat" replace />} />
          <Route path="chat" element={<Chat />} />
          <Route path="consultations" element={<Consultations />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route
            path="admin"
            element={
              <AdminRoute>
                <Admin />
              </AdminRoute>
            }
          />
        </Route>

        {/* 알 수 없는 경로 → chat으로 */}
        <Route path="*" element={<Navigate to="/chat" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
