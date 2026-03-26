package NotificationService;

import java.util.List;

public interface NotificationEventManager {
    void publish(Event event, List<User> user);
    void subscribe(NotificationObserver observer, Event event);
}
