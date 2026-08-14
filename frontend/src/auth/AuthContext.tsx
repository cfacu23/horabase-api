import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { api } from '../lib/api'
import type { AuthUser, LoginResponse } from '../types'

const KEY = 'horabase.session'
interface Session { token: string; expiresAt: string; user: AuthUser }
interface AuthContextValue extends Session { login: (businessId: number | null, document: string, password: string) => Promise<AuthUser>; logout: () => void; markPasswordChanged: () => void }

const AuthContext = createContext<AuthContextValue | null>(null)
const empty: Session = { token: '', expiresAt: '', user: null as unknown as AuthUser }

function load(): Session {
  try {
    const value = sessionStorage.getItem(KEY)
    if (!value) return empty
    const session = JSON.parse(value) as Session
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      sessionStorage.removeItem(KEY)
      return empty
    }
    return session
  } catch { return empty }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session>(load)
  const persist = useCallback((next: Session) => { setSession(next); sessionStorage.setItem(KEY, JSON.stringify(next)) }, [])
  const login = useCallback(async (businessId: number | null, document: string, password: string) => {
    const result = await api<LoginResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify({ businessId, document, password }), headers: { 'Content-Type': 'application/json' } })
    persist({ token: result.accessToken, expiresAt: result.expiresAt, user: result.user })
    return result.user
  }, [persist])
  const logout = useCallback(() => { sessionStorage.removeItem(KEY); setSession(empty) }, [])
  const markPasswordChanged = useCallback(() => persist({ ...session, user: { ...session.user, mustChangePassword: false } }), [persist, session])
  const value = useMemo(() => ({ ...session, login, logout, markPasswordChanged }), [session, login, logout, markPasswordChanged])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('AuthProvider no disponible')
  return value
}
