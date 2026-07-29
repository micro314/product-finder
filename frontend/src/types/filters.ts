export type ProductFilters = {
  source?: string
  manufacturer?: string
  chipsetManufacturer?: string
  chipset?: string
  memoryType?: string
  minMemorySizeGb?: number
  maxMemorySizeGb?: number
  minBoostClockMhz?: number
  maxBoostClockMhz?: number
  minPrice?: number
  maxPrice?: number
}

export function hasProductFilters(filters: ProductFilters) {
  return Object.values(filters).some((value) => Array.isArray(value) ? value.length > 0 : value !== undefined && value !== null)
}

export type FilterOptions = {
  sources: string[]
  manufacturers: string[]
  chipsetManufacturers: string[]
  chipsets: string[]
  memoryTypes: string[]
  memorySizesGb: number[]
  boostClockFrequenciesMhz: number[]
  prices: number[]
}
