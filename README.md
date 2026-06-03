# 🛒 AI Shopping Assistant Agent

**agentic e-commerce assistant** built with **AgentScope Java**, **Spring Boot 3.4.1**. The agent uses the **ReAct (Reasoning + Acting)** paradigm to autonomously understand customer queries, call the right tools, and return smart, contextual responses.

---

## 🧠 How It Works (ReAct Loop)

```
Customer: "I need a gift under ₦20,000 for a 5-year-old"
    │
    ▼
[THINK] What does the customer want? → toys/gifts, age 5, budget ₦20,000
    │
    ▼
[ACT] Call searchProducts("toys for 5-year-old", 20000)
    │
    ▼
[OBSERVE] Got 6 results: LEGO, Fisher-Price, Crayola, Puzzle, etc.
    │
    ▼
[THINK] Which ones are in stock? → checkInventory for top results
    │
    ▼
[ACT] Call checkInventory("P001"), checkInventory("P002"), ...
    │
    ▼
[THINK] User is U001 → get personalized picks
    │
    ▼
[ACT] Call getPersonalizedRecommendations("U001")
    │
    ▼
[RESPOND] "Here are my top 3 recommendations for a 5-year-old..."
```

---

## 🗂️ Project Structure

```
ai-shopping-agent/
├── src/main/java/com/shopping/agent/
│   ├── ShoppingAgentApplication.java       # Spring Boot entry point
│   ├── config/
│   │   ├── AgentConfig.java                # AgentScope ReActAgent + tool wiring
│   │   └── JacksonConfig.java              # ObjectMapper bean
│   ├── controller/
│   │   ├── ShoppingAgentController.java    # REST endpoints
│   │   └── GlobalExceptionHandler.java     # Error handling
│   ├── model/
│   │   ├── Product.java                    # Product entity
│   │   ├── Cart.java                       # Cart + CartItem
│   │   ├── UserProfile.java                # User preferences
│   │   └── ChatModels.java                 # Request/Response DTOs
│   ├── service/
│   │   ├── ProductCatalogService.java      # Catalog search + inventory
│   │   ├── UserProfileService.java         # User preferences store
│   │   ├── CartService.java                # Cart management + discounts
│   │   └── ShoppingAgentService.java       # Agent orchestration + sessions
│   └── tools/
│       └── ShoppingTools.java              # The 5 callable agent tools
└── src/test/java/com/shopping/agent/
    ├── tools/ShoppingToolsTest.java
    ├── service/CartServiceTest.java
    ├── service/ProductCatalogServiceTest.java
    └── controller/ShoppingAgentControllerTest.java
```

---

## ⚙️ Setup

### Prerequisites
- Java 21+
- Maven 3.8+
- DashScope API Key from [Alibaba Cloud](https://dashscope.aliyuncs.com)

### 1. Set Your API Key

**Option A — Environment variable (recommended):**
```bash
export DASHSCOPE_API_KEY=your-real-api-key-here
```

**Option B — application.properties:**
```properties
dashscope.api-key=your-real-api-key-here
```

### 2. Build & Run
```bash
cd ai-shopping-agent
mvn clean install -DskipTests
mvn spring-boot:run
```

### 3. Run Tests
```bash
mvn test
```

---

## 🔌 API Endpoints

### Chat with the Agent
```http
POST /api/v1/shopping/chat
Content-Type: application/json

{
  "message": "I need a gift under ₦20,000 for a 5-year-old",
  "userId": "U001",
  "cartId": "CART-001",
  "sessionId": "optional-for-multi-turn"
}
```

**Response:**
```json
{
  "reply": "Great news! I found several wonderful gift options...",
  "sessionId": "sess-abc123",
  "success": true
}
```

---

### Example Queries to Try

| Query | Tools Agent Will Call |
|---|---|
| `"I need a gift under ₦20,000 for a 5-year-old"` | searchProducts → checkInventory → getPersonalizedRecommendations |
| `"Are LEGO sets in stock?"` | checkInventory("P001") |
| `"What discounts do you have?"` | getAvailableDiscountCodes |
| `"Apply code KIDS20 to my cart"` | applyDiscount(cartId, "KIDS20") |
| `"Recommend something based on my history"` | getPersonalizedRecommendations("U001") |
| `"I want educational toys under ₦10,000"` | searchProducts("educational toys", 10000) |

---

### Clear Session (Reset Conversation Memory)
```http
DELETE /api/v1/shopping/session/{sessionId}
```

### Health Check
```http
GET /api/v1/shopping/health
```

---

## 🧰 Agent Tools

| Tool | Description |
|---|---|
| `searchProducts(query, maxPrice)` | Searches catalog by keywords + optional budget filter |
| `checkInventory(productId)` | Returns real-time stock status for a product |
| `getPersonalizedRecommendations(userId)` | Returns personalized picks based on user history |
| `applyDiscount(cartId, code)` | Applies promo code and returns updated cart total |
| `getAvailableDiscountCodes()` | Lists all valid promo codes |

---

## 🏷️ Valid Discount Codes

| Code | Discount |
|---|---|
| `KIDS20` | 20% off |
| `WELCOME15` | 15% off |
| `SAVE10` | 10% off |
| `FLASH5` | 5% off |

---

## 👤 Test Users

| User ID | Name | Profile |
|---|---|---|
| `U001` | Adaeze Okonkwo | Prefers Toys, Art & Craft, Books |
| `U002` | Emeka Nwosu | Prefers Electronics, Toys |
| `GUEST` | Guest User | No purchase history |

---

## 🛠️ Tech Stack

| Layer | Technology               |
|---|--------------------------|
| Framework | Spring Boot 3.4.1        |
| Agent Engine | AgentScope Java 1.0.0    |
| LLM | OPENAI  |
| Agent Pattern | ReAct (Reasoning + Acting) |
| Language | Java 21                  |
| Build | Maven                    |
| Testing | JUnit 5 + Mockito + AssertJ |

---

## 💡 Extending the Project

- **Connect a real DB**: Replace `ProductCatalogService` in-memory map with JPA + PostgreSQL
- **Add payment tool**: Create a `processPayment(cartId, method)` tool
- **Add streaming**: Use AgentScope's streaming API for token-by-token responses
- **Add WebSocket**: Stream agent reasoning steps live to a frontend chat UI
- **Add auth**: Secure endpoints with Spring Security + JWT (you already know this!)
