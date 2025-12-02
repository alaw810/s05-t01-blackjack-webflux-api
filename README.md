# 🃏 Blackjack WebFlux API

The goal of this project is to build a **fully reactive REST API** that simulates a simplified **Blackjack game**, using:

* **Spring WebFlux** (reactive programming)
* **MongoDB** to store game sessions
* **MySQL** to store player data
* **R2DBC** for reactive SQL access

The API must allow:

* Creating a new game
* Playing moves (HIT / STAND)
* Returning game state in real time
* Updating player names
* Getting a global players ranking
* Proper error handling with consistent JSON responses

The exercise focuses on:

* Clean architecture
* Reactive programming
* Dual persistence
* Good API design
* SOLID principles
* Unit and controller testing with WebTestClient

---

## 💻 **Technologies Used**

* **Java 21**
* **Spring Boot 3**
* **Spring WebFlux**
* **Spring Data R2DBC (MySQL)**
* **Spring Data Reactive MongoDB**
* **Reactor & Project Reactor Test**
* **Mockito + JUnit 5**
* **WebTestClient**
* **Docker & Docker Compose**
* **Lombok**
* **OpenAPI / Swagger**

---

## 📋 **Requirements**

Before running the project, ensure you have:

* **Java 21**
* **Maven 3.9+**
* **Docker & Docker Compose** (to run MongoDB + MySQL)
* IDE: IntelliJ IDEA recommended

Docker containers used:

| Service       | Version | Port  |
| ------------- | ------- | ----- |
| MongoDB       | 7.0     | 27017 |
| MySQL (R2DBC) | 8.x     | 3306  |


---

## 🛠️ **Installation**

### 1. Clone the repository

```bash
git clone https://github.com/alaw810/s05-t01-blackjack-webflux-api.git
cd s05-t01-blackjack-webflux-api
```

### 2. Start databases with Docker

```bash
docker compose up -d
```

This will start:

* `mongodb`
* `mysql` with schema initialization

### 3. Build the project

```bash
mvn clean install
```

## ▶️ **Execution**

Run the application:

```bash
mvn spring-boot:run
```

API will be available at:

```
http://localhost:8080
```

Swagger/OpenAPI:

```
http://localhost:8080/swagger-ui.html
```

---

# 🎮 **API Endpoints**

## 🎲 GAME ENDPOINTS (`/game`)

### **Create a new game**

`POST /game/new`

**Request**

```json
{
  "playerName": "Alice"
}
```

**Response**

```json
{
  "gameId": "123abc",
  "playerName": "Alice",
  "playerHand": ["AH", "5D"],
  "dealerHand": ["7C"],
  "playerHandValue": 16,
  "dealerHandValue": 7,
  "status": "IN_PROGRESS"
}
```

---

### **Get game details**

`GET /game/{id}`

Shows:

* Player cards
* Visible dealer card
* Real-time scores

---

### **Play a move (HIT / STAND)**

`POST /game/{id}/play`

**Request**

```json
{
  "move": "HIT"
}
```

**Response**

```json
{
  "gameId": "123abc",
  "status": "PLAYER_WIN",
  "playerHand": ["10H", "9D"],
  "dealerHand": ["5C", "7D"],
  "playerValue": 19,
  "dealerValue": 12,
  "message": "Player wins!"
}
```

---

### **Delete a game**

`DELETE /game/{id}/delete`
(Removes game from MongoDB)

---

## 🧍 PLAYER ENDPOINTS (`/player`)

### **Update player name**

`PUT /player/{id}`

**Request**

```json
{
  "newName": "Alice Updated"
}
```

---

### **Get global ranking**

`GET /player/ranking`

Sorted by:

1. Games won
2. Win rate
3. Player ID

---

# 🧱 **Architecture Overview**

```
controller → service → repository → database
               ↑ dto
               ↑ util (BlackjackRules, DeckFactory)
               ↑ exception handler
```

### Persistence:

* MySQL (R2DBC): Player data
* MongoDB: Game sessions (hands, deck, status)

### Reactive Design:

* All endpoints return `Mono` or `Flux`
* No blocking operations
* Game flow implemented in `GameServiceImpl`

---