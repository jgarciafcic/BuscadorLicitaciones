import { useEffect } from 'react';
import './Toast.css';

function Toast({ message, type, onClose }) {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(onClose, type === 'warning' ? 10000 : 5000);
    return () => clearTimeout(timer);
  }, [message, onClose]);

  if (!message) return null;

  return (
    <div className={`toast toast-${type || 'info'}`}>
      <span>{message}</span>
      <button className="toast-close" onClick={onClose}>&#x2715;</button>
    </div>
  );
}

export default Toast;
