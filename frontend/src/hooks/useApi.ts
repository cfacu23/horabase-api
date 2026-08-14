import { useCallback, useEffect, useState } from 'react'
import { ApiClientError, api } from '../lib/api'
import { useAuth } from '../auth/AuthContext'

export function useApiData<T>(path: string | null, initial: T) {
  const { token, logout } = useAuth()
  const [data, setData] = useState<T>(initial)
  const [loading, setLoading] = useState(Boolean(path))
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    if (!path) return
    setLoading(true); setError('')
    try { setData(await api<T>(path, {}, token)) }
    catch (reason) { if (reason instanceof ApiClientError && reason.status === 401) logout(); setError(reason instanceof Error ? reason.message : 'Error inesperado') }
    finally { setLoading(false) }
  }, [path, token, logout])
  useEffect(() => { void load() }, [load])
  return { data, setData, loading, error, reload: load }
}
