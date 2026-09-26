# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

# Запуск бота

## 1. Запустить БД и миграции

```bash
docker-compose up -d
```

## 2. Указать переменные окружения

| Приложение                | Переменная              |
|:--------------------------|:------------------------|
| ```BotApplication```      | ```BOT_TOKEN```         |
| ```ScrapperApplication``` | ```GITHUB_TOKEN```      |
| ```ScrapperApplication``` | ```STACKOVERFLOW_KEY``` |

## 3. Запустить оба приложения

Удобнее всего через запуск в Idea (там и указать переменные окружения)

## Kafka

По умолчанию scrapper публикует `LinkUpdate` в JSON в топик `link-updates`, а bot читает его группой `link-tracker-bot`. Для локального запуска `docker compose up -d` поднимает PostgreSQL, миграции и три KRaft-брокера Kafka. Запускайте Java-приложения после готовности кластера; bootstrap-адреса для приложений на хосте: `localhost:19092,localhost:29092,localhost:39092`. При другом адресе задайте одинаковое значение `KAFKA_BOOTSTRAP_SERVERS` для обоих приложений. Чтобы вернуть синхронную отправку, установите `app.messaging-type=http` в scrapper.

Топик создаётся при старте scrapper через KafkaAdmin: 3 partitions позволяют параллельно обрабатывать независимые ссылки; URL как key сохраняет порядок сообщений одной ссылки. Replication factor 3 и `min.insync.replicas=2` вместе с producer `acks=all` позволяют подтверждать запись при отказе одного брокера. Состояние каждого брокера хранится в отдельном Docker volume.
