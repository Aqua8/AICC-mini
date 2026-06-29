const STATUS_BADGE = {
  RESOLVED: 'badge-resolved',
  UNRESOLVED: 'badge-unresolved',
  IN_PROGRESS: 'badge-in-progress',
}

const STATUS_LABEL = {
  RESOLVED: '해결됨',
  UNRESOLVED: '미해결',
  IN_PROGRESS: '진행중',
}

export default function ConsultationCard({ item, onClick }) {
  const badgeClass = STATUS_BADGE[item.status] ?? 'badge-gray'
  const badgeLabel = STATUS_LABEL[item.status] ?? item.status

  return (
    <div onClick={onClick} className="card-hover">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-semibold text-gray-800 truncate">{item.title}</h3>
          {item.summary && (
            <p className="text-xs text-gray-500 mt-1 line-clamp-2">{item.summary}</p>
          )}
        </div>
        <span className={badgeClass}>{badgeLabel}</span>
      </div>
      <div className="flex items-center gap-2 mt-3 text-xs text-gray-400">
        {item.categoryName && (
          <span className="badge-primary">{item.categoryName}</span>
        )}
        <span>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</span>
      </div>
    </div>
  )
}
