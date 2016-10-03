package io.github.davepkennedy;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by dave on 03/10/2016.
 */
public class RoomManagerTest {
    private RoomManager manager;

    private static final String TEST_ROOM = "testRoom";
    private static final int MAX_ROOMS = 5;
    private static final String TEST_MESSAGE = "test message";

    @Before
    public void setup () {
        manager = new RoomManager(MAX_ROOMS);
    }

    @Test
    public void roomDoesNotInitiallyExist () {
        assertFalse(manager.roomExists(TEST_ROOM));
    }

    @Test
    public void canAddARoom () throws RoomManager.RoomManagerException {
        manager.add(TEST_ROOM);
        assertTrue(manager.roomExists(TEST_ROOM));
    }

    @Test (expected = RoomManager.RoomManagerException.class)
    public void numberOfRoomsIsLimited () throws RoomManager.RoomManagerException {
        for (int i = 0; i < MAX_ROOMS + 1; i++) {
            manager.add(TEST_ROOM + i);
        }
    }

    @Test
    public void messagesAreBroadcast () throws RoomManager.RoomManagerException {

        final AtomicBoolean wasCalled = new AtomicBoolean(false);

        Client client = new Client() {
            @Override
            public void send(String message) {
                assertEquals(message, TEST_MESSAGE);
                wasCalled.set(true);
            }

            @Override
            public String receive() throws ClientException {
                return null;
            }
        };
        manager.join(TEST_ROOM, client);
        manager.broadcast (TEST_ROOM, client, TEST_MESSAGE);

        assertTrue(wasCalled.get());
    }

    @Test (expected = RoomManager.RoomManagerException.class)
    public void messageCanOnlyBeSentByRoomMembers () throws RoomManager.RoomManagerException {
        // null is not a member of the test room
        manager.add(TEST_ROOM);
        manager.broadcast(TEST_ROOM, null, TEST_MESSAGE);
    }

}
