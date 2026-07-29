import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Product = {
  source: string
  externalId: string
  name: string
  manufacturer: string
  chipset: string
  memorySizeGb: number
  memoryType: string
  description: string
  price: number | null
  currency: string
  productUrl: string
}

type SearchResponse = { products: Product[]; failures: { source: string; message: string }[] }
type User = { username: string; email?: string }

const API_BASE = import.meta.env.VITE_API_URL ?? ''
const KEYCLOAK_ISSUER = import.meta.env.VITE_KEYCLOAK_ISSUER ?? 'http://localhost:8081/realms/product-finder'
const CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'product-finder-web'
const TOKEN_KEY = 'product-finder.access-token'

function toBase64Url(bytes: Uint8Array) {
  return btoa(String.fromCharCode(...bytes)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

async function sha256(value: string) {
  return toBase64Url(new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(value))))
}

async function startAuth(endpoint: 'auth' | 'registrations') {
  const verifier = toBase64Url(crypto.getRandomValues(new Uint8Array(32)))
  sessionStorage.setItem('product-finder.pkce-verifier', verifier)
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

async function exchangeCode(code: string) {
  const verifier = sessionStorage.getItem('product-finder.pkce-verifier')
  if (!verifier) throw new Error('Your sign-in session expired. Please try again.')
  const body = new URLSearchParams({
    grant_type: 'authorization_code', client_id: CLIENT_ID, code,
    redirect_uri: `${window.location.origin}/auth/callback`, code_verifier: verifier,
  })
  const response = await fetch(`${KEYCLOAK_ISSUER}/protocol/openid-connect/token`, { method: 'POST', body })
  if (!response.ok) throw new Error('Keycloak could not complete sign in.')
  const token = await response.json() as { access_token: string }
  sessionStorage.removeItem('product-finder.pkce-verifier')
  sessionStorage.setItem(TOKEN_KEY, token.access_token)
  return token.access_token
}

function money(product: Product) {
  if (product.price == null) return 'Price unavailable'
  try { return new Intl.NumberFormat(undefined, { style: 'currency', currency: product.currency || 'USD' }).format(product.price) }
  catch { return `${product.currency || '$'} ${product.price}` }
}

function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<User | null>(null)
  const [query, setQuery] = useState('RTX 4070')
  const [limit, setLimit] = useState(20)
  const [results, setResults] = useState<Product[]>([])
  const [failures, setFailures] = useState<SearchResponse['failures']>([])
  const [history, setHistory] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [authMessage, setAuthMessage] = useState('')

  useEffect(() => {
    const code = new URLSearchParams(window.location.search).get('code')
    if (!code) return
    exchangeCode(code).then((newToken) => { setToken(newToken); window.history.replaceState({}, '', '/') }).catch((reason: Error) => setAuthMessage(reason.message))
  }, [])

  useEffect(() => {
    if (!token) return
    fetch(`${API_BASE}/api/auth/me`, { headers: { Authorization: `Bearer ${token}` } })
      .then((response) => response.ok ? response.json() : Promise.reject())
      .then(setUser).catch(() => { sessionStorage.removeItem(TOKEN_KEY); setToken(null) })
    fetch(`${API_BASE}/api/query-history`, { headers: { Authorization: `Bearer ${token}` } })
      .then((response) => response.ok ? response.json() : []).then((items: { text: string }[]) => setHistory(items.map((item) => item.text)))
  }, [token])

  const search = async (event?: FormEvent, selectedQuery = query) => {
    event?.preventDefault()
    if (!selectedQuery.trim() || !token) return
    setLoading(true); setError(''); setFailures([])
    try {
      const response = await fetch(`${API_BASE}/api/products/search?q=${encodeURIComponent(selectedQuery.trim())}&limit=${limit}`, { headers: { Authorization: `Bearer ${token}` } })
      if (response.status === 401) throw new Error('Your session expired. Please sign in again.')
      if (!response.ok) throw new Error('Search failed. Please try again.')
      const data = await response.json() as SearchResponse
      setResults(data.products); setFailures(data.failures)
      setHistory((old) => [selectedQuery.trim(), ...old.filter((item) => item !== selectedQuery.trim())].slice(0, 5))
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Something went wrong.') }
    finally { setLoading(false) }
  }

  const signOut = () => { sessionStorage.removeItem(TOKEN_KEY); setToken(null); setUser(null); setResults([]) }
  const subtitle = useMemo(() => results.length ? `${results.length} matches from your connected retailers` : 'Compare what\'s available across the web', [results.length])

  return <div className="app-shell">
    <header className="topbar"><a className="brand" href="/"><span className="brand-mark">▰</span><span>pixel<span className="brand-accent">hunt</span></span></a><div className="top-actions">{user ? <><span className="user-chip"><span className="avatar">{(user.username || 'U')[0].toUpperCase()}</span>{user.username}</span><button className="text-button" onClick={signOut}>Sign out</button></> : <><button className="text-button" onClick={() => startAuth('registrations')}>Create account</button><button className="button button-small" onClick={() => startAuth('auth')}>Sign in <span>↗</span></button></>}</div></header>
    <main>
      <section className="hero"><div className="eyebrow"><span className="live-dot" /> LIVE CATALOG SEARCH</div><h1>Find your next<br /><em>graphics card.</em></h1><p className="hero-copy">One search. Every major retailer. Make your upgrade count.</p>
        <form className="search-bar" onSubmit={search}><span className="search-icon">⌕</span><input aria-label="Search graphics cards" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Try “RTX 4070”, “RX 7800 XT”..." /><select aria-label="Number of results" value={limit} onChange={(event) => setLimit(Number(event.target.value))}><option value={10}>10 results</option><option value={20}>20 results</option><option value={50}>50 results</option><option value={100}>100 results</option></select><button className="button search-button" type="submit" disabled={!token || loading}>{loading ? 'Searching…' : 'Search'} <span>→</span></button></form>
        {!token && <p className="auth-note">Sign in to search the catalog and keep your search history.</p>}{authMessage && <p className="error-message">{authMessage}</p>}
      </section>
      <section className="content-grid"><aside className="sidebar"><div className="section-label">RECENT SEARCHES</div>{history.length ? history.map((item) => <button className="history-item" key={item} onClick={() => { setQuery(item); void search(undefined, item) }}><span>↗</span>{item}</button>) : <p className="muted">Your searches will appear here.</p>}<div className="tip"><span className="tip-icon">✦</span><strong>Search smarter</strong><p>Use a model name, chipset, or memory size to get the most relevant matches.</p></div></aside><div className="results-area"><div className="results-heading"><div><div className="section-label">SEARCH RESULTS</div><p className="result-subtitle">{subtitle}</p></div>{results.length > 0 && <span className="result-count">{results.length} FOUND</span>}</div>{error && <div className="notice error-message">{error}</div>}{failures.map((failure) => <div className="notice" key={failure.source}>Couldn’t reach {failure.source}: {failure.message}</div>)}{!results.length && !loading ? <div className="empty-state"><div className="empty-icon">⌁</div><h2>{token ? 'Ready when you are' : 'Your card search starts here'}</h2><p>{token ? 'Search above to see live prices and availability from connected retailers.' : 'Sign in to compare graphics cards from multiple retailers in one place.'}</p></div> : <div className="product-list">{results.map((product) => <article className="product-card" key={`${product.source}-${product.externalId}`}><div className="product-visual"><span className="gpu-shape">▰</span><span className="source-tag">{product.source}</span></div><div className="product-details"><div className="product-meta"><span>{product.manufacturer}</span><span>•</span><span>{product.memorySizeGb} GB {product.memoryType}</span></div><h2>{product.name}</h2><p className="chipset">{product.chipset}</p><p className="description">{product.description || 'Graphics card details available from this retailer.'}</p><div className="product-footer"><strong>{money(product)}</strong>{product.productUrl && <a className="view-link" href={product.productUrl} target="_blank" rel="noreferrer">View retailer <span>↗</span></a>}</div></div></article>)}</div>}</div></section>
    </main><footer><span>pixelhunt <span className="brand-accent">•</span> graphics card finder</span><span>Connected to your product catalog</span></footer>
  </div>
}

export default App
