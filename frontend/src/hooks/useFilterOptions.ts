import { useEffect, useState } from 'react'
import { getFilterOptions } from '../services/api'
import type { FilterOptions } from '../types/filters'

export function useFilterOptions() {
  const [options, setOptions] = useState<FilterOptions | null>(null)

  useEffect(() => { getFilterOptions().then(setOptions).catch(() => setOptions(null)) }, [])

  return options
}
