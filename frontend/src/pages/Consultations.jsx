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

  // 카테고리 목록 로드
  useEffect(() => {
    getCategories()
      .then(setCategories)
      .catch(() => {}) // 관리자 API 접근 불가 시 무시
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
        setItems((prev) => [...prev, ...(data.items ?? [])])
      } else {
        setItems(data.items ?? [])
      }
      setNextCursor(data.nextCursor ?? null)
      setHasNext(data.hasNext ?? false)
    } catch {
      // 에러는 조용히 처리
    }
  }, [keyword, categoryId, status])

  // 검색 조건 바뀌면 처음부터 다시 로드
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
      const detail = await getConsultation(id)
      setSelected(detail)
    } catch {
      alert('상세 정보를 불러오지 못했습니다.')
    } finally {
      setDetailLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    // fetchItems는 keyword/categoryId/status 의존성으로 자동 실행됨
  }

  return (
    <div className="flex h-full">
      {/* 목록 패널 */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* 헤더 + 검색/필터 */}
        <div className="bg-white border-b border-gray-200 px-6 py-4">
          <h2 className="text-base font-semibold text-gray-800 mb-3">상담 이력</h2>
          <form onSubmit={handleSearch} className="flex flex-wrap gap-2">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="키워드 검색"
              className="flex-1 min-w-40 px-3 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-300"
            />
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-300 bg-white"
            >
              <option value="">전체 카테고리</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-300 bg-white"
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </form>
        </div>

        {/* 목록 */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3">
          {loading ? (
            <div className="text-center py-12 text-gray-400">불러오는 중...</div>
          ) : items.length === 0 ? (
            <div className="text-center py-12 text-gray-400">검색 결과가 없습니다</div>
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
                  className="w-full py-2.5 text-sm text-primary-600 hover:bg-primary-50 border border-primary-200 rounded-lg transition-colors disabled:opacity-50"
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
          {/* 상세 헤더 */}
          <div className="px-5 py-4 border-b border-gray-200 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-800 truncate flex-1">
              {selected ? selected.title : '불러오는 중...'}
            </h3>
            <button
              onClick={() => setSelected(null)}
              className="ml-2 text-gray-400 hover:text-gray-600 text-lg leading-none"
            >
              ×
            </button>
          </div>

          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
            {detailLoading ? (
              <div className="text-center py-8 text-gray-400 text-sm">불러오는 중...</div>
            ) : selected ? (
              <>
                {/* AI 요약 */}
                {selected.summary && (
                  <div className="bg-primary-50 border border-primary-100 rounded-lg px-4 py-3">
                    <p className="text-xs font-semibold text-primary-600 mb-1">AI 요약</p>
                    <p className="text-sm text-gray-700 leading-relaxed">{selected.summary}</p>
                  </div>
                )}

                {/* 메타 정보 */}
                <div className="text-xs text-gray-500 space-y-1">
                  {selected.categoryName && (
                    <span className="inline-block bg-primary-50 text-primary-600 px-2 py-0.5 rounded-pill font-medium mr-2">
                      {selected.categoryName}
                    </span>
                  )}
                  <span>{new Date(selected.createdAt).toLocaleString('ko-KR')}</span>
                </div>

                {/* 대화 내용 */}
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
