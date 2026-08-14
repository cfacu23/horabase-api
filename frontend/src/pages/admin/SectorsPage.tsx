import { Pencil, Plus } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Empty, Loading, Notice } from '../../components/Feedback'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useApiData } from '../../hooks/useApi'
import { api } from '../../lib/api'
import type { Sector } from '../../types'

export function SectorsPage() {
  const { token, user } = useAuth(); const path = `/api/businesses/${user.businessId}/sectors`; const sectors = useApiData<Sector[]>(path, []); const [editing, setEditing] = useState<Sector | null | undefined>(undefined); const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setError(''); const data = new FormData(event.currentTarget); const body = { name: data.get('name'), description: data.get('description') || null, ...(editing ? { active: data.get('active') === 'on' } : {}) }; try { await api(`${path}${editing ? `/${editing.id}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify(body) }, token); setEditing(undefined); await sectors.reload() } catch (reason) { setError(reason instanceof Error ? reason.message : 'No se pudo guardar') } }
  return <div className="page"><PageHeader eyebrow="Organización" title="Sectores" description="Ordená equipos y responsabilidades dentro del comercio." actions={<button className="button primary" onClick={() => setEditing(null)}><Plus size={18} /> Nuevo sector</button>} />{sectors.loading ? <Loading /> : sectors.data.length === 0 ? <Empty title="Sin sectores" text="Creá el primero para poder incorporar empleados." /> : <div className="card-grid">{sectors.data.map((sector) => <article className="entity-card" key={sector.id}><div className="entity-icon">{sector.name.slice(0, 2).toUpperCase()}</div><div><h3>{sector.name}</h3><p>{sector.description || 'Sin descripción'}</p></div><StatusBadge status={sector.active ? 'CONFIRMED' : 'CANCELLED'} /><button className="icon-button" onClick={() => setEditing(sector)} aria-label="Editar"><Pencil size={17} /></button></article>)}</div>}{editing !== undefined && <Modal title={editing ? 'Editar sector' : 'Nuevo sector'} onClose={() => setEditing(undefined)}><form className="form-stack" onSubmit={submit}>{error && <Notice message={error} />}<label>Nombre<input name="name" defaultValue={editing?.name} maxLength={80} required /></label><label>Descripción<textarea name="description" defaultValue={editing?.description} rows={3} maxLength={250} /></label>{editing && <label className="check"><input name="active" type="checkbox" defaultChecked={editing.active} /> Sector activo</label>}<div className="modal-actions"><button type="button" className="button ghost" onClick={() => setEditing(undefined)}>Cancelar</button><button className="button primary">Guardar</button></div></form></Modal>}</div>
}
