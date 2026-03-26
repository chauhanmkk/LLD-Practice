package NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    int userid;
    String userName;
    Map<MessageType, List<Channel>> userPreference;

    public User(int userid, String userName) {
        this.userid = userid;
        this.userName = userName;
        userPreference = new HashMap<>();
        defaultPreference();
    }

    void addPreference(MessageType type, Channel channel) {
        this.userPreference.computeIfAbsent(type, k-> new ArrayList<>()).add(channel);
    }

    void defaultPreference() {
        //here assign user dfefault preference;
    }
    // void removePreference // to remove preference for user

}
