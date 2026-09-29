import type { Product } from "../types/product";

export function formatPrice(product: Product) {
  if (product.price == null) return "Price unavailable";
  try {
    return new Intl.NumberFormat(undefined, {
      style: "currency",
      currency: product.currency || "USD",
    }).format(product.price);
  } catch {
    return `${product.currency || "$"} ${product.price}`;
  }
}
