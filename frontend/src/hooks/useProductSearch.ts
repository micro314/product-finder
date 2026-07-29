import { useCallback, useEffect, useRef, useState } from 'react'
import { getQueryHistory, searchProducts } from '../services/api'
import type { Product, SourceFailure } from '../types/product'

export function useProductSearch(token: string | null) {
  const [results, setResults] = useState<Product[]>([])
  const [failures, setFailures] = useState<SourceFailure[]>([])
  const [history, setHistory] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const historyVersion = useRef(0)
  const searchVersion = useRef(0)

  useEffect(() => {
    if (!token) { historyVersion.current += 1; setHistory([]); return }
    const versionAtRequest = historyVersion.current
    getQueryHistory(token)
      .then((items) => { if (versionAtRequest === historyVersion.current) setHistory(items) })
      .catch(() => { if (versionAtRequest === historyVersion.current) setHistory([]) })
  }, [token])

  const search = useCallback(async (query: string, limit: number) => {
    if (!token || !query.trim()) return
    const version = ++searchVersion.current
    historyVersion.current += 1
    setLoading(true); setError(''); setFailures([])
    try {
      const data = await searchProducts(token, query.trim(), limit)
      if (version !== searchVersion.current) return
      setResults(data.products); setFailures(data.failures)
      setHistory((old) => [query.trim(), ...old.filter((item) => item !== query.trim())].slice(0, 5))
    } catch (reason) {
      if (version === searchVersion.current) setError(reason instanceof Error ? reason.message : 'Something went wrong.')
    }
    finally { if (version === searchVersion.current) setLoading(false) }
  }, [token])

  return { results, failures, history, loading, error, search }
}
