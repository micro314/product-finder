import { API_BASE } from '../config'
import type { User } from '../types/auth'
import type { HistoryItem } from '../types/history'
import type { IndexStatus } from '../types/index'
import type { FilterOptions, ProductFilters } from '../types/filters'
import type { SearchResponse } from '../types/product'

export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

function authHeaders(token: string) { return { Authorization: `Bearer ${token}` } }

export async function getIndexStatus(): Promise<IndexStatus> {
  const response = await fetch(`${API_BASE}/api/index/status`)
  if (!response.ok) throw new ApiError(`Unable to load index status (HTTP ${response.status}).`, response.status)
  return response.json() as Promise<IndexStatus>
}

export async function getFilterOptions(): Promise<FilterOptions> {
  const response = await fetch(`${API_BASE}/api/index/filter-options`)
  if (!response.ok) throw new ApiError(`Unable to load filter options (HTTP ${response.status}).`, response.status)
  return response.json() as Promise<FilterOptions>
}

export async function getCurrentUser(token: string): Promise<User> {
  const response = await fetch(`${API_BASE}/api/auth/me`, { headers: authHeaders(token) })
  if (!response.ok) throw new ApiError(`Unable to load your account (HTTP ${response.status}).`, response.status)
  return response.json() as Promise<User>
}

export async function getQueryHistory(token: string): Promise<HistoryItem[]> {
  const response = await fetch(`${API_BASE}/api/query-history`, { headers: authHeaders(token) })
  if (!response.ok) return []
  const items = await response.json() as HistoryItem[]
  const seen = new Set<string>()
  return items.filter((item) => {
    const key = item.query.trim().toLocaleLowerCase()
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

export async function deleteQueryHistoryItem(token: string, id: number) {
  await fetch(`${API_BASE}/api/query-history/${id}`, { method: 'DELETE', headers: authHeaders(token) })
}

export async function deleteAllQueryHistory(token: string) {
  await fetch(`${API_BASE}/api/query-history`, { method: 'DELETE', headers: authHeaders(token) })
}

export async function searchProducts(token: string, query: string, filters: ProductFilters): Promise<SearchResponse> {
  const params = new URLSearchParams({ q: query, limit: '10000' })
  Object.entries(filters).forEach(([key, value]) => {
    if (Array.isArray(value)) value.forEach((item) => params.append(key, String(item)))
    else if (value !== undefined && value !== null) params.set(key, String(value))
  })
  const response = await fetch(`${API_BASE}/api/products/search?${params}`, { headers: authHeaders(token) })
  if (response.status === 401) throw new Error('Your session expired. Please sign in again.')
  if (!response.ok) throw new Error('Search failed. Please try again.')
  return response.json() as Promise<SearchResponse>
}
