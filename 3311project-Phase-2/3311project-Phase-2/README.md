# ConsultHub – Service Booking & Consulting Platform
## Phase 2: Frontend, Docker Deployment & AI Integration

**EECS 3311 – Software Design | York University**

---

## GitHub Repository

https://github.com/Rhoguns/3311project/tree/Phase-2

---

## Project Overview

ConsultHub is a Service Booking & Consulting Platform that connects clients with professional consultants. Phase 2 builds directly on the Phase 1 Java backend by adding a REST API layer, a complete frontend, Docker-based deployment, and an AI-powered customer assistant chatbot.

---

## Architecture Overview

```
Docker Network consists of: Frontend (nginx:80 and index.html) --> Backend (Spring Boot:8080)  --> PostgreSQL DB:5432
Docker Network interacts with the Groq API (external LLM)
```

### Phase 1 Code Reuse
All Phase 1 Java classes are preserved unchanged under `backend/src/main/java/com/consulthub/legacy/`:
- `admin/` – Admin, CancellationPolicy, PricingPolicy, NotificationPolicy, RefundPolicy + Command Pattern
- `booking/` – Booking, BookingService + State Pattern
- `consultant/` – Consultant, AvailabilitySlot
- `payment/` – Payment, PaymentMethod + subclasses + State Pattern

The Spring Boot REST controllers wrap these classes directly.

---

## Design Patterns Used (Phase 1 + Phase 2)

| Pattern | Where Used |
|---|---|
| **Singleton** | Admin, CancellationPolicy, PricingPolicy, NotificationPolicy, RefundPolicy |
| **Command** | PolicyCommand hierarchy – CancellationPolicyCommand, PricingPolicyCommand, etc. |
| **State** | Booking lifecycle (Requested→Confirmed→PendingPayment→Paid→Completed/Rejected/Cancelled) |
| **State** | Payment lifecycle (Pending→Successful→Refunded/Failed) |
| **Strategy** | PaymentMethod hierarchy (CreditCard, DebitCard, PayPal, BankAccount) |

---

## Running with Docker (Single Command)

### Prerequisites
- Docker Desktop installed and running
- Git

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Rhoguns/3311project
cd 3311project

# 2. Copy env file (optionally add your Groq API key)
cp .env.example .env
# Edit .env and add GROQ_API_KEY=gsk_... (optional, chatbot works without it in fallback mode)

# 3. Start everything
docker-compose up --build

> **If you previously ran an older version**, force a full rebuild to avoid stale cached layers:
> ```bash
> docker-compose build --no-cache && docker-compose up
> ```

# 4. Open in browser
# Frontend: http://localhost
# Backend API: http://localhost:8080/api
```

To stop:
```bash
docker-compose down
```

To stop and delete all data:
```bash
docker-compose down -v
```
If it does not work try this 
```bash
Docker Network consists of: Frontend (nginx:80 and index.html) --> Backend (Spring Boot:8080)  --> PostgreSQL DB:5432
Docker Network interacts with the Groq API (external LLM)
```
---

## Running Without Docker (Local Dev)

### Backend
```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080 using H2 in-memory database
```

### Frontend
```bash
# Open frontend/index.html in a browser
# Set API base to localhost for local dev:
# In browser console: window.API_BASE = 'http://localhost:8080/api'; navigate('dashboard');
# Or simply open index.html, it defaults to /api which works via Docker nginx
```

For local dev without Docker, open `frontend/index.html` and the app will attempt to hit `localhost:8080/api`.

---

## REST API Endpoints

### Consultants
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/consultants` | All consultants |
| GET | `/api/consultants/approved` | Approved consultants only |
| GET | `/api/consultants/{id}` | Single consultant |
| POST | `/api/consultants` | Register consultant |
| PUT | `/api/consultants/{id}/approve` | Admin approve |
| PUT | `/api/consultants/{id}/reject` | Admin reject |
| GET | `/api/consultants/{id}/slots` | Get availability slots |
| POST | `/api/consultants/{id}/slots` | Add slot |
| DELETE | `/api/consultants/{id}/slots/{slotId}` | Remove slot |

