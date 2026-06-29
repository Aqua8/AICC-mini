export default function StatCard({ label, value, sub, color = 'primary' }) {
  const colorMap = {
    primary: 'text-primary-600',
    success: 'text-success-600',
    warning: 'text-warning-600',
    error: 'text-error-600',
    gray: 'text-gray-600',
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 px-6 py-5 shadow-card">
      <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">{label}</p>
      <p className={`text-3xl font-bold mt-2 ${colorMap[color] ?? colorMap.primary}`}>
        {typeof value === 'number' ? value.toLocaleString() : value}
      </p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  )
}
