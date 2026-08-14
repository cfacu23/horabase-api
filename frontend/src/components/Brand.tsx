import { Clock3 } from 'lucide-react'

export function Brand({ compact = false }: { compact?: boolean }) {
  return <div className="brand"><span className="brand-mark"><Clock3 size={20} /></span>{!compact && <span>HoraBase</span>}</div>
}
