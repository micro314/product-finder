import { CLIENT_ID, KEYCLOAK_ISSUER, OAUTH_STATE_KEY, PKCE_VERIFIER_KEY, TOKEN_KEY } from '../config'

type AuthEndpoint = 'auth' | 'registrations'

function toBase64Url(bytes: Uint8Array) {
  return btoa(String.fromCharCode(...bytes)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

async function sha256(value: string) {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(value))
  return toBase64Url(new Uint8Array(digest))
}

export async function startAuth(endpoint: AuthEndpoint) {
  const verifier = toBase64Url(crypto.getRandomValues(new Uint8Array(32)))
  const state = toBase64Url(crypto.getRandomValues(new Uint8Array(24)))
  sessionStorage.setItem(PKCE_VERIFIER_KEY, verifier)
  sessionStorage.setItem(OAUTH_STATE_KEY, state)
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: `${window.location.origin}/auth/callback`,
    response_type: 'code',
    scope: 'openid profile email',
    code_challenge: await sha256(verifier),
    code_challenge_method: 'S256',
    state,
  })
  window.location.assign(`${KEYCLOAK_ISSUER}/protocol/openid-connect/${endpoint}?${params}`)
}

export async function exchangeCode(code: string, returnedState: string | null) {
  const verifier = sessionStorage.getItem(PKCE_VERIFIER_KEY)
  if (!verifier) throw new Error('Your sign-in session expired. Please try again.')
  const expectedState = sessionStorage.getItem(OAUTH_STATE_KEY)
  if (!returnedState || !expectedState || returnedState !== expectedState) throw new Error('The sign-in response could not be verified. Please try again.')
  const body = new URLSearchParams({
    grant_type: 'authorization_code', client_id: CLIENT_ID, code,
    redirect_uri: `${window.location.origin}/auth/callback`, code_verifier: verifier,
  })
  const response = await fetch(`${KEYCLOAK_ISSUER}/protocol/openid-connect/token`, { method: 'POST', body })
  if (!response.ok) {
    const details = await response.json().catch(() => null) as { error_description?: string } | null
    throw new Error(details?.error_description || 'Keycloak could not complete sign in.')
  }
  const token = await response.json() as { access_token: string }
  sessionStorage.removeItem(PKCE_VERIFIER_KEY)
  sessionStorage.removeItem(OAUTH_STATE_KEY)
  sessionStorage.setItem(TOKEN_KEY, token.access_token)
  return token.access_token
}

export function getStoredToken() { return sessionStorage.getItem(TOKEN_KEY) }
export function clearStoredToken() { sessionStorage.removeItem(TOKEN_KEY) }
