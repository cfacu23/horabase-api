import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { AuthProvider } from '../auth/AuthContext'
import { LoginPage } from './LoginPage'

describe('LoginPage', () => {
  beforeEach(() => sessionStorage.clear())

  it('muestra el acceso principal sin depender de datos simulados', () => {
    render(<MemoryRouter><AuthProvider><LoginPage /></AuthProvider></MemoryRouter>)
    expect(screen.getByRole('heading', { name: /Ingresá a HoraBase/i })).toBeInTheDocument()
    expect(screen.getByLabelText('Documento')).toBeRequired()
    expect(screen.getByLabelText('Contraseña')).toBeRequired()
    expect(screen.getByRole('button', { name: /Ingresar/i })).toBeEnabled()
  })
})
