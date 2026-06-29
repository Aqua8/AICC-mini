/**
 * 채팅 말풍선 컴포넌트
 * sender: "USER" | "AI"
 */
export default function MessageBubble({ sender, content, createdAt }) {
  const isUser = sender === 'USER'

  return (
    <div className={`flex items-end gap-2 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      {/* 아바타 */}
      {!isUser && (
        <div className="w-8 h-8 rounded-full bg-primary-500 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
          AI
        </div>
      )}

      <div className={`flex flex-col gap-1 max-w-[70%] ${isUser ? 'items-end' : 'items-start'}`}>
        <div
          className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap break-words ${
            isUser
              ? 'bg-primary-500 text-white rounded-br-sm'
              : 'bg-white text-gray-800 border border-gray-200 rounded-bl-sm shadow-xs'
          }`}
        >
          {content}
        </div>
        {createdAt && (
          <span className="text-xs text-gray-400">
            {new Date(createdAt).toLocaleTimeString('ko-KR', {
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        )}
      </div>
    </div>
  )
}

/** AI 응답 대기 로딩 인디케이터 */
export function TypingIndicator() {
  return (
    <div className="flex items-end gap-2">
      <div className="w-8 h-8 rounded-full bg-primary-500 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
        AI
      </div>
      <div className="bg-white border border-gray-200 rounded-2xl rounded-bl-sm px-4 py-3 shadow-xs">
        <div className="flex gap-1">
          <span className="w-2 h-2 rounded-full bg-gray-400 animate-dot-1" />
          <span className="w-2 h-2 rounded-full bg-gray-400 animate-dot-2" />
          <span className="w-2 h-2 rounded-full bg-gray-400 animate-dot-3" />
        </div>
      </div>
    </div>
  )
}
