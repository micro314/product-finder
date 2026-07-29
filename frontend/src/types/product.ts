export type Product = {
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

export type SourceFailure = { source: string; message: string }
export type SearchResponse = { products: Product[]; failures: SourceFailure[] }
