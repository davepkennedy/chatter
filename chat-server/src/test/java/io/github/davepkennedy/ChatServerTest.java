package io.github.davepkennedy;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dave on 03/10/2016.
 */
public class ChatServerTest {
    private static final int CLIENT_LIMIT = 5;
    private static final int ROOM_LIMIT = 5;
    private static final String TEST_MESSAGE = "test message";
    private static final String TEST_ROOM = "foo";

    private static ChatServer server;
    private RoomManager roomManager;

    @Before
    public void setup () {
        this.roomManager = new RoomManager(ROOM_LIMIT);
        this.server = new TestChatServer (CLIENT_LIMIT, roomManager);
    }

    @Test (expected = ChatServer.ChatServerException.class)
    public void serverIsBoundedToALimit () throws ChatServer.ChatServerException {
        for (int i = 0; i < CLIENT_LIMIT + 1; i++) {
            server.accepted(new Client() {
                @Override
                public void send(String message) throws ClientException {

                }

                @Override
                public String receive() throws ClientException {
                    return null;
                }
            });
        }
    }

    @Test
    public void aClientJoinsARoom () throws ChatServer.ChatServerException, RoomManager.RoomManagerException {
        Client client = new Client() {
            @Override
            public void send(String message) throws ClientException {

            }

            @Override
            public String receive() throws ClientException {
                return "JOIN " + TEST_ROOM;
            }
        };
        server.read(client);
        // If the client didn't join the room, this will throw an exception
        roomManager.broadcast(TEST_ROOM, client, TEST_MESSAGE);
    }

    @Test
    public void aClientSendsToARoom () throws ChatServer.ChatServerException, RoomManager.RoomManagerException {
        final AtomicBoolean wasCalled = new AtomicBoolean(false);
        Client client = new Client() {
            @Override
            public void send(String message) throws ClientException {
                assertEquals (message, TEST_MESSAGE);
                wasCalled.set(true);
            }

            @Override
            public String receive() throws ClientException {
                return "SEND " + TEST_ROOM + " " + TEST_MESSAGE;
            }
        };
        roomManager.join(TEST_ROOM, client);
        server.read(client);
        assertTrue(wasCalled.get());
    }
}
