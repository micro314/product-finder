package com.rosati.productfinder.product;

import java.util.List;

/** Extension point implemented by every remote product database adapter. */
public interface ProductSource {
    String name();

    List<Product> search(ProductQuery query);

    /**
     * Returns the source's complete catalog for the product category handled by this application.
     * Sources that cannot supply a catalog snapshot may retain the default implementation.
     */
    default List<Product> allProducts() {
        throw new UnsupportedOperationException(name() + " does not support full catalog refreshes");
    }
}
