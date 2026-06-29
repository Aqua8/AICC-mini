const STATUS_MAP = {
  RESOLVED: { label: '해결됨', className: 'bg-success-50 text-success-700 border-success-200' },
  UNRESOLVED: { label: '미해결', className: 'bg-error-50 text-error-700 border-error-200' },
  IN_PROGRESS: { label: '진행중', className: 'bg-warning-50 text-warning-700 border-warning-200' },
}

export default function ConsultationCard({ item, onClick }) {
  const status = STATUS_MAP[item.status] ?? { label: item.status, className: 'bg-gray-100 text-gray-600' }

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-lg border border-gray-200 px-5 py-4 hover:shadow-card-hover hover:border-primary-200 cursor-pointer transition-all"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-semibold text-gray-800 truncate">{item.title}</h3>
          {item.summary && (
            <p className="text-xs text-gray-500 mt-1 line-clamp-2">{item.summary}</p>
          )}
        </div>
        <span
          className={`flex-shrink-0 text-xs font-medium px-2 py-0.5 rounded-pill border ${status.className}`}
        >
          {status.label}
        </span>
      </div>
      <div className="flex items-center gap-3 mt-3 text-xs text-gray-400">
        {item.categoryName && (
          <span className="bg-primary-50 text-primary-600 px-2 py-0.5 rounded-pill font-medium">
            {item.categoryName}
          </span>
        )}
        <span>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</span>
      </div>
    </div>
  )
}
