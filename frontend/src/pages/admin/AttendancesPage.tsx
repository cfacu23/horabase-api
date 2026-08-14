import { ClockArrowDown, ClockArrowUp, Plus } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Empty, Loading, Notice } from '../../components/Feedback'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useApiData } from '../../hooks/useApi'
import { api } from '../../lib/api'
import { formatDateTime, isoInput, minutes } from '../../lib/format'
import type { Attendance, Employee } from '../../types'

export function AttendancesPage() {
  const { token, user } = useAuth(); const businessId = user.businessId; const [days, setDays] = useState(30); const [modal, setModal] = useState<'create' | 'in' | 'out' | null>(null); const [selected, setSelected] = useState<Attendance | null>(null); const [error, setError] = useState('')
  const to = new Date(); const from = new Date(); from.setDate(from.getDate() - days)
  const attendances = useApiData<Attendance[]>(`/api/businesses/${businessId}/attendances?from=${encodeURIComponent(from.toISOString())}&to=${encodeURIComponent(to.toISOString())}`, []); const employees = useApiData<Employee[]>(`/api/businesses/${businessId}/employees`, [])
  function openCorrection(item: Attendance, type: 'in' | 'out') { setSelected(item); setModal(type); setError('') }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); const values = new FormData(event.currentTarget)
    try {
      if (modal === 'create') await api(`/api/businesses/${businessId}/attendances`, { method: 'POST', body: JSON.stringify({ employeeId: Number(values.get('employeeId')), checkInAt: new Date(String(values.get('checkInAt'))).toISOString(), checkOutAt: values.get('checkOutAt') ? new Date(String(values.get('checkOutAt'))).toISOString() : null, notes: values.get('notes') || null }) }, token)
      else await api(`/api/businesses/${businessId}/attendances/${selected?.id}/check-${modal}`, { method: 'PATCH', body: JSON.stringify({ occurredAt: new Date(String(values.get('occurredAt'))).toISOString(), reason: values.get('reason') }) }, token)
      setModal(null); await attendances.reload()
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar la asistencia') }
  }
  return <div className="page"><PageHeader eyebrow="Control horario" title="Asistencias" description="Revisá marcaciones y corregí excepciones con trazabilidad." actions={<button className="button primary" onClick={() => { setSelected(null); setModal('create') }}><Plus size={18} /> Alta manual</button>} /><div className="toolbar"><label>Período <select value={days} onChange={(event) => setDays(Number(event.target.value))}><option value="7">7 días</option><option value="30">30 días</option><option value="90">90 días</option></select></label><span>{attendances.data.length} registros</span></div>{error && <Notice message={error} />}{attendances.loading ? <Loading /> : attendances.data.length === 0 ? <Empty title="Sin marcaciones" text="No hay asistencias para el período seleccionado." /> : <div className="table-wrap"><table><thead><tr><th>Empleado</th><th>Entrada</th><th>Salida</th><th>Trabajado</th><th>Origen</th><th>Estado</th><th></th></tr></thead><tbody>{attendances.data.map((item) => <tr key={item.id}><td><strong>{item.employeeName}</strong></td><td>{formatDateTime(item.checkInAt)}</td><td>{formatDateTime(item.checkOutAt)}</td><td>{minutes(item.workedMinutes)}</td><td>{item.origin === 'TERMINAL' ? 'Terminal' : 'Manual'}</td><td><StatusBadge status={item.status} /></td><td className="row-actions"><button className="icon-button" onClick={() => openCorrection(item, 'in')} title="Corregir entrada"><ClockArrowDown size={17} /></button><button className="icon-button" onClick={() => openCorrection(item, 'out')} title="Corregir salida"><ClockArrowUp size={17} /></button></td></tr>)}</tbody></table></div>}{modal && <Modal title={modal === 'create' ? 'Asistencia manual' : `Corregir ${modal === 'in' ? 'entrada' : 'salida'}`} onClose={() => setModal(null)}><form className="form-grid" onSubmit={submit}>{error && <div className="full"><Notice message={error} /></div>}{modal === 'create' ? <><label className="full">Empleado<select name="employeeId" required>{employees.data.filter((item) => item.active).map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label><label>Entrada<input name="checkInAt" type="datetime-local" defaultValue={isoInput(new Date())} required /></label><label>Salida opcional<input name="checkOutAt" type="datetime-local" /></label><label className="full">Observaciones<textarea name="notes" rows={3} /></label></> : <><label className="full">Nueva fecha y hora<input name="occurredAt" type="datetime-local" defaultValue={isoInput(new Date(modal === 'in' ? selected!.checkInAt : selected!.checkOutAt ?? new Date()))} required /></label><label className="full">Motivo de la corrección<textarea name="reason" rows={3} required /></label></>}<div className="modal-actions full"><button type="button" className="button ghost" onClick={() => setModal(null)}>Cancelar</button><button className="button primary">Guardar cambios</button></div></form></Modal>}</div>
}
