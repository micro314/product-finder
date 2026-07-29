import { ProductCard } from './ProductCard'
import { useEffect, useState } from 'react'
import type { Product, SourceFailure } from '../types/product'

type ResultsPanelProps = { results: Product[]; failures: SourceFailure[]; error: string; loading: boolean; authenticated: boolean }

export function ResultsPanel({ results, failures, error, loading, authenticated }: ResultsPanelProps) {
  const [pageSize, setPageSize] = useState(20)
  const [page, setPage] = useState(1)
  useEffect(() => setPage(1), [results])
  const pageCount = Math.max(1, Math.ceil(results.length / pageSize))
  const visibleResults = results.slice((page - 1) * pageSize, page * pageSize)
  const subtitle = results.length ? `${results.length} matches from your connected retailers` : 'Compare what\'s available across the web'
  return <div className="results-area"><div className="results-heading"><div><div className="section-label">SEARCH RESULTS</div><p className="result-subtitle">{subtitle}</p></div>{results.length > 0 && <div className="results-controls"><label>Per page <select value={pageSize} onChange={(event) => { setPageSize(Number(event.target.value)); setPage(1) }}><option value={10}>10</option><option value={20}>20</option><option value={50}>50</option><option value={100}>100</option></select></label><span className="result-count">{results.length} FOUND</span></div>}</div>{error && <div className="notice error-message">{error}</div>}{failures.map((failure) => <div className="notice" key={failure.source}>Couldn’t reach {failure.source}: {failure.message}</div>)}{!results.length && !loading ? <div className="empty-state"><div className="empty-icon">⌁</div><h2>{authenticated ? 'Ready when you are' : 'Your card search starts here'}</h2><p>{authenticated ? 'Search above to see live prices and availability from connected retailers.' : 'Sign in to compare graphics cards from multiple retailers in one place.'}</p></div> : <><div className="product-list">{visibleResults.map((product) => <ProductCard key={`${product.source}-${product.externalId}`} product={product} />)}</div>{pageCount > 1 && <nav className="pagination" aria-label="Search results pages"><button className="pagination-button" disabled={page === 1} onClick={() => setPage((value) => value - 1)}>← Previous</button><span>Page {page} of {pageCount}</span><button className="pagination-button" disabled={page === pageCount} onClick={() => setPage((value) => value + 1)}>Next →</button></nav>}</>}</div>
}
