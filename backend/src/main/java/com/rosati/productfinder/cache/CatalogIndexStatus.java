package com.rosati.productfinder.cache;

import java.time.Instant;

public record CatalogIndexStatus(long vendorCount, long recordCount, Instant lastPolledAt) {
}
