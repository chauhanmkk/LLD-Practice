package NotificationService;

import java.util.Collections;

public class Main {
    static void main() {
        Event event = new Event("Order", new Message("Order placed", MessageType.Transaction));
        NotificationObserver observer = new NotificationController();
        NotificationEventManager eventBus = new EventBus();
        eventBus.subscribe(observer, event);

        User user =  new User(1, "Mohit");
        user.addPreference(MessageType.Transaction, new SMSChannel());
        user.addPreference(MessageType.Transaction, new EmailChannel());

        eventBus.publish(event, Collections.singletonList(user));
    }
}
