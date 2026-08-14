import { CalendarDays, ChartNoAxesCombined, CircleAlert, CircleUserRound, ClipboardCheck, Clock3, DoorOpen, LayoutDashboard, LogOut, Menu, MonitorSmartphone, UsersRound, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Brand } from '../components/Brand'

const admin = [
  ['/admin', LayoutDashboard, 'Resumen'], ['/admin/empleados', UsersRound, 'Empleados'], ['/admin/calendario', CalendarDays, 'Calendario'], ['/admin/asistencias', Clock3, 'Asistencias'], ['/admin/incidencias', CircleAlert, 'Incidencias'], ['/admin/horas-extra', ChartNoAxesCombined, 'Horas extra'], ['/admin/solicitudes', ClipboardCheck, 'Solicitudes'], ['/admin/sectores', DoorOpen, 'Sectores'], ['/admin/terminales', MonitorSmartphone, 'Terminales'],
] as const
const employee = [
  ['/mi', LayoutDashboard, 'Inicio'], ['/mi/turnos', CalendarDays, 'Mis turnos'], ['/mi/asistencias', Clock3, 'Mis asistencias'], ['/mi/incidencias', CircleAlert, 'Incidencias'], ['/mi/horas-extra', ChartNoAxesCombined, 'Horas extra'], ['/mi/solicitudes', ClipboardCheck, 'Solicitudes'], ['/mi/perfil', CircleUserRound, 'Mi perfil'],
] as const

export function AppShell() {
  const { user, logout } = useAuth(); const [open, setOpen] = useState(false); const items = user.role === 'ADMIN' ? admin : employee
  return <div className="shell"><aside className={open ? 'sidebar sidebar-open' : 'sidebar'}><div className="sidebar-head"><Brand /><button className="icon-button sidebar-close" onClick={() => setOpen(false)} aria-label="Cerrar menú"><X /></button></div><nav>{items.map(([to, Icon, label]) => <NavLink key={to} to={to} end={to === '/admin' || to === '/mi'} onClick={() => setOpen(false)}><Icon size={19} /><span>{label}</span></NavLink>)}</nav><div className="sidebar-user"><div className="avatar">{user.email.slice(0, 1).toUpperCase()}</div><div><strong>{user.role === 'ADMIN' ? 'Administrador' : 'Empleado'}</strong><small>{user.email}</small></div><button className="icon-button" onClick={logout} title="Cerrar sesión"><LogOut size={18} /></button></div></aside><main className="main"><div className="mobile-topbar"><button className="icon-button" onClick={() => setOpen(true)} aria-label="Abrir menú"><Menu /></button><Brand /><span /></div><Outlet /></main>{open && <div className="sidebar-overlay" onClick={() => setOpen(false)} />}</div>
}
