import type { ProductFilters } from './filters'

export type HistoryItem = { id: number; query: string; searchQuery?: string; filters?: ProductFilters }
