package io.github.davepkennedy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.*;

public class ServerApp
{
    private static final int MAX_BUFFER_SIZE = 1024; // 1kb should be plenty for a simple chat server
    private static final int MAX_CLIENTS = 100;
    private static final int MAX_ROOMS = 100;
    private static final Charset MESSAGE_CHARSET = Charset.forName("UTF-8");

    ServerSocketChannel listener;
    Selector selector;
    Map<String, Set <SocketChannel>> rooms = new HashMap<>();

    public static void main( String[] args ) throws IOException {
        new ServerApp().run();
    }

    public void run () throws IOException {
        listener = ServerSocketChannel.open();
        listener.configureBlocking(false);
        listener.socket().bind(new InetSocketAddress(9000));

        selector = Selector.open();
        listener.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {
            /*
             Slightly clunky iteration here.
             The key must be removed from the set to indicate it has been handled.
            */
            System.out.println ("Current key set " + selector.keys().size());
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    handleAccept ();
                }
                if (key.isReadable()) {
                    handleRead ((SocketChannel)key.channel());
                }
                keyIterator.remove();
            }
        }
    }

    private void handleAccept () throws IOException {
        System.out.println ("Accept");
        SocketChannel client = listener.accept();
        // Selector will contain n clients + 1 listener
        if (selector.keys().size() < MAX_CLIENTS + 1) {
            System.out.println("Accepted client " + client);
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } else {
            // If too many clients connected, hard close the connection
            client.close();
        }
    }

    private void handleRead (SocketChannel clientChannel) {
        System.out.println ("Read");
        CharsetDecoder decoder = MESSAGE_CHARSET.newDecoder();
        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        try {
            int read = clientChannel.read (buffer);
            if (read > 0) {
                buffer.flip();
                CharBuffer message = decoder.decode(buffer);
                processMessage(message, clientChannel);
            } else {
                System.out.println ("The client has disconnected");
                clientChannel.close();
            }
        } catch (IOException e) {
            // The client has disconnected from the server, unregister the channel.
            System.out.println ("The client has disconnected");
        }
    }

    private void processMessage (CharBuffer message, SocketChannel clientChannel) throws IOException {
        String command = message.subSequence(0, 4).toString();
        if (command.compareToIgnoreCase("JOIN") == 0) {
            processJoinRequest (message, clientChannel);

        }
        if (command.compareToIgnoreCase("SEND") == 0) {
            System.out.println ("Client wants to SEND to a room");
            processSendMessage (message, clientChannel);
        }
    }

    private String readRoom (CharBuffer message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 5; i < message.length(); i++) {
            char c = message.charAt(i);
            if (Character.isWhitespace(c)) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private void processSendMessage(CharBuffer message, SocketChannel clientChannel) throws IOException {
        String roomName = readRoom(message);
        if (roomName.length() == 0) {
            return;
        }

        if (rooms.containsKey(roomName)) {
            CharBuffer chatMessage = message.subSequence(5 + roomName.length(), message.length());
            CharsetEncoder encoder = MESSAGE_CHARSET.newEncoder();
            ByteBuffer buffer = encoder.encode(chatMessage);
            Set<SocketChannel> room = rooms.get(roomName);
            buffer.flip();
            for (SocketChannel channel : room) {
                channel.write(buffer);
                buffer.rewind();
            }
        }
    }

    private void processJoinRequest(CharBuffer message, SocketChannel clientChannel) {
        String roomName = readRoom(message);
        if (roomName.length() == 0) {
            return;
        }
        // Add room if it doesn't exists and there are fewer than max rooms
        if ((!rooms.containsKey(roomName)) && rooms.size() < MAX_ROOMS) {
            rooms.put(roomName, new HashSet<SocketChannel>());
        }
        // Add client to room if it exists
        if (rooms.containsKey(roomName)) {
            rooms.get(roomName).add(clientChannel);
        }
    }
}
