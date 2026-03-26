package NotificationService;

public class SMSChannel implements Channel {
    @Override
    public void notify(User user, Message message) {
        System.out.println("SMS : Notifying to user "+ user.userName + "with message "+message.message);
    }
}
