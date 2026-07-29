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
