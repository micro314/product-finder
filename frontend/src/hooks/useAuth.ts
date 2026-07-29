import { useEffect, useState } from 'react'
import { clearStoredToken, exchangeCode, getStoredToken, logout, startAuth } from '../services/auth'
import { ApiError, getCurrentUser } from '../services/api'
import type { User } from '../types/auth'

export function useAuth() {
  const [token, setToken] = useState(getStoredToken)
  const [user, setUser] = useState<User | null>(null)
  const [authMessage, setAuthMessage] = useState('')

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    const callbackError = params.get('error_description') || params.get('error')
    if (callbackError) {
      setAuthMessage(callbackError)
      window.history.replaceState({}, '', '/')
      return
    }
    if (!code) return
    exchangeCode(code, params.get('state'))
      .then((newToken) => { setToken(newToken); window.history.replaceState({}, '', '/') })
      .catch((reason: Error) => { setAuthMessage(reason.message); window.history.replaceState({}, '', '/') })
  }, [])

  useEffect(() => {
    if (!token) return
    getCurrentUser(token).then(setUser).catch((reason: Error) => {
      clearStoredToken(); setToken(null); setUser(null)
      setAuthMessage(reason instanceof ApiError && reason.status === 401 ? 'Your session expired. Please sign in again.' : reason.message)
    })
  }, [token])

  const signOut = () => { setToken(null); setUser(null); logout() }
  return { token, user, authMessage, signIn: () => startAuth('auth'), register: () => startAuth('registrations'), signOut }
}
