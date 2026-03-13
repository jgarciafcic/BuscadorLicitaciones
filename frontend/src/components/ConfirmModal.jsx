import { useEffect } from 'react';
import './ConfirmModal.css';

function ConfirmModal({ title, message, onConfirm, onCancel, confirmLabel = 'Aceptar', cancelLabel = 'Cancelar', danger = false }) {
  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') onCancel();
      if (e.key === 'Enter') onConfirm();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onConfirm, onCancel]);

  return (
    <>
      <div className="confirm-overlay" onClick={onCancel} />
      <div className="confirm-modal">
        {title && <h3 className="confirm-title">{title}</h3>}
        <p className="confirm-message">{message}</p>
        <div className="confirm-actions">
          <button className="confirm-btn-cancel" onClick={onCancel}>{cancelLabel}</button>
          <button className={`confirm-btn-ok${danger ? ' confirm-btn-danger' : ''}`} onClick={onConfirm}>{confirmLabel}</button>
        </div>
      </div>
    </>
  );
}

export default ConfirmModal;
