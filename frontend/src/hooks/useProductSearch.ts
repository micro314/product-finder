import { useCallback, useEffect, useState } from 'react'
import { getQueryHistory, searchProducts } from '../services/api'
import type { Product, SourceFailure } from '../types/product'

export function useProductSearch(token: string | null) {
  const [results, setResults] = useState<Product[]>([])
  const [failures, setFailures] = useState<SourceFailure[]>([])
  const [history, setHistory] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!token) { setHistory([]); return }
    getQueryHistory(token).then(setHistory).catch(() => setHistory([]))
  }, [token])

  const search = useCallback(async (query: string, limit: number) => {
    if (!token || !query.trim()) return
    setLoading(true); setError(''); setFailures([])
    try {
      const data = await searchProducts(token, query.trim(), limit)
      setResults(data.products); setFailures(data.failures)
      setHistory((old) => [query.trim(), ...old.filter((item) => item !== query.trim())].slice(0, 5))
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Something went wrong.') }
    finally { setLoading(false) }
  }, [token])

  return { results, failures, history, loading, error, search }
}
