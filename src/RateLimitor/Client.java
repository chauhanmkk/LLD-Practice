package RateLimitor;

import java.util.Objects;

public class Client {
    private final int clientId;
    private final String userName;

    public Client(int clientId, String userName) {
        this.clientId = clientId;
        this.userName = userName;
    }

    public int getClientId() {
        return clientId;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Client client)) {
            return false;
        }
        return clientId == client.clientId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
