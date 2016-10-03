package io.github.davepkennedy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dave on 03/10/2016.
 */
public class RoomManager {

    private final int bound;
    private final Map<String, Set<Client>> rooms = new HashMap<>();

    public static class RoomManagerException extends Exception {
        public RoomManagerException (String message) {
            super(message);
        }

        public RoomManagerException (String message, Throwable cause) {
            super (message, cause);
        }
    }

    public static class RoomManagerAccessException extends RoomManagerException {
        public RoomManagerAccessException(String message) {
            super(message);
        }

        public RoomManagerAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public RoomManager (int bound) {
        this.bound = bound;
    }

    public boolean roomExists (String room) {
        return rooms.containsKey(room);
    }

    public void add (String room) throws RoomManagerException {
        if (roomExists(room)) {
            return;
        }

        // Adding this room would exceed the bounds
        if (rooms.size()+1 > bound) {
            throw new RoomManagerException("The limit of rooms will be exceeded");
        }
        rooms.put(room, new HashSet<Client>());
    }

    public void join (String room, Client client) throws RoomManagerException {
        if (!roomExists(room)) {
            add (room);
        }
        rooms.get(room).add(client);
    }

    public void broadcast(String room, Client sender, String message) throws RoomManagerException {
        if (!roomExists(room)) {
            throw new RoomManagerAccessException("No such room exists");
        }
        if (!rooms.get(room).contains(sender)) {
            throw new RoomManagerAccessException("Client is not a member of this room");
        }
        for (Client client : rooms.get(room)) {
            try {
                client.send(message);
            } catch (Client.ClientException e) {
                throw new RoomManagerException("There was an error sending a message to a client", e);
            }
        }
    }

    public void remove(Client client) {
        for (Set<Client> clients : rooms.values()) {
            clients.remove(client);
        }
    }
}
