import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { Role } from '../types'

export function ProtectedRoute({ role }: { role?: Role }) {
  const { token, user } = useAuth()
  const location = useLocation()
  if (!token) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (user.mustChangePassword && location.pathname !== '/cambiar-contrasena') return <Navigate to="/cambiar-contrasena" replace />
  if (role && user.role !== role) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/mi'} replace />
  return <Outlet />
}
