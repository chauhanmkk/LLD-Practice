# Notification System — LLD Interview Ready Sheet
> Target: Tier-1 (Amazon, Flipkart, Google, Uber) | Pattern: Strategy + Observer | Difficulty: Medium

---

## 1. Clarifying Questions (Ask These First)

- Which channels to support — Email, SMS, Push, WhatsApp?
- Do users have channel preferences per notification type?
- Priority levels — High (OTP, payment) vs Low (promotional)?
- Retry logic if channel fails?
- Multiple channels per notification (SMS + Email simultaneously)?
- Who triggers notifications — internal events or external API calls?

---

## 2. Core Entities

| Class | Key Fields | Responsibility |
|-------|-----------|---------------|
| `User` | userId, name, UserPreference | Person to notify |
| `UserPreference` | Map\<MessageType, List\<Channel\>\> | Per message type, which channels to use |
| `Notification` | notificationId, Message, Priority, List\<Channel\> | What to send and how |
| `Message` | messageId, content, MessageType | The actual content + type |
| `Channel` | interface | Contract for all delivery channels |
| `NotificationService` | send(), retry logic | Orchestrator — checks preferences, dispatches |
| `Event` | eventType, userId, metadata | Triggers notification via Observer |

### Enums
```java
enum Priority    { HIGH, MEDIUM, LOW }
enum MessageType { TRANSACTIONAL, PROMOTIONAL, OTP }
```

---

## 3. Key Design Insight — Two Patterns Working Together
> Interviewers check both. Missing Observer is the #1 gap candidates have.

**Strategy** — *how* to send (channel is swappable)
**Observer** — *when* to send (event triggers notification)

```
OrderService ──publishes──> OrderPlacedEvent
                                   │
                    NotificationService (Observer)
                                   │
                    checks UserPreference
                                   │
                    ┌──────────────┼──────────────┐
                    ▼              ▼               ▼
               EmailChannel   SMSChannel    PushChannel
               (Strategy)     (Strategy)    (Strategy)
```

Without Observer — nothing triggers the notification system.
Without Strategy — channel logic bleeds into NotificationService.

---

## 4. Strategy — Channel Interface + Implementations

```java
public interface Channel {
    boolean send(Message message, User user);
    ChannelType getChannelType();
}

public class EmailChannel implements Channel {
    @Override
    public boolean send(Message message, User user) {
        System.out.println("Sending EMAIL to " + user.getEmail() + ": " + message.getContent());
        // integrate with email provider (SendGrid, SES etc)
        return true;
    }
    @Override public ChannelType getChannelType() { return ChannelType.EMAIL; }
}

public class SMSChannel implements Channel {
    @Override
    public boolean send(Message message, User user) {
        System.out.println("Sending SMS to " + user.getPhone() + ": " + message.getContent());
        return true;
    }
    @Override public ChannelType getChannelType() { return ChannelType.SMS; }
}

public class PushChannel implements Channel {
    @Override
    public boolean send(Message message, User user) {
        System.out.println("Sending PUSH to device: " + user.getDeviceToken());
        return true;
    }
    @Override public ChannelType getChannelType() { return ChannelType.PUSH; }
}
```

---

## 5. Observer Pattern — Event Driven Triggering

```java
// Event published by any service
public class Event {
    String eventType;    // ORDER_PLACED, PAYMENT_SUCCESS, OTP_REQUEST
    String userId;
    Map<String, String> metadata;
}

// Observer interface
public interface NotificationObserver {
    void onEvent(Event event);
}

// Publisher interface — any service can publish events
public interface EventPublisher {
    void subscribe(String eventType, NotificationObserver observer);
    void publish(Event event);
}

public class EventBus implements EventPublisher {
    Map<String, List<NotificationObserver>> subscribers = new HashMap<>();

    @Override
    public void subscribe(String eventType, NotificationObserver observer) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
    }

    @Override
    public void publish(Event event) {
        List<NotificationObserver> observers = subscribers.getOrDefault(event.getEventType(), List.of());
        for (NotificationObserver observer : observers) {
            observer.onEvent(event);
        }
    }
}
```

---

## 6. UserPreference — Per MessageType Channel Mapping

```java
public class UserPreference {
    // For each MessageType, which channels does user want?
    Map<MessageType, List<Channel>> preference;

    public UserPreference() {
        preference = new HashMap<>();
        // Sensible defaults
        preference.put(MessageType.OTP,           List.of(new SMSChannel()));
        preference.put(MessageType.TRANSACTIONAL, List.of(new SMSChannel(), new EmailChannel()));
        preference.put(MessageType.PROMOTIONAL,   List.of(new EmailChannel()));
    }

    public List<Channel> getChannelsFor(MessageType type) {
        return preference.getOrDefault(type, List.of());
    }

    public void setPreference(MessageType type, List<Channel> channels) {
        preference.put(type, channels);
    }
}
```

