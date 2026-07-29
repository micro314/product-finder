import { API_BASE } from '../config'
import type { User } from '../types/auth'
import type { SearchResponse } from '../types/product'

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

function authHeaders(token: string) { return { Authorization: `Bearer ${token}` } }

export async function getCurrentUser(token: string): Promise<User> {
  const response = await fetch(`${API_BASE}/api/auth/me`, { headers: authHeaders(token) })
  if (!response.ok) throw new ApiError(`Unable to load your account (HTTP ${response.status}).`, response.status)
  return response.json() as Promise<User>
}

export async function getQueryHistory(token: string): Promise<string[]> {
  const response = await fetch(`${API_BASE}/api/query-history`, { headers: authHeaders(token) })
  if (!response.ok) return []
  const items = await response.json() as { text: string }[]
  return items.map((item) => item.text)
}

export async function searchProducts(token: string, query: string, limit: number): Promise<SearchResponse> {
  const params = new URLSearchParams({ q: query, limit: String(limit) })
  const response = await fetch(`${API_BASE}/api/products/search?${params}`, { headers: authHeaders(token) })
  if (response.status === 401) throw new Error('Your session expired. Please sign in again.')
  if (!response.ok) throw new Error('Search failed. Please try again.')
  return response.json() as Promise<SearchResponse>
}
