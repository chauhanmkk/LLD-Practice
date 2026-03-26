package NotificationService;

import java.util.List;

public class NotificationController implements NotificationObserver {


    @Override
    public void notify(Event event, List<User> user) {
        for(User user1 : user) {
            sendNotification(user1,event);
        }
    }

    private void sendNotification(User user1, Event event) {
        Message message = event.message;
        List<Channel> channels = user1.userPreference.get(message.messageType);
        for(Channel channel : channels) {
            channel.notify(user1, message);
        }
    }
}
