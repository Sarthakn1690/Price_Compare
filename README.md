# Smart Price Comparison & Analysis Platform

Full-stack app to compare product prices across Amazon, Flipkart, Myntra and get AI-powered purchase recommendations.

## Stack

- **Backend:** Java 17, Spring Boot 3.x, JPA, PostgreSQL, Jsoup
- **Frontend:** React 18, Vite, Tailwind CSS, Recharts, Framer Motion, React Router

## Quick start

### Backend

1. **PostgreSQL:** Create DB and set credentials.
   ```bash
   createdb price_comparison
   ```
2. **Config:** Copy `backend/src/main/resources/application.properties` and set:
   - `spring.datasource.url`, `username`, `password`
3. **Run:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   API base: `http://localhost:8080/api`

### Frontend

1. **Env:** Create `frontend/.env`:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
2. **Install & run:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   App: `http://localhost:5173`

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/products/search` | Body: `{ "url": "https://amazon.in/..." }` — search by URL |
| GET | `/api/products/{id}` | Product details + prices |
| GET | `/api/products/{id}/prices` | Current prices |
| GET | `/api/products/{id}/history?days=14&platform=amazon` | Price history |
| GET | `/api/products/{id}/recommendation` | AI recommendation |
| POST | `/api/products/{id}/track` | Add to tracking |

## Features

- **Search:** Paste Amazon/Flipkart/Myntra URL → get product + price
- **Compare:** Cards per platform with “Best deal” and % savings
- **History:** 7/14-day price chart (Recharts)
- **Recommendation:** Buy now / Wait / Price increasing + confidence
- **Watchlist:** Local storage + backend track

## Project layout

```
SEPM/
├── backend/          # Spring Boot API
│   └── src/main/java/com/pricecomparison/
│       ├── config/   # CORS, RestTemplate, Scheduler
│       ├── controller/
│       ├── service/  # Product, Scraper, PriceTracking, AIRecommendation
│       ├── repository/
│       ├── model/
│       ├── dto/
│       ├── scraper/  # Amazon, Flipkart, Myntra
│       └── exception/
├── frontend/         # Vite + React
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── context/
│       ├── services/
│       └── utils/
└── README.md
```

## Optional

- **Backend:** Set `OPENAI_API_KEY` for enhanced AI (currently rule-based).
- **DB:** Use H2 for dev by switching driver and URL in `application-dev.properties`.
