# GCI (Graphics Card Index)

GCI is a graphics-card catalog and search application. It caches product data from configured retailers in MongoDB, exposes a secured Spring Boot API, and provides a React frontend for searching, filtering, and comparing cards.

## Requirements

- Docker with Docker Compose v2
- Bash for `.sh` scripts, or Windows PowerShell for `.ps1` scripts
- Node.js and npm for frontend-only development
- Java 17+ for backend-only development

## Run the application

From the repository root, build the images and start the full Compose stack in the background:

```bash
./start.sh
```

On Windows PowerShell:

```powershell
.\start.ps1
```

The services are available at:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Keycloak: http://localhost:8081
- PostgreSQL: `localhost:5432`
- MongoDB: `localhost:27017`

The default local Keycloak administrator is `admin` / `admin`. Change `KEYCLOAK_ADMIN_PASSWORD` for non-local use.

## Populate the catalog

The application starts with an empty MongoDB catalog. Populate it after the stack is running:

```bash
./populate-db.sh
./populate-db.sh /path/to/catalog.json
./populate-db.sh --clear
```

PowerShell equivalents:

```powershell
.\populate-db.ps1
.\populate-db.ps1 C:\path\to\catalog.json
.\populate-db.ps1 -Clear
```

The default sample file is [`backend/sample-data/catalog_products.json`](backend/sample-data/catalog_products.json), which contains 200 graphics-card records in MongoDB Extended JSON format. The sample data includes card manufacturer, chipset manufacturer, chipset, VRAM, boost clock, price, description, retailer URL, and availability data.

The Compose stack uses these persistent volumes:

- `product-finder-keycloak`
- `product-finder-postgres`
- `product-finder-mongo`

## Configuration

Compose provides local defaults for PostgreSQL, MongoDB, and Keycloak. Override them with environment variables or a `.env` file:

- `POSTGRES_PASSWORD`
- `MONGO_PASSWORD`
- `KEYCLOAK_ADMIN_PASSWORD`
- `KEYCLOAK_REDIRECT_URI`
- `KEYCLOAK_ISSUER_URI`
- `KEYCLOAK_CLIENT_ID`
- `NEWEGG_BASE_URL`, `NEWEGG_API_KEY`
- `BH_PHOTO_VIDEO_BASE_URL`, `BH_PHOTO_VIDEO_API_KEY`
- `ABT_BASE_URL`, `ABT_API_KEY`
- `PRODUCT_CACHE_REFRESH_CRON`
- `PRODUCT_CACHE_REFRESH_ZONE`
- `PRODUCT_FULL_REFRESH_LIMIT`

The scheduled cache refresh runs at midnight UTC by default. It polls each enabled retailer, upserts records into MongoDB's `catalog_products` collection, and removes stale records from successfully refreshed sources.

## API overview

Authenticated endpoints require a Keycloak bearer token:

- `GET /api/products/search?q=RTX%209070%20XT&limit=10000`
- `GET /api/query-history`
- `DELETE /api/query-history/{id}`
- `DELETE /api/query-history`
- `GET /api/auth/me`

Public index metadata endpoints used by the frontend:

- `GET /api/index/status`
- `GET /api/index/filter-options`

Searches use cached MongoDB data. Advanced filters support retailer, manufacturers, chipsets, memory type, VRAM, boost clock, and price. Results can be paginated and further filtered in the browser.

## Development and verification

Backend tests:

```bash
cd backend
./gradlew test
```

Frontend lint and production build:

```bash
cd frontend
npm install
npm run lint
npm run build
```

For frontend development with Vite, run `npm run dev` from `frontend`. The frontend uses `VITE_API_URL` when the API is not served from the same origin.

See [`backend/README.md`](backend/README.md) for backend module and retailer-adapter details.
