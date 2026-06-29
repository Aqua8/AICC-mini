const VALUE_COLOR = {
  primary: 'text-primary-600',
  success: 'text-success-600',
  warning: 'text-warning-600',
  error: 'text-error-600',
  gray: 'text-gray-600',
}

export default function StatCard({ label, value, color = 'primary' }) {
  return (
    <div className="stat-card">
      <p className="stat-card-label">{label}</p>
      <p className={`stat-card-value ${VALUE_COLOR[color] ?? VALUE_COLOR.primary}`}>
        {typeof value === 'number' ? value.toLocaleString() : value}
      </p>
    </div>
  )
}
