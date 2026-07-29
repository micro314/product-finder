import { ProductCard } from './ProductCard'
import type { Product, SourceFailure } from '../types/product'

type ResultsPanelProps = { results: Product[]; failures: SourceFailure[]; error: string; loading: boolean; authenticated: boolean }

export function ResultsPanel({ results, failures, error, loading, authenticated }: ResultsPanelProps) {
  const subtitle = results.length ? `${results.length} matches from your connected retailers` : 'Compare what\'s available across the web'
  return <div className="results-area"><div className="results-heading"><div><div className="section-label">SEARCH RESULTS</div><p className="result-subtitle">{subtitle}</p></div>{results.length > 0 && <span className="result-count">{results.length} FOUND</span>}</div>{error && <div className="notice error-message">{error}</div>}{failures.map((failure) => <div className="notice" key={failure.source}>Couldn’t reach {failure.source}: {failure.message}</div>)}{!results.length && !loading ? <div className="empty-state"><div className="empty-icon">⌁</div><h2>{authenticated ? 'Ready when you are' : 'Your card search starts here'}</h2><p>{authenticated ? 'Search above to see live prices and availability from connected retailers.' : 'Sign in to compare graphics cards from multiple retailers in one place.'}</p></div> : <div className="product-list">{results.map((product) => <ProductCard key={`${product.source}-${product.externalId}`} product={product} />)}</div>}</div>
}
