import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '../store/authStore'

/**
 * STOMP over SockJS WebSocket 훅
 * @param {number|null} roomId - 구독할 채팅방 ID. null이면 연결 안 함.
 * @param {function} onMessage - 메시지 수신 콜백 (parsed JSON)
 * @param {function} [onReconnect] - 끊겼다가 다시 연결됐을 때 호출 (끊긴 사이 놓친 메시지 동기화용)
 */
export function useWebSocket(roomId, onMessage, onReconnect) {
  const clientRef = useRef(null)
  const onMessageRef = useRef(onMessage)
  onMessageRef.current = onMessage
  const onReconnectRef = useRef(onReconnect)
  onReconnectRef.current = onReconnect

  useEffect(() => {
    if (!roomId) return

    const token = getToken()
    let connectedBefore = false

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws/chat'),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/rooms/${roomId}`, (frame) => {
          try {
            const data = JSON.parse(frame.body)
            onMessageRef.current(data)
          } catch (e) {
            console.error('WS message parse error', e)
          }
        })
        // 구독을 마친 뒤에 동기화해야 그 사이의 메시지를 놓치지 않는다
        if (connectedBefore) onReconnectRef.current?.()
        connectedBefore = true
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame)
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [roomId])

  const sendMessage = useCallback((content) => {
    const client = clientRef.current
    if (!client?.connected) return
    client.publish({
      destination: '/app/chat',
      body: JSON.stringify({ roomId, content }),
    })
  }, [roomId])

  return { sendMessage }
}
