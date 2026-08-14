import { AlertCircle, CheckCircle2, LoaderCircle } from 'lucide-react'

export function Loading({ label = 'Cargando información' }: { label?: string }) { return <div className="feedback"><LoaderCircle className="spin" />{label}</div> }
export function Empty({ title, text }: { title: string; text: string }) { return <div className="empty"><div className="empty-symbol">○</div><strong>{title}</strong><p>{text}</p></div> }
export function Notice({ message, kind = 'error' }: { message: string; kind?: 'error' | 'success' }) { return <div className={`notice notice-${kind}`}>{kind === 'success' ? <CheckCircle2 /> : <AlertCircle />}<span>{message}</span></div> }