### Bookings
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/bookings` | All bookings |
| GET | `/api/bookings/{id}` | Single booking |
| GET | `/api/bookings/client/{clientId}` | Client's bookings |
| GET | `/api/bookings/consultant/{consultantId}` | Consultant's bookings |
| POST | `/api/bookings` | Create booking |
| PUT | `/api/bookings/{id}/accept` | Consultant accepts |
| PUT | `/api/bookings/{id}/reject` | Consultant rejects |
| PUT | `/api/bookings/{id}/cancel` | Cancel booking |
| PUT | `/api/bookings/{id}/complete` | Mark completed |

### Payments
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/payments/client/{clientId}` | Payment history |
| POST | `/api/payments/process` | Process payment |
| POST | `/api/payments/refund` | Process refund |
| GET | `/api/payments/methods/{clientId}` | Saved methods |
| POST | `/api/payments/methods` | Add method |
| DELETE | `/api/payments/methods/{clientId}/{pmId}` | Remove method |

### Admin
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/policies` | Get all policies |
| PUT | `/api/admin/policies/cancellation` | Update cancellation window |
| PUT | `/api/admin/policies/pricing` | Update price range |
| PUT | `/api/admin/policies/notifications` | Toggle notifications |
| GET | `/api/admin/stats` | Platform statistics |

### AI Chatbot
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/chat` | Send message to AI assistant |

---

## Frontend Features

### Client Role
- Dashboard with booking stats and service cards
- Book a Session (select consultant, service, available slot)
- My Bookings (filter by state, view detail, pay, cancel)
- Payments (transaction history, refunds)
- Payment Methods (add/remove Credit Card, Debit Card, PayPal, Bank Account)
- Browse Consultants
- AI Customer Assistant chatbot

### Consultant Role
- Dashboard with booking summary
- My Schedule (upcoming sessions)
- Booking Requests (accept/reject)
- Manage Bookings (all bookings, mark complete)
- Availability Management (add/remove time slots)

### Admin Role
- Dashboard with platform stats
- System Policies (edit cancellation, pricing, notifications)
- Consultant Review (approve/reject registrations)
- All Bookings overview
- Payments Overview with revenue totals

---

## AI Customer Assistant

See `AI_CHATBOT_DOCUMENTATION.md` for full details.

**Quick setup:**
1. Add `GROQ_API_KEY=gsk_...` to your `.env` file
2. The chatbot is accessible from the Client view "AI Assistant" in the sidebar
3. If no API key is set, a rule-based fallback answers common questions automatically

---

## Project Structure

```
TeamName_Phase2/
├── backend/
│   ├── src/main/java/com/consulthub/
│   │   ├── ConsultHubApplication.java
│   │   ├── config/WebConfig.java
│   │   ├── controller/
│   │   │   ├── BookingController.java
│   │   │   ├── ConsultantController.java
│   │   │   ├── PaymentController.java
│   │   │   ├── AdminController.java
│   │   │   ├── ServiceAndClientController.java
│   │   │   └── ChatController.java
│   │   ├── service/DataStore.java
│   │   └── legacy/          ← Phase 1 code (unchanged)
│   │       ├── admin/
│   │       ├── booking/
│   │       ├── consultant/
│   │       └── payment/
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── index.html           ← Complete single-file SPA
│   ├── nginx.conf
│   └── Dockerfile
├── diagrams/
│   ├── use_case_diagram.pdf
│   └── class_diagram.pdf
├── docker-compose.yml
├── .env.example
├── AI_CHATBOT_DOCUMENTATION.md
└── README.md
```

---

## Team Contributions

| Member | GitHub | Phase 1 | Phase 2 |
|---|---|---|---|
| Hasan Kerret | `lalogongum` | Consultant subsystem | Availability API, slot management |
| Justin Fera | `Justin1374` | Admin subsystem | Admin REST API, policy controllers |
| Anh Tu Le | `TuLe12` / `tim96121204` | Payment subsystem | Payment REST API, AI chatbot |
| Philips Rhoguns | `Rhoguns` | Booking subsystem | Booking REST API, Docker, frontend |

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_PASSWORD` | `consulthub123` | PostgreSQL password |
| `GROQ_API_KEY` | _(empty)_ | Groq API key for AI chatbot (get one free at console.groq.com) |

---

## Port Mappings

| Service | Port | URL |
|---|---|---|
| Frontend | 80 | http://localhost |
| Backend | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | localhost:5432 |
