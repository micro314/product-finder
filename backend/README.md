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

`GET /api/products/search?q=graphics%20card&limit=20`

The response contains canonical `products` plus per-source `failures`. A failed or timed-out database does not discard
results returned by healthy databases.

Run verification with `./gradlew test`.

## Authentication with Keycloak

Product Finder delegates account creation and sign-in to Keycloak; it never receives or stores a password. Configure a
public Keycloak client and set these environment variables:

```bash
KEYCLOAK_ISSUER_URI=https://keycloak.example.com/realms/product-finder
KEYCLOAK_CLIENT_ID=product-finder-web
KEYCLOAK_REDIRECT_URI=https://app.example.com/auth/callback
```

The redirect URI must exactly match a Valid Redirect URI configured on that Keycloak client. Enable **User
registration** in the realm if users should be able to create their own accounts.

- `GET /api/auth/login` redirects to Keycloak's authorization flow.
- `GET /api/auth/register` redirects to Keycloak's registration flow.
- `GET /api/auth/me` requires `Authorization: Bearer <Keycloak access token>` and returns the authenticated user's
  subject, username, and email.

With `KEYCLOAK_ISSUER_URI` configured, the backend validates issuer-signed JWTs on the authenticated endpoint. The
product search API remains public.

## Query history

Every product search submitted with a Keycloak bearer token is stored against that token's subject. Retrieve the
current user's most recent searches with `GET /api/query-history` and the same bearer token. Anonymous searches remain
available but are not stored because they cannot be attributed to a user.

## Catalog cache

At midnight UTC each enabled catalog is queried with its wildcard full-catalog request and the results are upserted
into MongoDB's `catalog_products` collection. Every cached record retains both the catalog name and its external
product ID. Records missing from a successfully refreshed catalog are removed. Override the schedule with
`PRODUCT_CACHE_REFRESH_CRON` and `PRODUCT_CACHE_REFRESH_ZONE`; `PRODUCT_FULL_REFRESH_LIMIT` defaults to 10,000.
Set `MONGODB_AUTO_INDEX_CREATION=true` when provisioning a new MongoDB deployment so its source/external-ID unique
index is created automatically (the Compose setup already does this).

The remote catalog APIs currently expose search-style contracts rather than a documented paginated inventory feed, so
the adapters use the providers' wildcard query convention for their complete graphics-card catalogs. If a provider
publishes a different inventory or pagination API, update that adapter's `allProducts()` implementation.

For local development, run `docker compose up --build`. It starts Keycloak at `http://localhost:8081` with the
`product-finder` realm and a `product-finder-web` public client. The default Keycloak administrator is `admin` /
`admin`; override `KEYCLOAK_ADMIN_PASSWORD` before using it outside local development. The compose file uses
`KEYCLOAK_JWK_SET_URI` so the backend can retrieve signing keys over the Docker network while retaining the browser's
`localhost` issuer URL. It also starts PostgreSQL and stores its data in the `product-finder-postgres` Docker volume.
MongoDB stores the catalog cache in the `product-finder-mongo` Docker volume.
