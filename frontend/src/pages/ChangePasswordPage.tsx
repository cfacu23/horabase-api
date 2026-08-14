import { KeyRound } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Notice } from '../components/Feedback'
import { api } from '../lib/api'

export function ChangePasswordPage() {
  const { token, user, markPasswordChanged } = useAuth(); const navigate = useNavigate(); const [currentPassword, setCurrent] = useState(''); const [newPassword, setNext] = useState(''); const [confirm, setConfirm] = useState(''); const [message, setMessage] = useState(''); const [error, setError] = useState('')
  async function submit(event: FormEvent) { event.preventDefault(); setError(''); if (newPassword !== confirm) { setError('Las nuevas contraseñas no coinciden'); return } try { await api('/api/auth/change-password', { method: 'POST', body: JSON.stringify({ currentPassword, newPassword }), headers: { 'Content-Type': 'application/json' } }, token); markPasswordChanged(); setMessage('Contraseña actualizada'); setTimeout(() => navigate(user.role === 'ADMIN' ? '/admin' : '/mi'), 500) } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo actualizar') } }
  return <main className="standalone-page"><form className="auth-card" onSubmit={submit}><div className="round-icon"><KeyRound /></div><div><span className="eyebrow">Seguridad</span><h2>{user.mustChangePassword ? 'Creá tu contraseña definitiva' : 'Cambiar contraseña'}</h2><p>{user.mustChangePassword ? 'La contraseña temporal debe reemplazarse antes de continuar.' : 'Actualizá tu acceso cuando lo necesites.'}</p></div>{error && <Notice message={error} />}{message && <Notice message={message} kind="success" />}<label>Contraseña actual<input type="password" required value={currentPassword} onChange={(event) => setCurrent(event.target.value)} /></label><label>Nueva contraseña<input type="password" minLength={8} required value={newPassword} onChange={(event) => setNext(event.target.value)} /></label><label>Repetir nueva contraseña<input type="password" minLength={8} required value={confirm} onChange={(event) => setConfirm(event.target.value)} /></label><button className="button primary wide">Guardar contraseña</button></form></main>
}
