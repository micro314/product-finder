# Backend

The backend is a Spring Modulith application organized into these modules:

- `product`: canonical product model and source contracts.
- `search`: cached search use case and HTTP API.
- `source`: Newegg, B&H Photo Video, and Abt adapters.
- `cache`: MongoDB catalog cache and scheduled refresh job.
- `history`: authenticated query history stored in PostgreSQL.
- `identity`: Keycloak integration and JWT security.

Run backend tests from this directory:

```bash
./gradlew test
```

Each retailer adapter is independently configured with its base URL and API key. The remote APIs currently expose search-style contracts rather than documented full-inventory feeds, so the scheduled refresh uses each provider's wildcard query convention. Update the corresponding `*ProductSource` class when an authoritative provider API is available.

The cache refresh schedule defaults to midnight UTC. It is controlled by `PRODUCT_CACHE_REFRESH_CRON`, `PRODUCT_CACHE_REFRESH_ZONE`, and `PRODUCT_FULL_REFRESH_LIMIT`. See the [project README](../README.md) for application setup, Compose commands, API endpoints, authentication, and sample-data population.
