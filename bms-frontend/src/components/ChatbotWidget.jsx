import { useState, useEffect, useRef } from 'react';
import { chatAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './ChatbotWidget.css';

export default function ChatbotWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [sessionId, setSessionId] = useState(null);
  const [inputVal, setInputVal] = useState('');
  const [typing, setTyping] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (isOpen && !sessionId) {
      initChat();
    }
  }, [isOpen]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, typing]);

  const initChat = async () => {
    setTyping(true);
    try {
      const res = await chatAPI.start(user?.id);
      setSessionId(res.data.sessionId);
      setMessages([res.data.message]);
    } catch (err) {
      setMessages([{ role: 'assistant', content: 'Could not connect to CineBot.' }]);
    }
    setTyping(false);
  };

  const handleSend = async (text) => {
    if (!text.trim() || !sessionId) return;
    
    // add user message
    const newMsgs = [...messages, { role: 'user', content: text }];
    setMessages(newMsgs);
    setInputVal('');
    setTyping(true);

    try {
      const res = await chatAPI.sendMessage({ sessionId, message: text, userId: user?.id });
      setMessages(prev => [...prev, res.data]);
    } catch (err) {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Something went wrong.' }]);
    }
    setTyping(false);
  };

  const handleQuickOption = (opt) => {
    if (opt === 'Login') navigate('/login');
    else if (opt.includes('View Cart')) {
      const lastMsg = messages[messages.length - 1];
      if (lastMsg?.cartId) navigate(`/cart/${lastMsg.cartId}`);
    }
    else if (opt === 'Start new booking' || opt === 'Start over') {
      setSessionId(null);
      setMessages([]);
      initChat();
    }
    else handleSend(opt);
  };

  return (
    <div className="chatbot-container">
      {isOpen && (
        <div className="chatbot-window glass-card">
          <div className="chat-header">
            <div className="chat-bot-info">
              <span className="bot-avatar-svg">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 8V4H8"></path><rect width="16" height="12" x="4" y="8" rx="2"></rect><path d="M2 14h2"></path><path d="M20 14h2"></path><path d="M15 13v2"></path><path d="M9 13v2"></path></svg>
              </span>
              <div>
                <h4>CineBot AI</h4>
                <small className="typing-status">{typing ? 'CineBot is thinking...' : 'Online and ready'}</small>
              </div>
            </div>
            <button className="chat-close" onClick={() => setIsOpen(false)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"></path><path d="m6 6 12 12"></path></svg>
            </button>
          </div>

          <div className="chat-body">
            {messages.map((msg, idx) => (
              <div key={idx} className={`chat-bubble-wrap ${msg.role}`}>
                {msg.role === 'assistant' && (
                  <span className="bubble-avatar-svg">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="16" height="12" x="4" y="8" rx="2"></rect><path d="M12 8V4H8"></path><path d="M9 13v2"></path><path d="M15 13v2"></path></svg>
                  </span>
                )}
                <div className="chat-bubble-content">
                  <div className={`chat-bubble ${msg.role}`}>
                    {msg.content.split('\n').map((line, i) => (
                      <p key={i}>{line.includes('**') ? <strong dangerouslySetInnerHTML={{__html: line.replace(/\*\*/g, '')}}></strong> : line}</p>
                    ))}
                  </div>
                  {msg.quickOptions && msg.quickOptions.length > 0 && !typing && idx === messages.length - 1 && (
                    <div className="quick-options">
                      {msg.quickOptions.map(opt => (
                        <button key={opt} className="quick-opt-btn" onClick={() => handleQuickOption(opt)}>
                          {opt}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
            {typing && (
              <div className="chat-bubble-wrap assistant">
                 <span className="bubble-avatar-svg">
                   <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="16" height="12" x="4" y="8" rx="2"></rect><path d="M12 8V4H8"></path><path d="M9 13v2"></path><path d="M15 13v2"></path></svg>
                 </span>
                 <div className="chat-bubble typing-dots"><span></span><span></span><span></span></div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <form className="chat-footer" onSubmit={(e) => { e.preventDefault(); handleSend(inputVal); }}>
            <input 
              value={inputVal} 
              onChange={e => setInputVal(e.target.value)}
              placeholder="Type your message..." 
              className="chat-input"
              disabled={typing}
            />
            <button type="submit" className="chat-send" disabled={!inputVal.trim() || typing}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m22 2-7 20-4-9-9-4Z"></path><path d="M22 2 11 13"></path></svg>
            </button>
          </form>
        </div>
      )}

      <button className={`chatbot-toggle ${isOpen ? 'active' : ''}`} onClick={() => setIsOpen(!isOpen)}>
        {isOpen ? (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"></path><path d="m6 6 12 12"></path></svg>
        ) : (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m3 21 1.9-5.7a8.5 8.5 0 1 1 3.8 3.8z"></path></svg>
        )}
      </button>
    </div>
  );
}
