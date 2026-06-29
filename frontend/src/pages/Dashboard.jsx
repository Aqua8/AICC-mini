import { useState, useEffect } from 'react'
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts'
import { getDailyStats, getCategoryStats, getSummaryStats } from '../api/stats'
import StatCard from '../components/StatCard'

function getDateRange(days) {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - (days - 1))
  const fmt = (d) => d.toISOString().slice(0, 10)
  return { from: fmt(from), to: fmt(to) }
}

const PIE_COLORS = ['#5B6CF9', '#10B981', '#F59E0B', '#F43F5E', '#8B5CF6', '#06B6D4']

export default function Dashboard() {
  const [summary, setSummary] = useState(null)
  const [daily, setDaily] = useState([])
  const [category, setCategory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const { from, to } = getDateRange(30)
    Promise.all([getSummaryStats(), getDailyStats(from, to), getCategoryStats()])
      .then(([s, d, c]) => { setSummary(s); setDaily(d); setCategory(c) })
      .catch(() => setError('통계 데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="empty-state h-full">통계를 불러오는 중...</div>
  if (error) return <div className="empty-state h-full text-error-500">{error}</div>

  // 백엔드 resolutionRate는 이미 퍼센트값(예: 75.0)으로 반환됨
  const resolutionPct = summary ? `${summary.resolutionRate.toFixed(1)}%` : '-'

  return (
    <div className="page-content space-y-6 overflow-y-auto">
      <h2 className="page-title">통계 대시보드</h2>

      {/* 요약 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="총 상담 수" value={summary?.totalConsultations ?? 0} color="primary" />
        <StatCard label="해결률" value={resolutionPct} color="success" />
        <StatCard label="해결됨" value={summary?.resolved ?? 0} color="success" />
        <StatCard label="미해결" value={summary?.unresolved ?? 0} color="error" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 일별 상담 건수 */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">일별 상담 건수 (최근 30일)</h3>
          </div>
          {daily.length === 0 ? (
            <div className="empty-state py-12">데이터 없음</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={daily} margin={{ top: 4, right: 12, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#94A3B8' }}
                  tickFormatter={(v) => v.slice(5)} />
                <YAxis tick={{ fontSize: 11, fill: '#94A3B8' }} />
                <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #E2E8F0' }} />
                <Line type="monotone" dataKey="totalCount" stroke="#5B6CF9" strokeWidth={2}
                  dot={false} name="상담 수" />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* 카테고리별 분포 */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">카테고리별 분포</h3>
          </div>
          {category.length === 0 ? (
            <div className="empty-state py-12">데이터 없음</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={category} dataKey="count" nameKey="name"
                  cx="50%" cy="50%" outerRadius={80}
                  label={({ name, percent }) =>
                    `${name} ${(percent * 100).toFixed(0)}%`}
                  labelLine={false}>
                  {category.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Legend formatter={(v) => <span style={{ fontSize: 12, color: '#475569' }}>{v}</span>} />
                <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #E2E8F0' }} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* 처리 상태 현황 */}
      {summary && (
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">처리 상태 현황</h3>
          </div>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-2xl font-bold text-success-600">{summary.resolved.toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">해결됨</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-error-500">{summary.unresolved.toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">미해결</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-warning-500">{summary.inProgress.toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">진행중</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
