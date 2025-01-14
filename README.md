## KvansiptoBot

**KvansiptoBot** is a Telegram bot that helps you get information about workout exercises and track your exercise progress.

### ✨ Core Features:
- 📝 Handles Telegram commands  
- 💪 Manages exercises and users' progress  
- 🔄 Asynchronous communication using **Kafka**  
- ⚡ Data caching with **Redis**

---

### 🛠️ Stack:
- **Java** 17/18  
- **Spring Boot** 3.2.5  
- **Kafka**  
- **Redis**  
- **PostgreSQL**  
- **Telegram API**  
- **Docker** (optional)

---

### 🚀 How to Launch the Application Locally:

There are three ways to run the application depending on your setup.

### **Common Step: Clone the repository**  
Before proceeding with any of the options below, clone the repository:

```bash
git clone https://github.com/yourusername/KvansiptoBot.git
cd KvansiptoBot
```

---

### **1. Run everything with Docker (infra + services)**  
In this setup, both the infrastructure and services are run in Docker containers using their respective profiles.

For this option just run the following command:
   ```bash
   docker-compose --profile infra --profile services up -d
   ```
---

### **2. Run infrastructure with Docker, services locally**  
Here, only the infrastructure runs in Docker, and the services are run locally using the `local` profile in your IDE.

**Steps:**  
**1. Start only the infrastructure using the `infra` profile:**
```bash
   docker-compose --profile infra up -d
   ```
**2. Run the services locally in your IDE:**
Use the `local` Spring profile when launching the services.

**Important**: Make sure to add the required VM options to pass your bot credentials:
```bash
    -Dtelegram.bot.botName=KvansiptoBot
    -Dtelegram.bot.botToken=YourTelegramBotToken
```
Replace `YourTelegramBotToken` with the actual token for your bot.

**3. Verify the local environment:**
Ensure that the services are pointing to the following infrastructure endpoints:

| Component    | URL                               |
|--------------|-----------------------------------|
| PostgreSQL   | `jdbc:postgresql://localhost:5432/kvbot` |
| Redis        | `localhost:6379`                  |
| Kafka Brokers| `localhost:9092, localhost:9093, localhost:9094` |

---

### **3. Run everything locally (without Docker)**  
In this setup, both infrastructure and services are run locally without Docker.

**Steps:**

1. Install and start the required infrastructure components locally:
   - **PostgreSQL** on port `5432` (you have to create the database with name `kvbot`) 
   - **Redis** on port `6379`  
   - **Zookeeper** and **Kafka**
2. Run the services locally using the `local` Spring profile in your IDE.
3. Use the same **VM options** as listed above when starting services.

---
