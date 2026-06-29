import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '../store/authStore'

/**
 * STOMP over SockJS WebSocket 훅
 * @param {number|null} roomId - 구독할 채팅방 ID. null이면 연결 안 함.
 * @param {function} onMessage - 메시지 수신 콜백 (parsed JSON)
 */
export function useWebSocket(roomId, onMessage) {
  const clientRef = useRef(null)
  const onMessageRef = useRef(onMessage)
  onMessageRef.current = onMessage

  useEffect(() => {
    if (!roomId) return

    const token = getToken()

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
