package NotificationService;

public class Event {
    String eventName;
    Message message;

    public Event(String eventName, Message message) {
        this.eventName = eventName;
        this.message = message;
    }
}
