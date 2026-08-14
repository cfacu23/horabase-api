import { CalendarDays, Clock3, KeyRound, Plus } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { Empty, Loading, Notice } from '../../components/Feedback'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useApiData } from '../../hooks/useApi'
import { api } from '../../lib/api'
import { formatDate, formatDateTime, minutes } from '../../lib/format'
import type { Attendance, EmployeeProfile, EmployeeRequest, Incident, Overtime, Shift } from '../../types'

const range = (pastDays: number, futureDays = 1) => { const from = new Date(); const to = new Date(); from.setDate(from.getDate() - pastDays); to.setDate(to.getDate() + futureDays); return { from, to } }

export function EmployeeHomePage() {
  const profile = useApiData<EmployeeProfile>('/api/me/profile', {} as EmployeeProfile)
  if (profile.loading) return <Loading />; if (profile.error) return <Notice message={profile.error} />
  const data = profile.data; const next = data.upcomingShifts[0]
  return <div className="page"><PageHeader eyebrow="Mi jornada" title={`Hola, ${data.employee.firstName}`} description="Acá tenés tu agenda y novedades de trabajo." /><section className="employee-welcome"><div><span>Próximo turno</span>{next ? <><h2>{formatDateTime(next.startsAt)}</h2><p>{next.sectorName} · {minutes(next.plannedWorkMinutes)}</p></> : <><h2>Sin turnos próximos</h2><p>Tu agenda está libre por ahora.</p></>}</div><CalendarDays /></section><div className="mini-metrics"><div><span>Turnos próximos</span><strong>{data.upcomingShifts.length}</strong></div><div><span>Solicitudes pendientes</span><strong>{data.requests.filter((item) => item.status === 'PENDING').length}</strong></div><div><span>Horas semanales</span><strong>{data.employee.weeklyHours} h</strong></div></div><div className="two-columns"><section className="panel"><div className="panel-head"><h2>Próximos turnos</h2><Link to="/mi/turnos">Ver agenda</Link></div>{data.upcomingShifts.slice(0, 4).map((shift) => <div className="simple-row" key={shift.id}><div><strong>{formatDateTime(shift.startsAt)}</strong><small>{shift.sectorName}</small></div><StatusBadge status={shift.status} /></div>)}</section><section className="panel"><div className="panel-head"><h2>Solicitudes recientes</h2><Link to="/mi/solicitudes">Ver todas</Link></div>{data.requests.slice(0, 4).map((request) => <div className="simple-row" key={request.id}><div><strong>{request.title}</strong><small>{formatDate(request.relatedDate)}</small></div><StatusBadge status={request.status} /></div>)}</section></div></div>
}

export function MyShiftsPage() {
  const period = range(14, 60); const shifts = useApiData<Shift[]>(`/api/me/calendar?from=${encodeURIComponent(period.from.toISOString())}&to=${encodeURIComponent(period.to.toISOString())}`, [])
  return <div className="page"><PageHeader eyebrow="Mi agenda" title="Mis turnos" description="Consultá tus horarios pasados y próximos." />{shifts.loading ? <Loading /> : shifts.data.length === 0 ? <Empty title="Sin turnos" text="No hay horarios asignados en este período." /> : <div className="schedule-list">{shifts.data.map((shift) => <article key={shift.id}><div className="schedule-date"><strong>{new Date(shift.startsAt).getDate()}</strong><span>{new Date(shift.startsAt).toLocaleDateString('es-UY', { month: 'short' })}</span></div><div><h3>{formatDateTime(shift.startsAt).split('·')[1]} – {new Date(shift.endsAt).toLocaleTimeString('es-UY', { hour: '2-digit', minute: '2-digit' })}</h3><p>{shift.sectorName} · descanso {shift.breakMinutes} min</p></div><StatusBadge status={shift.status} /></article>)}</div>}</div>
}

export function MyAttendancesPage() {
  const period = range(90); const records = useApiData<Attendance[]>(`/api/me/attendances?from=${encodeURIComponent(period.from.toISOString())}&to=${encodeURIComponent(period.to.toISOString())}`, [])
  return <div className="page"><PageHeader eyebrow="Registro horario" title="Mis asistencias" description="Historial de entradas, salidas y horas trabajadas." />{records.loading ? <Loading /> : records.data.length === 0 ? <Empty title="Sin asistencias" text="Tus marcaciones aparecerán acá." /> : <div className="table-wrap"><table><thead><tr><th>Entrada</th><th>Salida</th><th>Tiempo</th><th>Estado</th></tr></thead><tbody>{records.data.map((item) => <tr key={item.id}><td>{formatDateTime(item.checkInAt)}</td><td>{formatDateTime(item.checkOutAt)}</td><td>{minutes(item.workedMinutes)}</td><td><StatusBadge status={item.status} /></td></tr>)}</tbody></table></div>}</div>
}

