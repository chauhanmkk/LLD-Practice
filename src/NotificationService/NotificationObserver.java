package NotificationService;

import java.util.List;

public interface NotificationObserver {

    void notify(Event event, List<User> user);
}
