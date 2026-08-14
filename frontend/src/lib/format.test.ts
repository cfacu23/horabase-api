import { describe, expect, it } from 'vitest'
import { minutes, money } from './format'

describe('format helpers', () => {
  it('formatea minutos y valores monetarios para la interfaz', () => {
    expect(minutes(135)).toBe('2 h 15 min')
    expect(money(1250)).toContain('1.250')
  })
})
