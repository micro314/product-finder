# Product Finder backend

The backend is a Spring Modulith application with three modules:

- `product`: canonical product model and remote-source extension contracts.
- `search`: federated search use case and HTTP API.
- `source`: dedicated Newegg, B&H Photo Video, and Abt graphics-card adapters.

## Configure the catalogs

Each catalog has an independent adapter because its request and response contracts differ:

- Newegg: `GET /api/search?keyword=...&pageSize=...`, authenticated with `X-Newegg-Api-Key`.
- B&H Photo Video: `POST /api/products/search` with a graphics-card category, authenticated with a bearer token.
- Abt: `GET /resources/search?query=...&rows=...&category=graphics-cards`, authenticated with `X-Abt-Api-Key`.

Configure their locations and credentials with environment variables:

```yaml
product-finder:
  connect-timeout: 2s
  read-timeout: 5s
  catalogs:
    newegg:
      enabled: true
      base-url: ${NEWEGG_BASE_URL}
      api-key: ${NEWEGG_API_KEY}
    bh-photo-video:
      enabled: true
      base-url: ${BH_PHOTO_VIDEO_BASE_URL}
      api-key: ${BH_PHOTO_VIDEO_API_KEY}
    abt:
      enabled: true
      base-url: ${ABT_BASE_URL}
      api-key: ${ABT_API_KEY}
```

Only catalogs with `enabled: true` are registered. Every canonical product includes manufacturer, GPU chipset, VRAM
capacity, and VRAM type in addition to price and descriptive fields. The retailer API contracts are explicit assumptions
isolated in `NeweggProductSource`, `BhPhotoVideoProductSource`, and `AbtProductSource`; adjust the corresponding adapter
when an authoritative API specification is available.

## API

`GET /api/products/search?q=office%20chair&limit=20`

The response contains canonical `products` plus per-source `failures`. A failed or timed-out database does not discard
results returned by healthy databases.

Run verification with `./gradlew test`.