---

## 7. Core Algorithm — sendNotification()

```java
public class NotificationService implements NotificationObserver {
    private final Map<ChannelType, Channel> channelRegistry;
    private final int MAX_RETRIES = 3;

    public NotificationService(List<Channel> channels) {
        channelRegistry = new HashMap<>();
        channels.forEach(c -> channelRegistry.put(c.getChannelType(), c));
    }

    // Observer callback — triggered by EventBus
    @Override
    public void onEvent(Event event) {
        // Build notification from event
        Message message = buildMessage(event);
        User user = getUserById(event.getUserId());
        sendNotification(message, user);
    }

    public void sendNotification(Message message, User user) {
        List<Channel> channels = user.getPreference().getChannelsFor(message.getMessageType());

        if (channels.isEmpty()) {
            System.out.println("User has opted out of " + message.getMessageType() + " notifications");
            return;
        }

        for (Channel channel : channels) {
            sendWithRetry(channel, message, user, message.getPriority());
        }
    }

    // Batch send — same notification to many users
    public void sendNotification(Message message, List<User> users) {
        for (User user : users) {
            sendNotification(message, user);
        }
    }

    private void sendWithRetry(Channel channel, Message message, User user, Priority priority) {
        int attempts = 0;
        int maxAttempts = priority == Priority.HIGH ? MAX_RETRIES : 1; // LOW priority — no retry

        while (attempts < maxAttempts) {
            try {
                boolean success = channel.send(message, user);
                if (success) return;
            } catch (Exception e) {
                System.out.println("Channel failed: " + e.getMessage() + " attempt " + (attempts + 1));
            }
            attempts++;
        }

        if (priority == Priority.HIGH) {
            // Fallback — try any other available channel
            fallback(message, user, channel.getChannelType());
        }
    }

    private void fallback(Message message, User user, ChannelType failedChannel) {
        // Try any channel that isn't the failed one
        channelRegistry.entrySet().stream()
            .filter(e -> e.getKey() != failedChannel)
            .findFirst()
            .ifPresent(e -> e.getValue().send(message, user));
    }
}
```

---

## 8. Supporting Classes

```java
public class Message {
    String messageId;
    String content;
    MessageType messageType;
    Priority priority;

    public Message(String content, MessageType type, Priority priority) {
        this.messageId   = UUID.randomUUID().toString();
        this.content     = content;
        this.messageType = type;
        this.priority    = priority;
    }
}

public class User {
    String userId;
    String name;
    String email;
    String phone;
    String deviceToken;
    UserPreference preference;
}
```

---

## 9. Curveballs + Answers

| Curveball | Answer |
|-----------|--------|
| User opts out of promotional | `UserPreference` returns empty list for PROMOTIONAL — `sendNotification` exits early |
| SMS gateway down, OTP must reach user | `sendWithRetry` with fallback to Email/Push for HIGH priority |
| 1 million users — bulk notification | `ExecutorService` thread pool — parallelize `sendNotification` per user |
| Add new channel (WhatsApp) | Implement `Channel` interface, register in `channelRegistry` — zero other changes |
| Rate limit notifications per user | Add `RateLimiter` check in `sendNotification` before dispatching |
| Notification history / audit | Observer on `NotificationService` — `AuditService` subscribes to sent events |

---

## 10. Mistakes to Avoid in Interview

| Mistake | Why It's Bad |
|---------|-------------|
| Missing Observer pattern | Nothing triggers notifications — system has no entry point |
| Single channel per notification | Can't send SMS + Email simultaneously |
| `Map<MessageType, Boolean>` for preference | Just on/off — can't specify which channels per type |
| No retry logic | HIGH priority notifications (OTP) silently fail |
| Retry on LOW priority too | Wastes resources on promotional messages |
| Channel logic inside `NotificationService` | SRP violation — channel is a Strategy |
| No fallback for HIGH priority | OTP fails if SMS is down — user locked out |

---

## 11. One-Line Pattern Justifications
> Say these out loud in the interview.

- **Strategy for Channel** — *"Delivery mechanism changes independently of notification logic — new channel means new class, zero changes to NotificationService."*
- **Observer for triggering** — *"Notification system shouldn't be coupled to OrderService or PaymentService — events decouple the trigger from the handler."*
- **UserPreference as Map\<MessageType, List\<Channel\>\>** — *"Preference is per message type — user wants SMS for OTP but only email for promotions."*
- **Retry only for HIGH priority** — *"Retrying promotional notifications wastes resources — retry budget should be reserved for critical messages like OTP."*
- **Fallback channel** — *"HIGH priority notifications must reach the user — if primary channel fails, fallback ensures delivery."*