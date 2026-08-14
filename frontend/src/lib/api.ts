import type { ApiError } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiClientError extends Error {
  constructor(public status: number, message: string, public fields?: Record<string, string>) {
    super(message)
  }
}

async function parseError(response: Response): Promise<ApiClientError> {
  let error: ApiError | undefined
  try { error = await response.json() as ApiError } catch { /* respuesta sin JSON */ }
  return new ApiClientError(response.status, error?.message ?? 'No se pudo completar la operación', error?.errors)
}

export async function api<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  if (!response.ok) throw await parseError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const jsonBody = (value: unknown): RequestInit => ({ body: JSON.stringify(value) })
