# Microservices Data Viewer Project

This project demonstrates a robust microservices architecture designed to handle and display large datasets (approx. 10MB) efficiently. It consists of three distinct applications working in harmony: a Data Provider service, a Client Intermediary service, and a modern React Frontend.

## Architecture Overview

The system follows a linear data flow:
**Frontend (React)** &rarr; **Client App (Spring Boot)** &rarr; **Data Service (Spring Boot)**

1.  **Data Service**: A backend service that generates and streams a large JSON payload (~10MB).
2.  **Client App**: Acts as an API Gateway/Client. It fetches data from the Data Service using optimized `Apache HttpClient 5` with connection pooling and exposes it to the UI.
3.  **Frontend**: A responsive React application that consumes the data, handling network timeouts and large payload parsing gracefully.

## Tech Stack

-   **Java**: 21
-   **Spring Boot**: 3.4.0
-   **Build Tool**: Gradle 8.5
-   **Frontend**: React 18 + Vite
-   **Node.js**: 22+ (npm 10+)
-   **HTTP Client**: Apache HttpClient 5 (for backend-to-backend communication)

## Component Details & Ports

| Component | Directory | Port | Description |
| :--- | :--- | :--- | :--- |
| **Data Service** | `data-service` | **8081** | Generates 50,000 items of mock data. Uses `StreamingResponseBody` for memory efficiency. |
| **Client App** | `app` | **8080** | Intermediary service. Configured with high timeouts and connection pooling to handle large transfers. |
| **Frontend** | `frontend` | **5173** | User Interface. Includes a "timeout" simulator and large list rendering. |

---

## How to Run

You will need three separate terminal windows to run the full stack.

### 1. Start the Data Service
This service must be running first as it is the source of the data.

```bash
./gradlew :data-service:bootRun
```
*Health Check*: `http://localhost:8081/data` (Warning: This will download a 10MB file)

### 2. Start the Client App
Run this after the Data Service is up.

```bash
./gradlew :app:bootRun
```
*Health Check*: `http://localhost:8080/fetch-remote-data`

### 3. Start the Frontend UI
Navigate to the frontend directory and start the Vite dev server.

```bash
cd frontend
npm install  # Run only once to install dependencies
npm run dev
```
*Access UI*: [http://localhost:5173](http://localhost:5173)

---

## Key Features & Best Practices

### Backend (Java/Spring)
-   **Apache HttpClient 5**: Replaces the default `SimpleClientHttpRequestFactory` for `RestTemplate`.
-   **Connection Pooling**: Configured with `PoolingHttpClientConnectionManager` (Max 200 total, 50 per route) to handle concurrent high-load requests.
-   **Timeouts**: Explicit connection (5s) and read (30s) timeouts to prevent thread starvation on large payloads.
-   **Streaming**: The Data Service uses `StreamingResponseBody` to write directly to the output stream, avoiding loading the entire 10MB JSON string into heap memory at once.

### Frontend (React)
-   **AbortController**: Implements the `AbortController` API to allow user-defined cancellation of network requests.
-   **Graceful Timeouts**: A custom timeout mechanism aborts the `fetch` request if it exceeds the specified duration (default 15s), preventing the UI from hanging.
-   **Performance Metrics**: Displays the time taken to fetch and the specific size of the payload received.
