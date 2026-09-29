import {
  CLIENT_ID,
  ID_TOKEN_KEY,
  KEYCLOAK_ISSUER,
  OAUTH_STATE_KEY,
  PKCE_VERIFIER_KEY,
  REFRESH_TOKEN_KEY,
  TOKEN_KEY,
} from "../config";

type AuthEndpoint = "auth" | "registrations";

export const AUTH_TOKEN_UPDATED_EVENT = "product-finder-auth-token-updated";
export const AUTH_EXPIRED_EVENT = "product-finder-auth-expired";

let refreshPromise: Promise<string> | null = null;

function toBase64Url(bytes: Uint8Array) {
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

async function sha256(value: string) {
  const digest = await crypto.subtle.digest(
    "SHA-256",
    new TextEncoder().encode(value),
  );
  return toBase64Url(new Uint8Array(digest));
}

export async function startAuth(endpoint: AuthEndpoint) {
  const verifier = toBase64Url(crypto.getRandomValues(new Uint8Array(32)));
  const state = toBase64Url(crypto.getRandomValues(new Uint8Array(24)));
  sessionStorage.setItem(PKCE_VERIFIER_KEY, verifier);
  sessionStorage.setItem(OAUTH_STATE_KEY, state);
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    redirect_uri: `${window.location.origin}/auth/callback`,
    response_type: "code",
    scope: "openid profile email",
    code_challenge: await sha256(verifier),
    code_challenge_method: "S256",
    state,
  });
  if (endpoint === "auth") params.set("prompt", "login");
  window.location.assign(
    `${KEYCLOAK_ISSUER}/protocol/openid-connect/${endpoint}?${params}`,
  );
}

export async function exchangeCode(code: string, returnedState: string | null) {
  const verifier = sessionStorage.getItem(PKCE_VERIFIER_KEY);
  if (!verifier)
    throw new Error("Your sign-in session expired. Please try again.");
  const expectedState = sessionStorage.getItem(OAUTH_STATE_KEY);
  if (!returnedState || !expectedState || returnedState !== expectedState)
    throw new Error(
      "The sign-in response could not be verified. Please try again.",
    );
  const body = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: CLIENT_ID,
    code,
    redirect_uri: `${window.location.origin}/auth/callback`,
    code_verifier: verifier,
  });
  const response = await fetch(
    `${KEYCLOAK_ISSUER}/protocol/openid-connect/token`,
    { method: "POST", body },
  );
  if (!response.ok) {
    const details = (await response.json().catch(() => null)) as {
      error_description?: string;
    } | null;
    throw new Error(
      details?.error_description || "Keycloak could not complete sign in.",
    );
  }
  const token = (await response.json()) as {
    access_token: string;
    refresh_token?: string;
    id_token?: string;
  };
  sessionStorage.removeItem(PKCE_VERIFIER_KEY);
  sessionStorage.removeItem(OAUTH_STATE_KEY);
  sessionStorage.setItem(TOKEN_KEY, token.access_token);
  if (token.refresh_token)
    sessionStorage.setItem(REFRESH_TOKEN_KEY, token.refresh_token);
  else sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  if (token.id_token) sessionStorage.setItem(ID_TOKEN_KEY, token.id_token);
  return token.access_token;
}

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}
export function getStoredRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

function notifyTokenUpdated(token: string) {
  window.dispatchEvent(
    new CustomEvent(AUTH_TOKEN_UPDATED_EVENT, { detail: token }),
  );
}

function notifyExpired() {
  window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
}

export async function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  refreshPromise = refreshAccessTokenInternal();
  try {
    return await refreshPromise;
  } finally {
    refreshPromise = null;
  }
}

async function refreshAccessTokenInternal() {
  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) {
    clearStoredToken();
    notifyExpired();
    throw new Error("Your session expired. Please sign in again.");
  }

  const body = new URLSearchParams({
    grant_type: "refresh_token",
    client_id: CLIENT_ID,
    refresh_token: refreshToken,
  });
  const response = await fetch(
    `${KEYCLOAK_ISSUER}/protocol/openid-connect/token`,
    { method: "POST", body },
  );
  if (!response.ok) {
    clearStoredToken();
    notifyExpired();
    throw new Error("Your session expired. Please sign in again.");
  }

  const token = (await response.json()) as {
    access_token: string;
    refresh_token?: string;
    id_token?: string;
  };
  sessionStorage.setItem(TOKEN_KEY, token.access_token);
  // Keycloak may rotate refresh tokens. Keep the new one when supplied;
  // otherwise continue using the existing refresh token.
  if (token.refresh_token)
    sessionStorage.setItem(REFRESH_TOKEN_KEY, token.refresh_token);
  if (token.id_token) sessionStorage.setItem(ID_TOKEN_KEY, token.id_token);
  notifyTokenUpdated(token.access_token);
  return token.access_token;
}

function tokenExpiresAt(token: string) {
  try {
    const payload = token.split(".")[1];
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(
      base64.length + ((4 - (base64.length % 4)) % 4),
      "=",
    );
    const decoded = JSON.parse(atob(padded)) as { exp?: number };
    return decoded.exp ? decoded.exp * 1000 : null;
  } catch {
    return null;
  }
}

export async function refreshAccessTokenIfNeeded(bufferMs = 60_000) {
  const token = getStoredToken();
  const expiresAt = token && tokenExpiresAt(token);
  if (!token || !expiresAt || expiresAt - Date.now() > bufferMs) return token;
  return refreshAccessToken();
}

export function clearStoredToken() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  sessionStorage.removeItem(ID_TOKEN_KEY);
  sessionStorage.removeItem(PKCE_VERIFIER_KEY);
  sessionStorage.removeItem(OAUTH_STATE_KEY);
}

export function logout() {
  const idToken = sessionStorage.getItem(ID_TOKEN_KEY);
  clearStoredToken();
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    post_logout_redirect_uri: `${window.location.origin}/`,
  });
  if (idToken) params.set("id_token_hint", idToken);
  window.location.assign(
    `${KEYCLOAK_ISSUER}/protocol/openid-connect/logout?${params}`,
  );
}
