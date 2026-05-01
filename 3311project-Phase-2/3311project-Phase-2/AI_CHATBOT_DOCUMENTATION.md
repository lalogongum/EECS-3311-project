# AI Customer Assistant – Documentation

## Overview

ConsultHub includes an AI-powered Customer Assistant chatbot accessible from the client interface.
It is built on the **Groq API** (using the `llama-3.3-70b-versatile` model) and helps clients
understand the platform without exposing any personal or sensitive data.

The chatbot also includes a built-in rule-based fallback mode that works without any API key,
so the feature is always functional in demo environments.

---

## Functionality

The chatbot answers general questions about:

- How to book a consulting session (step-by-step)
- Available services and pricing
- Accepted payment methods
- Cancellation policies and refund rules
- How the booking lifecycle works
- How to manage payment methods
- General platform navigation

### Example Questions

| Question | Sample Answer |
|---|---|
| "How do I book a session?" | Step-by-step booking flow explanation |
| "What payment methods do you accept?" | Credit Card, Debit Card, PayPal, Bank Transfer |
| "Can I cancel my booking?" | Cancellation window rules from current policy |
| "What services are available?" | Lists all services with duration and price |
| "How do refunds work?" | Refund eligibility and process |

---

## System Context Provided to AI

The backend constructs a prompt containing **only public, non-personal information**:

```
- Available services (name, duration, price)
- Current cancellation window (hours)
- Current pricing range
- Notification policy status
- Payment methods accepted
- Booking state flow
- Count of approved consultants (no names/details)
```

**The AI is never given:**
- Any client names, emails, or IDs
- Any booking details or transaction data
- Any payment method details
- Any consultant personal details

---

## Integration Architecture

```
Client Browser
     │
     ▼ POST /api/chat  { message: "..." }
  Frontend (nginx)
     │
     ▼ proxy → backend:8080
  Spring Boot Backend  (ChatController.java)
     │  1. Builds system context from DataStore (public info only)
     │  2. Calls Groq API with system prompt + user message
     ▼
  Groq API  (llama-3.3-70b-versatile)
     │
     ▼ returns { reply: "..." }
  Backend → Frontend → User
```

---

## Implementation Details

- **File:** `backend/src/main/java/com/consulthub/controller/ChatController.java`
- **API Provider:** Groq (`https://api.groq.com/openai/v1/chat/completions`)
- **Model:** `llama-3.3-70b-versatile` (fast, high-quality open-source LLM)
- **Max tokens:** 512 per response
- **Fallback:** If no API key is set, a rule-based fallback responds to common questions
  so the chatbot works in demo mode without any API key

---

## Privacy & Safety Measures

1. **No personal data in prompts** – The system context is built from aggregate/public info only
2. **No database access** – The AI reads from the in-memory DataStore, not raw DB queries
3. **No automated actions** – The AI only provides information; it cannot create/modify bookings
4. **Explicit instructions** – The system prompt explicitly instructs the AI never to reveal personal info
5. **Input is user-generated text only** – No booking IDs, client IDs, or transaction data is passed

---

## Configuration

Set your Groq API key in `.env`:

```env
GROQ_API_KEY=gsk_...
```

Leave it blank for demo mode (rule-based fallback responses activate automatically).

Get a free Groq API key at: https://console.groq.com
