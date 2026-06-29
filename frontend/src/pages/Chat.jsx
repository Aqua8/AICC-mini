import { useState, useRef, useEffect } from 'react'
import { createRoom, endRoom } from '../api/chat'
import { useWebSocket } from '../hooks/useWebSocket'
import MessageBubble, { TypingIndicator } from '../components/MessageBubble'

export default function Chat() {
  const [room, setRoom] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [aiTyping, setAiTyping] = useState(false)
  const [loading, setLoading] = useState(false)
  const [ending, setEnding] = useState(false)
  const [error, setError] = useState('')
  const bottomRef = useRef(null)

  // 백엔드가 USER 메시지도 브로드캐스트하므로 AI 메시지만 처리
  // (USER 메시지는 handleSend에서 낙관적 UI로 이미 표시됨)
  const handleWsMessage = (msg) => {
    if (msg.sender === 'AI') {
      setAiTyping(false)
      setMessages((prev) => [...prev, msg])
    }
  }

  const { sendMessage } = useWebSocket(room?.id ?? null, handleWsMessage)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, aiTyping])

  const handleStartChat = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await createRoom()
      setRoom(data)
      setMessages(data.messages ?? [])
    } catch {
      setError('채팅방 생성에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleSend = () => {
    const content = input.trim()
    if (!content || !room) return

    const userMsg = { sender: 'USER', content, createdAt: new Date().toISOString() }
    setMessages((prev) => [...prev, userMsg])
    setInput('')
    setAiTyping(true)
    sendMessage(content)
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleEndChat = async () => {
    if (!room) return
    setEnding(true)
    try {
      const data = await endRoom(room.id)
      setRoom(data)
      setMessages(data.messages ?? messages)
    } catch {
      setError('상담 종료에 실패했습니다.')
    } finally {
      setEnding(false)
    }
  }

  const isClosed = room?.status === 'CLOSED'

  return (
    <div className="flex flex-col h-full">
      {/* 헤더 */}
      <div className="page-header flex items-center justify-between">
        <div>
          <h2 className="page-title">AI 채팅 상담</h2>
          {room && (
            <p className="page-subtitle">
              상담 #{room.id} &nbsp;·&nbsp;
              <span className={isClosed ? 'text-gray-400' : 'text-success-600'}>
                {isClosed ? '종료됨' : '진행중'}
              </span>
            </p>
          )}
        </div>
        {room && !isClosed && (
          <button onClick={handleEndChat} disabled={ending} className="btn-danger btn-sm">
            {ending ? '종료 중...' : '상담 종료'}
          </button>
        )}
      </div>

      {/* 채팅 메시지 영역 */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {!room ? (
          <div className="flex flex-col items-center justify-center h-full gap-4">
            <div className="text-center">
              <div className="text-4xl mb-3">💬</div>
              <p className="text-gray-600 font-medium">AI 상담을 시작해보세요</p>
              <p className="text-sm text-gray-400 mt-1">궁금한 점을 Gemini AI가 도와드립니다</p>
            </div>
            {error && <p className="text-sm text-error-600">{error}</p>}
            <button onClick={handleStartChat} disabled={loading} className="btn-primary btn-lg">
              {loading ? '연결 중...' : '새 상담 시작'}
            </button>
          </div>
        ) : (
          <>
            {messages.map((msg, i) => (
              <MessageBubble
                key={msg.id ?? i}
                sender={msg.sender}
                content={msg.content}
                createdAt={msg.createdAt}
              />
            ))}
            {aiTyping && <TypingIndicator />}
            {isClosed && (
              <div className="text-center py-4">
                <span className="inline-block text-xs text-gray-400 bg-gray-100 px-4 py-1.5 rounded-pill">
                  상담이 종료되었습니다
                </span>
              </div>
            )}
            <div ref={bottomRef} />
          </>
        )}
      </div>

      {/* 입력 영역 */}
      {room && !isClosed && (
        <div className="bg-white border-t border-gray-100 px-4 py-3">
          <div className="flex items-end gap-2 bg-gray-50 border border-gray-200 rounded-xl px-3 py-2">
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="메시지를 입력하세요... (Enter로 전송)"
              rows={1}
              className="flex-1 bg-transparent text-sm text-gray-800 placeholder-gray-400 resize-none outline-none max-h-32"
              style={{ minHeight: '24px' }}
            />
            <button
              onClick={handleSend}
              disabled={!input.trim()}
              className="p-2 bg-primary-500 hover:bg-primary-600 text-white rounded-lg transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          </div>
          <p className="text-xs text-gray-400 mt-1 px-1">Shift+Enter로 줄바꿈</p>
        </div>
      )}

      {room && !isClosed && error && (
        <p className="text-xs text-error-600 px-6 pb-2">{error}</p>
      )}
    </div>
  )
}
