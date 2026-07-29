import type { FormEvent } from 'react'
import { AdvancedFilters } from './AdvancedFilters'
import { hasProductFilters, type ProductFilters } from '../types/filters'
import type { FilterOptions } from '../types/filters'

type SearchHeroProps = { query: string; filters: ProductFilters; filterOptions: FilterOptions | null; loading: boolean; authenticated: boolean; authMessage: string; onQueryChange: (query: string) => void; onFiltersChange: (filters: ProductFilters) => void; onSearch: () => void }

export function SearchHero({ query, filters, filterOptions, loading, authenticated, authMessage, onQueryChange, onFiltersChange, onSearch }: SearchHeroProps) {
  const submit = (event: FormEvent) => { event.preventDefault(); onSearch() }
  return <section className="hero"><div className="eyebrow"><span className="live-dot" /> LIVE CATALOG SEARCH</div><h1>Find your next<br /><em>graphics card.</em></h1><p className="hero-copy">One search. Every major retailer. Make your upgrade count.</p><form className="search-bar" onSubmit={submit}><span className="search-icon">⌕</span><input aria-label="Search graphics cards" value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="Try “RTX 4070”, “RX 7800 XT”..." /><button className="button search-button" type="submit" disabled={!authenticated || loading || !query.trim() && !hasProductFilters(filters)}>{loading ? 'Searching…' : 'Search'} <span>→</span></button></form><AdvancedFilters filters={filters} options={filterOptions} onChange={onFiltersChange} />{!authenticated && <p className="auth-note">Sign in to search the catalog and keep your search history.</p>}{authMessage && <p className="error-message">{authMessage}</p>}</section>
}