export function MyIncidentsPage() {
  const period = range(180); const records = useApiData<Incident[]>(`/api/me/incidents?from=${period.from.toISOString().slice(0, 10)}&to=${period.to.toISOString().slice(0, 10)}`, [])
  return <div className="page"><PageHeader eyebrow="Excepciones" title="Mis incidencias" description="Consultá ausencias, llegadas tarde y su resolución." />{records.loading ? <Loading /> : records.data.length === 0 ? <Empty title="Sin incidencias" text="No tenés excepciones registradas en los últimos seis meses." /> : <div className="card-grid">{records.data.map((item) => <article className="entity-card" key={item.id}><div className="entity-icon">!</div><div><h3>{item.type.replaceAll('_', ' ')}</h3><p>{formatDate(item.incidentDate)} · {minutes(item.detectedMinutes)}</p>{item.notes && <p>{item.notes}</p>}</div><StatusBadge status={item.status} /></article>)}</div>}</div>
}

export function MyOvertimePage() {
  const period = range(180); const records = useApiData<Overtime[]>(`/api/me/overtime?from=${period.from.toISOString().slice(0, 10)}&to=${period.to.toISOString().slice(0, 10)}`, [])
  return <div className="page"><PageHeader eyebrow="Compensaciones" title="Mis horas extra" description="Seguimiento de tiempo adicional y sus aprobaciones." />{records.loading ? <Loading /> : records.data.length === 0 ? <Empty title="Sin horas extra" text="No hay registros en los últimos seis meses." /> : <div className="card-grid">{records.data.map((item) => <article className="entity-card" key={item.id}><div className="entity-icon"><Clock3 /></div><div><h3>{formatDate(item.workDate)}</h3><p>{minutes(item.approvedMinutes ?? item.detectedMinutes)} · {item.notes || 'Sin notas'}</p></div><StatusBadge status={item.status} /></article>)}</div>}</div>
}

export function MyRequestsPage() {
  const { token } = useAuth(); const requests = useApiData<EmployeeRequest[]>('/api/me/requests', []); const [open, setOpen] = useState(false); const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); const data = new FormData(event.currentTarget); setError(''); try { await api('/api/me/requests', { method: 'POST', body: JSON.stringify({ type: data.get('type'), title: data.get('title'), description: data.get('description'), relatedDate: data.get('relatedDate') || null }) }, token); setOpen(false); await requests.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo enviar') } }
  async function cancel(id: number) { try { await api(`/api/me/requests/${id}/cancel`, { method: 'POST' }, token); await requests.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo cancelar') } }
  return <div className="page"><PageHeader eyebrow="Comunicación" title="Mis solicitudes" description="Pedí cambios de horario, licencias o correcciones." actions={<button className="button primary" onClick={() => setOpen(true)}><Plus size={18} /> Nueva solicitud</button>} />{error && <Notice message={error} />}{requests.loading ? <Loading /> : requests.data.length === 0 ? <Empty title="Sin solicitudes" text="Cuando necesites algo, iniciá una solicitud." /> : <div className="request-list">{requests.data.map((item) => <article className="request-card" key={item.id}><div className="request-top"><div><span className="eyebrow">{item.type.replaceAll('_', ' ')}</span><h3>{item.title}</h3></div><StatusBadge status={item.status} /></div><p>{item.description}</p>{item.adminResponse && <blockquote>{item.adminResponse}</blockquote>}{item.status === 'PENDING' && <button className="button ghost" onClick={() => cancel(item.id)}>Cancelar solicitud</button>}</article>)}</div>}{open && <Modal title="Nueva solicitud" onClose={() => setOpen(false)}><form className="form-stack" onSubmit={submit}>{error && <Notice message={error} />}<label>Tipo<select name="type"><option value="SHIFT_CHANGE">Cambio de turno</option><option value="LEAVE">Licencia</option><option value="ATTENDANCE_CORRECTION">Corrección de asistencia</option><option value="OTHER">Otro</option></select></label><label>Título<input name="title" required maxLength={150} /></label><label>Descripción<textarea name="description" rows={4} required /></label><label>Fecha relacionada (opcional)<input name="relatedDate" type="date" /></label><div className="modal-actions"><button type="button" className="button ghost" onClick={() => setOpen(false)}>Cancelar</button><button className="button primary">Enviar</button></div></form></Modal>}</div>
}

export function MyProfilePage() {
  const profile = useApiData<EmployeeProfile>('/api/me/profile', {} as EmployeeProfile)
  if (profile.loading) return <Loading />; if (profile.error) return <Notice message={profile.error} />; const employee = profile.data.employee
  return <div className="page"><PageHeader eyebrow="Cuenta" title="Mi perfil" description="Tus datos personales y condiciones laborales." /><section className="profile-hero panel"><div className="avatar xl">{employee.firstName[0]}{employee.lastName[0]}</div><div><h2>{employee.firstName} {employee.lastName}</h2><p>{employee.email}</p><p>{employee.phone || 'Sin teléfono registrado'}</p></div><div className="profile-facts"><span>Sector: {employee.sectorName}</span><span>Ingreso: {formatDate(employee.hireDate)}</span><span>Documento: {employee.document}</span></div></section><section className="panel"><div className="panel-head"><div><span className="eyebrow">Seguridad</span><h2>Contraseña</h2></div><Link className="button soft" to="/cambiar-contrasena"><KeyRound size={17} /> Cambiar</Link></div><p className="muted">Usá una contraseña única que no compartas con otros servicios.</p></section></div>
}
