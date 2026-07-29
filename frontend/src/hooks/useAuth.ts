import { useEffect, useState } from 'react'
import { clearStoredToken, exchangeCode, getStoredToken, startAuth } from '../services/auth'
import { getCurrentUser } from '../services/api'
import type { User } from '../types/auth'

export function useAuth() {
  const [token, setToken] = useState(getStoredToken)
  const [user, setUser] = useState<User | null>(null)
  const [authMessage, setAuthMessage] = useState('')

  useEffect(() => {
    const code = new URLSearchParams(window.location.search).get('code')
    if (!code) return
    exchangeCode(code)
      .then((newToken) => { setToken(newToken); window.history.replaceState({}, '', '/') })
      .catch((reason: Error) => setAuthMessage(reason.message))
  }, [])

  useEffect(() => {
    if (!token) return
    getCurrentUser(token).then(setUser).catch(() => {
      clearStoredToken(); setToken(null); setUser(null)
    })
  }, [token])

  const signOut = () => { clearStoredToken(); setToken(null); setUser(null) }
  return { token, user, authMessage, signIn: () => startAuth('auth'), register: () => startAuth('registrations'), signOut }
}
