import { useEffect, useMemo, useState } from 'react'
import { AdvancedFilters } from './AdvancedFilters'
import { ProductCard } from './ProductCard'
import type { FilterOptions, ProductFilters } from '../types/filters'
import type { Product, SourceFailure } from '../types/product'
import { filterOptionsForResults, matchesProductFilters } from '../utils/filterProducts'

type ResultsPanelProps = { results: Product[]; filterOptions: FilterOptions | null; failures: SourceFailure[]; error: string; loading: boolean; authenticated: boolean }

export function ResultsPanel({ results, filterOptions, failures, error, loading, authenticated }: ResultsPanelProps) {
  const [filters, setFilters] = useState<ProductFilters>({})
  const [pageSize, setPageSize] = useState(20)
  const [page, setPage] = useState(1)
  const filteredResults = results.filter((product) => matchesProductFilters(product, filters))
  const clientFilterOptions = useMemo(() => filterOptionsForResults(results, filters), [results, filters])
  useEffect(() => setPage(1), [results, filters])
  const pageCount = Math.max(1, Math.ceil(filteredResults.length / pageSize))
  const visibleResults = filteredResults.slice((page - 1) * pageSize, page * pageSize)
  const subtitle = results.length ? `${filteredResults.length} of ${results.length} matches from your connected retailers` : 'Compare what\'s available across the web'

  return <div className="results-area"><div className="results-layout">{results.length > 0 && <AdvancedFilters filters={filters} options={clientFilterOptions ?? filterOptions} onChange={setFilters} collapsible={false} />}<div className="results-main"><div className="results-heading"><div><div className="section-label">SEARCH RESULTS</div><p className="result-subtitle">{subtitle}</p></div>{results.length > 0 && <div className="results-controls"><label>Per page <select value={pageSize} onChange={(event) => { setPageSize(Number(event.target.value)); setPage(1) }}><option value={10}>10</option><option value={20}>20</option><option value={50}>50</option><option value={100}>100</option></select></label><span className="result-count">{filteredResults.length} FOUND</span></div>}</div>{error && <div className="notice error-message">{error}</div>}{failures.map((failure) => <div className="notice" key={failure.source}>Couldn’t reach {failure.source}: {failure.message}</div>)}{!results.length && !loading ? <div className="empty-state"><div className="empty-icon">⌁</div><h2>{authenticated ? 'Ready when you are' : 'Your card search starts here'}</h2><p>{authenticated ? 'Search above to see live prices and availability from connected retailers.' : 'Sign in to compare graphics cards from multiple retailers in one place.'}</p></div> : results.length > 0 && !filteredResults.length ? <div className="empty-state"><div className="empty-icon">⌁</div><h2>No results match these filters</h2><p>Adjust the filters to see more cards.</p></div> : <><div className="product-list">{visibleResults.map((product) => <ProductCard key={`${product.source}-${product.externalId}`} product={product} />)}</div>{pageCount > 1 && <nav className="pagination" aria-label="Search results pages"><button className="pagination-button" disabled={page === 1} onClick={() => setPage((value) => value - 1)}>← Previous</button><span>Page {page} of {pageCount}</span><button className="pagination-button" disabled={page === pageCount} onClick={() => setPage((value) => value + 1)}>Next →</button></nav>}</>}</div></div></div>
}
