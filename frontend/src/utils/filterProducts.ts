import type { ProductFilters } from '../types/filters'
import type { FilterOptions } from '../types/filters'
import type { Product } from '../types/product'

export function matchesProductFilters(product: Product, filters: ProductFilters, excluded?: keyof ProductFilters) {
  return (excluded === 'source' || !filters.source || product.source === filters.source)
    && (excluded === 'manufacturer' || !filters.manufacturer || product.manufacturer === filters.manufacturer)
    && (excluded === 'chipsetManufacturer' || !filters.chipsetManufacturer || product.chipsetManufacturer === filters.chipsetManufacturer)
    && (excluded === 'chipset' || !filters.chipset || product.chipset === filters.chipset)
    && (excluded === 'memoryType' || !filters.memoryType || product.memoryType === filters.memoryType)
    && (excluded === 'minMemorySizeGb' || excluded === 'maxMemorySizeGb' || inRange(product.memorySizeGb, filters.minMemorySizeGb, filters.maxMemorySizeGb))
    && (excluded === 'minBoostClockMhz' || excluded === 'maxBoostClockMhz' || inRange(product.boostClockMhz, filters.minBoostClockMhz, filters.maxBoostClockMhz))
    && (excluded === 'minPrice' || excluded === 'maxPrice' || inRange(product.price, filters.minPrice, filters.maxPrice))
}

function inRange(value: number | null, minimum?: number, maximum?: number) {
  return (minimum === undefined || value !== null && value >= minimum)
    && (maximum === undefined || value !== null && value <= maximum)
}

export function filterOptionsForResults(products: Product[], filters: ProductFilters): FilterOptions {
  const values = (field: keyof ProductFilters, read: (product: Product) => string) => [...new Set(products.filter((product) => matchesProductFilters(product, filters, field)).map(read))].sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }))
  const numbers = (field: keyof ProductFilters, read: (product: Product) => number | null) => [...new Set(products.filter((product) => matchesProductFilters(product, filters, field)).map(read).filter((value): value is number => value !== null))].sort((a, b) => a - b)
  const increments = (field: keyof ProductFilters, read: (product: Product) => number | null, increment: number) => {
    const available = numbers(field, read)
    const maximum = available.length ? Math.ceil(available[available.length - 1] / increment) * increment : 0
    return Array.from({ length: maximum / increment + 1 }, (_, index) => index * increment)
  }
  return {
    sources: values('source', (product) => product.source), manufacturers: values('manufacturer', (product) => product.manufacturer),
    chipsetManufacturers: values('chipsetManufacturer', (product) => product.chipsetManufacturer), chipsets: values('chipset', (product) => product.chipset),
    memoryTypes: values('memoryType', (product) => product.memoryType), memorySizesGb: numbers('minMemorySizeGb', (product) => product.memorySizeGb),
    boostClockFrequenciesMhz: increments('minBoostClockMhz', (product) => product.boostClockMhz, 100), prices: increments('minPrice', (product) => product.price, 50),
  }
}
