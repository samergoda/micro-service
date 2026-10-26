# Viewing Kafka Messages (order-created events)

This guide explains how to see the `OrderCreatedEvent` messages that the Orders
microservice publishes to Kafka whenever a new order is created.

- **Kafka container name:** `kafka-container`
- **Bootstrap server:** `localhost:9092`
- **Topic:** `order-created`
- **Message key:** the order ID
- **Message value:** JSON of the event, e.g. `{"orderId":5,"userId":1,"eventType":"ORDER_CREATED"}`

---

## Step 1 — Make sure Kafka is running

```bash
docker ps | grep kafka
```

You should see `kafka-container` with port `0.0.0.0:9092->9092/tcp`.
If it is not running, start it:

```bash
docker start kafka-container
```

---

## Step 2 — Start (or restart) the Spring Boot app

Run the Orders microservice so it can publish events:

```bash
./mvnw spring-boot:run
```

(or run it from IntelliJ).

---

## Step 3 — Open a consumer to watch messages

In a **separate terminal**, start the console consumer and leave it running.
It prints every message in the topic and then waits for new ones.

```bash
docker exec -it kafka-container /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" => "
```

- `--from-beginning` shows all messages ever published (omit it to only see new ones).
- `print.key=true` also prints the order ID (the message key).
- Press `Ctrl+C` to stop the consumer.

> Note: If you see `UNKNOWN_TOPIC_OR_PARTITION`, the topic does not exist yet
> because no order has been created. It is auto-created on the first publish.
> Create an order (Step 4) and the message will appear.

---

## Step 4 — Create an order to trigger an event

Send a request to the create-order endpoint (adjust the port if needed):

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "customerEmail": "alice.smith@example.com",
    "shippingAddress": "123 Maple Street, Springfield, IL 62701",
    "paymentMethod": "CREDIT_CARD",
    "items": [
      { "productName": "Keyboard", "productSku": "SKU-KEY-01", "quantity": 1, "unitPrice": 129.99 }
    ]
  }'
```

Within a second, the event JSON appears in the consumer terminal, e.g.:

```
5 => {"orderId":5,"userId":1,"eventType":"ORDER_CREATED"}
```

---

## Useful extra commands

### List all topics
```bash
docker exec kafka-container /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

### Pre-create the topic (optional, avoids the UNKNOWN_TOPIC warning)
```bash
docker exec kafka-container /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --create --topic order-created \
  --partitions 1 --replication-factor 1
```

### Describe the topic (partitions, offsets)
```bash
docker exec kafka-container /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe --topic order-created
```

### Count how many messages are in the topic
```bash
docker exec kafka-container /opt/kafka/bin/kafka-run-class.sh \
  kafka.tools.GetOffsetShell \
  --bootstrap-server localhost:9092 --topic order-created
```
