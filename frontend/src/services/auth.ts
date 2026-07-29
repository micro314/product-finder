import { CLIENT_ID, KEYCLOAK_ISSUER, PKCE_VERIFIER_KEY, TOKEN_KEY } from '../config'

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
  sessionStorage.setItem(PKCE_VERIFIER_KEY, verifier)
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: `${window.location.origin}/auth/callback`,
    response_type: 'code',
    scope: 'openid profile email',
    code_challenge: await sha256(verifier),
    code_challenge_method: 'S256',
  })
  window.location.assign(`${KEYCLOAK_ISSUER}/protocol/openid-connect/${endpoint}?${params}`)
}

export async function exchangeCode(code: string) {
  const verifier = sessionStorage.getItem(PKCE_VERIFIER_KEY)
  if (!verifier) throw new Error('Your sign-in session expired. Please try again.')
  const body = new URLSearchParams({
    grant_type: 'authorization_code', client_id: CLIENT_ID, code,
    redirect_uri: `${window.location.origin}/auth/callback`, code_verifier: verifier,
  })
  const response = await fetch(`${KEYCLOAK_ISSUER}/protocol/openid-connect/token`, { method: 'POST', body })
  if (!response.ok) throw new Error('Keycloak could not complete sign in.')
  const token = await response.json() as { access_token: string }
  sessionStorage.removeItem(PKCE_VERIFIER_KEY)
  sessionStorage.setItem(TOKEN_KEY, token.access_token)
  return token.access_token
}

export function getStoredToken() { return sessionStorage.getItem(TOKEN_KEY) }
export function clearStoredToken() { sessionStorage.removeItem(TOKEN_KEY) }
