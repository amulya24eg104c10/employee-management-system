# Deploy (all free)
GitHub (code) -> Render (backend, Docker) + Netlify (frontend) + Aiven (free MySQL)

## Backend environment variables (set on Render)
| Name | Value |
|---|---|
| DB_URL | jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED   (from Aiven) |
| DB_USER | avnadmin |
| DB_PASSWORD | your Aiven password |
| ADMIN_USER | amulya |
| ADMIN_PASSWORD | admin password (set BEFORE first start) |
| ALLOWED_ORIGINS | https://YOUR-SITE.netlify.app  (no trailing slash) |

## Frontend settings (Netlify)
Base directory: frontend | Build command: node build.js | Publish directory: public
Environment variable: API_URL = https://YOUR-BACKEND.onrender.com/api
