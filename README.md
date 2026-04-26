# Remitly Assignment 2026

Simplified stock market service with the required REST API:
- bank state management
- wallet buy and sell operations
- audit log for successful wallet operations
- chaos endpoint that kills the serving instance

## Requirements
- Docker
- Java 21 (for local Maven test runs)

## One-command startup with port parameter

Windows PowerShell:

```powershell
./start.ps1 -Port 9090
```

Linux or macOS:

```bash
./start.sh 9090
```

After startup, API is available at:

```text
http://localhost:9090
```

If you skip the parameter, default port is 8080.

## High availability setup

`docker-compose.yaml` starts:
- `postgres`
- `app1` (Spring Boot instance)
- `app2` (Spring Boot instance)
- `gateway` (Nginx reverse proxy)

Client traffic goes through `gateway`, which load-balances requests across both app instances and retries another instance when one is unavailable.

Calling `POST /chaos` kills only the instance that served the request. The second instance keeps the product available.

## Stop environment

```bash
docker compose down
```

## Run tests

```bash
./mvnw test
```

## API endpoints

- `POST /wallets/{wallet_id}/stocks/{stock_name}` body `{ "type": "buy|sell" }`
- `GET /wallets/{wallet_id}`
- `GET /wallets/{wallet_id}/stocks/{stock_name}`
- `GET /stocks`
- `POST /stocks`
- `GET /log`
- `POST /chaos`
