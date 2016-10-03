package io.github.davepkennedy;

public class ServerApp
{
    private static final int MAX_CLIENTS = 100;
    private static final int MAX_ROOMS = 100;

    public static void main( String[] args ) {
        RoomManager roomManager = new RoomManager(MAX_ROOMS);
        ChatServer chatServer = new NIOChatServer(9000, MAX_CLIENTS, roomManager);
        try {
            chatServer.run();
        } catch (ChatServer.ChatServerException e) {
            e.printStackTrace();
        }
    }


}
