import { Check, X } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Empty, Loading, Notice } from '../../components/Feedback'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useApiData } from '../../hooks/useApi'
import { api } from '../../lib/api'
import { formatDate, formatDateTime } from '../../lib/format'
import type { EmployeeRequest } from '../../types'

export function AdminRequestsPage() {
  const { token, user } = useAuth(); const path = `/api/businesses/${user.businessId}/requests`; const requests = useApiData<EmployeeRequest[]>(path, []); const [selected, setSelected] = useState<EmployeeRequest | null>(null); const [decision, setDecision] = useState<'approve' | 'reject'>('approve'); const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); const response = new FormData(event.currentTarget).get('response'); try { await api(`${path}/${selected?.id}/${decision}`, { method: 'POST', body: JSON.stringify({ response }) }, token); setSelected(null); await requests.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo resolver') } }
  function open(item: EmployeeRequest, value: typeof decision) { setSelected(item); setDecision(value); setError('') }
  return <div className="page"><PageHeader eyebrow="Equipo" title="Solicitudes" description="Respondé cambios y necesidades del equipo desde un único lugar." />{requests.loading ? <Loading /> : requests.data.length === 0 ? <Empty title="Bandeja al día" text="No hay solicitudes del equipo." /> : <div className="request-list">{requests.data.map((item) => <article className="request-card" key={item.id}><div className="request-top"><div><span className="eyebrow">{item.type.replaceAll('_', ' ')}</span><h3>{item.title}</h3><p>{item.employeeName} · {formatDateTime(item.createdAt)}</p></div><StatusBadge status={item.status} /></div><p>{item.description}</p>{item.relatedDate && <small>Fecha relacionada: {formatDate(item.relatedDate)}</small>}{item.adminResponse && <blockquote>{item.adminResponse}</blockquote>}{item.status === 'PENDING' && <div className="request-actions"><button className="button soft" onClick={() => open(item, 'approve')}><Check size={17} /> Aprobar</button><button className="button ghost" onClick={() => open(item, 'reject')}><X size={17} /> Rechazar</button></div>}</article>)}</div>}{selected && <Modal title={decision === 'approve' ? 'Aprobar solicitud' : 'Rechazar solicitud'} onClose={() => setSelected(null)}><form onSubmit={submit} className="form-stack">{error && <Notice message={error} />}<p>Respondé a <strong>{selected.employeeName}</strong> sobre “{selected.title}”.</p><label>Respuesta<textarea name="response" rows={4} required /></label><div className="modal-actions"><button type="button" className="button ghost" onClick={() => setSelected(null)}>Cancelar</button><button className="button primary">Enviar respuesta</button></div></form></Modal>}</div>
}
