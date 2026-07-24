package com.rosati.productfinder.product;

import java.util.List;

/** Extension point implemented by every remote product database adapter. */
public interface ProductSource {
    String name();

    List<Product> search(ProductQuery query);
}
