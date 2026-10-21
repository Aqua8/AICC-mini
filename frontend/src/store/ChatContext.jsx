import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react'
import { createRoom, endRoom, getRoom } from '../api/chat'
import { useWebSocket } from '../hooks/useWebSocket'
import { getAuth } from './authStore'

// 채팅 상태와 WebSocket 연결을 앱 레이아웃 단계에서 들고 있어서
// 상담 이력·대시보드 등 다른 화면에 다녀와도 진행 중인 상담이 유지된다.
// 새로고침하면 localStorage의 방 번호로 복원한다.
// 창을 닫으면 연결이 끊기고, 백엔드가 유예 시간 뒤 상담을 자동 종료·저장한다.

const ChatContext = createContext(null)

const storageKey = () => {
  const auth = getAuth()
  return auth ? `aicc_active_room_${auth.userId}` : null
}

const readActiveRoomId = () => {
  try {
    const key = storageKey()
    return key ? localStorage.getItem(key) : null
  } catch {
    return null
  }
}

const writeActiveRoomId = (roomId) => {
  try {
    const key = storageKey()
    if (!key) return
    if (roomId) localStorage.setItem(key, String(roomId))
    else localStorage.removeItem(key)
  } catch {
    // 저장소를 못 써도 채팅 자체는 동작한다
  }
}

export function ChatProvider({ children }) {
  const [room, setRoom] = useState(null)
  const [messages, setMessages] = useState([])
  const [aiTyping, setAiTyping] = useState(false)
  const [loading, setLoading] = useState(false)
  const [ending, setEnding] = useState(false)
  const [restoring, setRestoring] = useState(() => !!readActiveRoomId())
  const [error, setError] = useState('')

  // 백엔드가 USER 메시지도 브로드캐스트하므로 AI 메시지만 처리
  // (USER 메시지는 sendMessage에서 낙관적 UI로 이미 표시됨)
  const handleWsMessage = useCallback((msg) => {
    if (msg.sender === 'AI') {
      setAiTyping(false)
      setMessages((prev) => [...prev, msg])
    }
  }, [])

  const roomIdRef = useRef(null)
  roomIdRef.current = room?.id ?? null

  // 연결이 끊겼다 돌아오면 놓친 메시지를 서버 기준으로 다시 맞춘다
  const syncRoom = useCallback(() => {
    if (!roomIdRef.current) return
    getRoom(roomIdRef.current)
      .then((data) => {
        if (data.status !== 'ACTIVE') {
          setRoom(data)
          writeActiveRoomId(null)
        }
        const synced = data.messages ?? []
        setMessages(synced)
        setAiTyping(synced.length > 0 && synced[synced.length - 1].sender === 'USER')
      })
      .catch(() => {})
  }, [])

  const { sendMessage: wsSend } = useWebSocket(room?.id ?? null, handleWsMessage, syncRoom)
  // AI 응답이 오래 안 오면 "입력 중" 표시를 풀어 입력이 멈춘 것처럼 보이지 않게 한다
  useEffect(() => {
    if (!aiTyping) return
    const timer = setTimeout(() => setAiTyping(false), 60_000)
    return () => clearTimeout(timer)
  }, [aiTyping])

  // 새로고침 후 진행 중이던 상담 복원
  useEffect(() => {
    const savedId = readActiveRoomId()
    if (!savedId) return
    let cancelled = false
    getRoom(savedId)
      .then((data) => {
        if (cancelled) return
        if (data.status === 'ACTIVE') {
          setRoom(data)
          setMessages(data.messages ?? [])
        } else {
          writeActiveRoomId(null)
        }
      })
      .catch(() => writeActiveRoomId(null))
      .finally(() => !cancelled && setRestoring(false))
    return () => {
      cancelled = true
    }
  }, [])

  const startChat = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await createRoom()
      setRoom(data)
      setMessages(data.messages ?? [])
      setAiTyping(false)
      writeActiveRoomId(data.id)
    } catch {
      setError('채팅방 생성에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const sendMessage = (content) => {
    if (!content || !room) return
    setMessages((prev) => [...prev, { sender: 'USER', content, createdAt: new Date().toISOString() }])
    setAiTyping(true)
    wsSend(content)
  }

  const endChat = async () => {
    if (!room) return
    setEnding(true)
    try {
      const data = await endRoom(room.id)
      setRoom(data)
      setMessages(data.messages ?? messages)
      setAiTyping(false)
      writeActiveRoomId(null)
    } catch {
      setError('상담 종료에 실패했습니다.')
    } finally {
      setEnding(false)
    }
  }

  // 종료된 상담 화면을 닫고 새 상담을 시작할 수 있는 초기 상태로 되돌린다
  const resetChat = () => {
    setRoom(null)
    setMessages([])
    setAiTyping(false)
    setError('')
  }

  const value = {
    room, messages, aiTyping, loading, ending, restoring, error,
    startChat, sendMessage, endChat, resetChat,
  }
  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>
}

export function useChat() {
  return useContext(ChatContext)
}
