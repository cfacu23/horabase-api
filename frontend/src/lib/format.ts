import { format, formatDistanceToNowStrict, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'

export const formatDate = (value?: string) => value ? format(parseISO(value), 'dd/MM/yyyy') : '—'
export const formatDateTime = (value?: string) => value ? format(parseISO(value), "dd/MM/yyyy · HH:mm") : '—'
export const formatTime = (value?: string) => value ? format(parseISO(value), 'HH:mm') : '—'
export const relativeTime = (value?: string) => value ? formatDistanceToNowStrict(parseISO(value), { addSuffix: true, locale: es }) : 'Nunca'
export const money = (value?: number) => value == null ? '—' : new Intl.NumberFormat('es-UY', { style: 'currency', currency: 'UYU', maximumFractionDigits: 2 }).format(value)
export const minutes = (value?: number) => value == null ? '—' : `${Math.floor(value / 60)} h ${value % 60} min`
export const isoInput = (date: Date) => new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
