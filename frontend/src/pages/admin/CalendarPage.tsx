import { CalendarPlus, ChevronLeft, ChevronRight, Pencil, XCircle } from 'lucide-react'
import { useMemo, useState, type FormEvent } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Empty, Loading, Notice } from '../../components/Feedback'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useApiData } from '../../hooks/useApi'
import { api } from '../../lib/api'
import { formatDate, formatTime, isoInput } from '../../lib/format'
import type { Employee, Sector, Shift } from '../../types'

function weekStart(value: Date) {
  const date = new Date(value); const day = date.getDay() || 7
  date.setHours(0, 0, 0, 0); date.setDate(date.getDate() - day + 1)
  return date
}

export function CalendarPage() {
  const { token, user } = useAuth(); const businessId = user.businessId
  const [anchor, setAnchor] = useState(() => weekStart(new Date()))
  const [editing, setEditing] = useState<Shift | null | undefined>(undefined); const [error, setError] = useState('')
  const end = new Date(anchor); end.setDate(end.getDate() + 7)
  const path = `/api/businesses/${businessId}/shifts?from=${encodeURIComponent(anchor.toISOString())}&to=${encodeURIComponent(end.toISOString())}`
  const shifts = useApiData<Shift[]>(path, []); const employees = useApiData<Employee[]>(`/api/businesses/${businessId}/employees`, []); const sectors = useApiData<Sector[]>(`/api/businesses/${businessId}/sectors`, [])
  const days = useMemo(() => Array.from({ length: 7 }, (_, index) => { const date = new Date(anchor); date.setDate(date.getDate() + index); return date }), [anchor])
  const changeWeek = (delta: number) => { const date = new Date(anchor); date.setDate(date.getDate() + delta * 7); setAnchor(date) }
  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); const values = new FormData(event.currentTarget)
    const body = { employeeId: Number(values.get('employeeId')), sectorId: Number(values.get('sectorId')), startsAt: new Date(String(values.get('startsAt'))).toISOString(), endsAt: new Date(String(values.get('endsAt'))).toISOString(), breakMinutes: Number(values.get('breakMinutes')), notes: values.get('notes') || null, ...(editing ? { status: editing.status } : {}) }
    try { await api(`/api/businesses/${businessId}/shifts${editing ? `/${editing.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify(body) }, token); setEditing(undefined); await shifts.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar el turno') }
  }
  async function cancel(shift: Shift) {
    if (!window.confirm(`¿Cancelar el turno de ${shift.employeeName}?`)) return
    try { await api(`/api/businesses/${businessId}/shifts/${shift.id}`, { method: 'PUT', body: JSON.stringify({ employeeId: shift.employeeId, sectorId: shift.sectorId, startsAt: shift.startsAt, endsAt: shift.endsAt, breakMinutes: shift.breakMinutes, notes: shift.notes, status: 'CANCELLED' }) }, token); await shifts.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cancelar') }
  }
  const defaultStart = new Date(anchor); defaultStart.setHours(9, 0); const defaultEnd = new Date(defaultStart); defaultEnd.setHours(17)
  return <div className="page"><PageHeader eyebrow="Planificación" title="Calendario de turnos" description="Organizá la semana y mantené al equipo alineado." actions={<button className="button primary" onClick={() => setEditing(null)}><CalendarPlus size={18} /> Nuevo turno</button>} /><div className="calendar-toolbar"><button className="icon-button" onClick={() => changeWeek(-1)} aria-label="Semana anterior"><ChevronLeft /></button><strong>{formatDate(anchor.toISOString())} — {formatDate(new Date(end.getTime() - 1).toISOString())}</strong><button className="icon-button" onClick={() => changeWeek(1)} aria-label="Semana siguiente"><ChevronRight /></button><button className="button ghost" onClick={() => setAnchor(weekStart(new Date()))}>Hoy</button></div>{error && <Notice message={error} />}{shifts.loading ? <Loading /> : <section className="week-grid">{days.map((day) => { const current = shifts.data.filter((shift) => new Date(shift.startsAt).toDateString() === day.toDateString()); return <article className="day-column" key={day.toISOString()}><header><span>{day.toLocaleDateString('es-UY', { weekday: 'short' })}</span><strong>{day.getDate()}</strong></header><div>{current.length === 0 ? <small className="muted">Sin turnos</small> : current.map((shift) => <button className={`shift-card ${shift.status === 'CANCELLED' ? 'cancelled' : ''}`} key={shift.id} onClick={() => setEditing(shift)}><span>{formatTime(shift.startsAt)} – {formatTime(shift.endsAt)}</span><strong>{shift.employeeName}</strong><small>{shift.sectorName}</small><StatusBadge status={shift.status} /></button>)}</div></article> })}</section>}{shifts.data.length === 0 && !shifts.loading && <Empty title="Semana disponible" text="Todavía no se asignaron turnos en este período." />}{editing !== undefined && <Modal title={editing ? 'Editar turno' : 'Nuevo turno'} onClose={() => setEditing(undefined)}><form className="form-grid" onSubmit={save}>{error && <div className="full"><Notice message={error} /></div>}<label>Empleado<select name="employeeId" defaultValue={editing?.employeeId} required>{employees.data.filter((item) => item.active).map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label><label>Sector<select name="sectorId" defaultValue={editing?.sectorId} required>{sectors.data.filter((item) => item.active).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Inicio<input name="startsAt" type="datetime-local" defaultValue={editing ? isoInput(new Date(editing.startsAt)) : isoInput(defaultStart)} required /></label><label>Fin<input name="endsAt" type="datetime-local" defaultValue={editing ? isoInput(new Date(editing.endsAt)) : isoInput(defaultEnd)} required /></label><label>Descanso (minutos)<input name="breakMinutes" type="number" min="0" max="1440" defaultValue={editing?.breakMinutes ?? 0} required /></label><label className="full">Notas<textarea name="notes" defaultValue={editing?.notes} rows={3} /></label><div className="modal-actions full">{editing && editing.status !== 'CANCELLED' && <button type="button" className="button danger" onClick={() => { void cancel(editing); setEditing(undefined) }}><XCircle size={17} /> Cancelar turno</button>}<button type="button" className="button ghost" onClick={() => setEditing(undefined)}>Cerrar</button><button className="button primary"><Pencil size={17} /> Guardar</button></div></form></Modal>}</div>
}
