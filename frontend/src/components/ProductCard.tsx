import { formatPrice } from '../utils/format'
import type { Product } from '../types/product'

export function ProductCard({ product }: { product: Product }) {
  return <article className="product-card"><div className="product-visual"><span className="gpu-shape">▰</span><span className="source-tag">{product.source}</span></div><div className="product-details"><div className="product-meta"><span>{product.manufacturer}</span><span>•</span><span>{product.memorySizeGb} GB {product.memoryType}</span></div><h2>{product.name}</h2><p className="chipset">{product.chipset}</p><p className="description">{product.description || 'Graphics card details available from this retailer.'}</p><div className="product-footer"><strong>{formatPrice(product)}</strong>{product.productUrl && <a className="view-link" href={product.productUrl} target="_blank" rel="noreferrer">View retailer <span>↗</span></a>}</div></div></article>
}
