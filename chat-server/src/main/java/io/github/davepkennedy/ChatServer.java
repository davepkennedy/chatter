package io.github.davepkennedy;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dave on 03/10/2016.
 */
public abstract class ChatServer {
    private static final String JOIN_COMMAND = "JOIN";
    private static final String SEND_COMMAND = "SEND";
    private final RoomManager roomManager;

    public static class ChatServerException extends Exception {
        public ChatServerException (String message) {
            super (message);
        }

        public ChatServerException (String message, Throwable cause) {
            super (message, cause);
        }
    }

    private final int bound;
    private final Set<Client> clients = new HashSet<>();

    public ChatServer(int bound, RoomManager roomManager) {
        this.bound = bound;
        this.roomManager = roomManager;
    }

    public abstract void run () throws ChatServerException;

    public void accepted(Client client) throws ChatServerException {
        if (clients.size() + 1 > bound) {
            throw new ChatServerException("The limit of clients will be exceeded");
        }
        clients.add(client);
    }

    private String readFromClient (Client client) throws ChatServerException {
        try {
            return client.receive();
        } catch (Client.ClientException e) {
            throw new ChatServerException("An error occurred reading from the client", e);
        }
    }

    public boolean read (Client client) throws ChatServerException {
        String message = readFromClient(client);
        if (message == null) {
            roomManager.remove (client);
            return false;
        }

        if (message.startsWith(JOIN_COMMAND)) {
            handleJoin(message, client);
        } else if (message.startsWith(SEND_COMMAND)) {
            handleSend(message, client);
        }

        return true;
    }

    private void handleSend(String message, Client client) throws ChatServerException {
        String sendMessage = message.substring(JOIN_COMMAND.length()+1).trim();
        int spacePos = sendMessage.indexOf(' ');
        if (spacePos < 0) {
            // This is not a good message for the system…
            return;
        }
        String room = sendMessage.substring(0, spacePos).trim();
        String broadcastMessage = sendMessage.substring(spacePos+1).trim();
        try {
            roomManager.broadcast(room, client, broadcastMessage + "\n");
        } catch (RoomManager.RoomManagerAccessException e) {
            try {
                client.send(e.getMessage());
            } catch (Client.ClientException e1) {
                throw new ChatServerException("There was an error sending an error to the client");
            }
        } catch (RoomManager.RoomManagerException e) {
            throw new ChatServerException("There was an error broadcasting the message", e);
        }
    }

    private void handleJoin(String message, Client client) throws ChatServerException {
        String room = message.substring(JOIN_COMMAND.length()+1).trim();
        try {
            roomManager.join(room, client);
        } catch (RoomManager.RoomManagerException e) {
            throw new ChatServerException("There was an error in joining the room", e);
        }
    }
}
