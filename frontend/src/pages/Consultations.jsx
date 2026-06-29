import { useState, useEffect, useCallback } from 'react'
import { getConsultations, getConsultation } from '../api/consultations'
import { getCategories } from '../api/admin'
import ConsultationCard from '../components/ConsultationCard'
import MessageBubble from '../components/MessageBubble'

const STATUS_OPTIONS = [
  { value: '', label: '전체 상태' },
  { value: 'RESOLVED', label: '해결됨' },
  { value: 'UNRESOLVED', label: '미해결' },
  { value: 'IN_PROGRESS', label: '진행중' },
]

export default function Consultations() {
  const [items, setItems] = useState([])
  const [nextCursor, setNextCursor] = useState(null)
  const [hasNext, setHasNext] = useState(false)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)

  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [status, setStatus] = useState('')
  const [categories, setCategories] = useState([])

  const [selected, setSelected] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)

  useEffect(() => {
    getCategories().then(setCategories).catch(() => {})
  }, [])

  const fetchItems = useCallback(async (cursor = null, append = false) => {
    const params = { size: 20 }
    if (cursor) params.cursor = cursor
    if (keyword) params.keyword = keyword
    if (categoryId) params.categoryId = categoryId
    if (status) params.status = status

    try {
      const data = await getConsultations(params)
      if (append) {
        setItems((prev) => [...prev, ...(data.content ?? [])])
      } else {
        setItems(data.content ?? [])
      }
      setNextCursor(data.nextCursor ?? null)
      setHasNext(data.hasNext ?? false)
    } catch {
      // 조용히 처리
    }
  }, [keyword, categoryId, status])

  useEffect(() => {
    setLoading(true)
    fetchItems(null, false).finally(() => setLoading(false))
  }, [fetchItems])

  const handleLoadMore = async () => {
    setLoadingMore(true)
    await fetchItems(nextCursor, true)
    setLoadingMore(false)
  }

  const handleCardClick = async (id) => {
    setDetailLoading(true)
    setSelected(null)
    try {
      setSelected(await getConsultation(id))
    } catch {
      alert('상세 정보를 불러오지 못했습니다.')
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <div className="flex h-full">
      {/* 목록 패널 */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* 헤더 + 검색/필터 */}
        <div className="page-header">
          <h2 className="page-title mb-3">상담 이력</h2>
          <div className="flex flex-wrap gap-2">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="키워드 검색"
              className="input-field flex-1 min-w-40"
            />
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="input-field w-auto"
            >
              <option value="">전체 카테고리</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="input-field w-auto"
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>
        </div>

        {/* 목록 */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3">
          {loading ? (
            <div className="empty-state">불러오는 중...</div>
          ) : items.length === 0 ? (
            <div className="empty-state">검색 결과가 없습니다</div>
          ) : (
            <>
              {items.map((item) => (
                <ConsultationCard
                  key={item.id}
                  item={item}
                  onClick={() => handleCardClick(item.id)}
                />
              ))}
              {hasNext && (
                <button
                  onClick={handleLoadMore}
                  disabled={loadingMore}
                  className="btn-secondary w-full"
                >
                  {loadingMore ? '불러오는 중...' : '더 보기'}
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* 상세 패널 */}
      {(selected || detailLoading) && (
        <div className="w-96 border-l border-gray-200 bg-white flex flex-col">
          <div className="page-header flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-800 truncate flex-1">
              {selected ? selected.title : '불러오는 중...'}
            </h3>
            <button
              onClick={() => { setSelected(null); setDetailLoading(false) }}
              className="btn-ghost btn-sm ml-2"
            >
              ×
            </button>
          </div>

          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
            {detailLoading ? (
              <div className="empty-state text-sm">불러오는 중...</div>
            ) : selected ? (
              <>
                {selected.summary && (
                  <div className="bg-primary-50 border border-primary-100 rounded-lg px-4 py-3">
                    <p className="text-xs font-semibold text-primary-600 mb-1">AI 요약</p>
                    <p className="text-sm text-gray-700 leading-relaxed">{selected.summary}</p>
                  </div>
                )}

                <div className="flex flex-wrap items-center gap-2 text-xs text-gray-500">
                  {selected.categoryName && (
                    <span className="badge-primary">{selected.categoryName}</span>
                  )}
                  <span>{new Date(selected.createdAt).toLocaleString('ko-KR')}</span>
                </div>

                <div className="space-y-3 pt-2">
                  {(selected.messages ?? []).map((msg, i) => (
                    <MessageBubble
                      key={msg.id ?? i}
                      sender={msg.sender}
                      content={msg.content}
                      createdAt={msg.createdAt}
                    />
                  ))}
                </div>
              </>
            ) : null}
          </div>
        </div>
      )}
    </div>
  )
}
