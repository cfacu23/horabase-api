import type { Status } from '../types'

const labels: Record<string, string> = { OPEN: 'Abierta', CLOSED: 'Cerrada', CANCELLED: 'Cancelada', SCHEDULED: 'Programado', PENDING: 'Pendiente', APPROVED: 'Aprobada', REJECTED: 'Rechazada', PAID: 'Pagada', DETECTED: 'Detectada', CONFIRMED: 'Confirmada', JUSTIFIED: 'Justificada', DISMISSED: 'Descartada' }
export function StatusBadge({ status }: { status: Status }) { return <span className={`badge badge-${status.toLowerCase()}`}>{labels[status] ?? status}</span> }
