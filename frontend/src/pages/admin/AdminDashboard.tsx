import { CalendarClock, ClipboardCheck, ClockAlert, Timer, UserCheck, UsersRound, type LucideIcon } from 'lucide-react'
import { useMemo } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { Loading, Notice } from '../../components/Feedback'
import { PageHeader } from '../../components/PageHeader'
import { useApiData } from '../../hooks/useApi'
import { formatDateTime } from '../../lib/format'
import type { DashboardSummary, Shift } from '../../types'

export function AdminDashboard() {
  const { user } = useAuth(); const businessId = user.businessId
  const summary = useApiData<DashboardSummary>(`/api/businesses/${businessId}/dashboard`, {} as DashboardSummary)
  const shiftPeriod = useMemo(() => { const from = new Date(); return { from: from.toISOString(), to: new Date(from.getTime() + 7 * 86400000).toISOString() } }, [])
  const shifts = useApiData<Shift[]>(`/api/businesses/${businessId}/shifts?from=${encodeURIComponent(shiftPeriod.from)}&to=${encodeURIComponent(shiftPeriod.to)}`, [])
  if (summary.loading) return <Loading />
  const metrics: Array<[LucideIcon, string, number, string]> = [
    [UsersRound, 'Empleados activos', summary.data.activeEmployees, 'Equipo habilitado'],
    [UserCheck, 'Trabajando ahora', summary.data.workingNow, 'Marcaciones abiertas'],
    [ClockAlert, 'Llegadas tarde', summary.data.lateArrivalsToday, 'Durante la jornada'],
    [ClipboardCheck, 'Solicitudes', summary.data.pendingRequests, 'Esperando resolución'],
    [Timer, 'Horas extra', summary.data.pendingOvertime, 'Pendientes de revisar'],
    [CalendarClock, 'Próximos turnos', summary.data.upcomingShifts, 'En los siguientes 7 días'],
  ]
  return <div className="page"><PageHeader eyebrow="Vista general" title="Buen día" description="Lo importante de tu equipo, en un solo lugar." /><section className="metric-grid">{metrics.map(([Icon, label, value, note]) => <article className="metric-card" key={label}><div className="metric-icon"><Icon /></div><div><span>{label}</span><strong>{value}</strong><small>{note}</small></div></article>)}</section>{summary.error && <Notice message={summary.error} />}<section className="panel"><div className="panel-head"><div><span className="eyebrow">Agenda</span><h2>Próximos turnos</h2></div><span className="muted">7 días</span></div>{shifts.loading ? <Loading /> : shifts.data.length === 0 ? <p className="muted">No hay turnos próximos.</p> : <div className="timeline-list">{shifts.data.slice(0, 6).map((shift) => <div className="timeline-item" key={shift.id}><span className="timeline-dot" /><div><strong>{shift.employeeName}</strong><small>{shift.sectorName}</small></div><time>{formatDateTime(shift.startsAt)}</time></div>)}</div>}</section></div>
}
