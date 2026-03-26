package NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus implements NotificationEventManager {

    Map<String, List<NotificationObserver>> map = new HashMap<>();

    @Override
    public void publish(Event event, List<User> user) {
        List<NotificationObserver> observers = map.get(event.eventName);
        for(NotificationObserver observer : observers) {
            observer.notify(event,user);
        }
    }

    @Override
    public void subscribe(NotificationObserver observer, Event event) {
        map.computeIfAbsent(event.eventName, k-> new ArrayList<>()).add(observer);
    }
}
