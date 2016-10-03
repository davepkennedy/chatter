package io.github.davepkennedy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dave on 03/10/2016.
 */
public class NIOChatServer extends ChatServer {
    private final int port;
    private ServerSocketChannel listener;
    private Selector selector;

    public NIOChatServer(int port, int bound, RoomManager roomManager) {
        super(bound, roomManager);
        this.port = port;
    }

    @Override
    public void run() throws ChatServerException {
        try {
            internalRun ();
        }
        catch (IOException e) {
            throw new ChatServerException("Error in running the chat server", e);
        }
    }

    private void internalRun() throws IOException, ChatServerException {
        listener = ServerSocketChannel.open();
        listener.configureBlocking(false);
        listener.socket().bind(new InetSocketAddress(port));

        selector = Selector.open();
        listener.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            doSelectLoop(keyIterator);
        }
    }

    private void doSelectLoop(Iterator<SelectionKey> keyIterator) throws ChatServerException {
        /*
        Slightly clunky iteration here.
        The key must be removed from the set to indicate it has been handled.
        */
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isAcceptable()) {
                handleAccept();
            }
            if (key.isReadable()) {
                Client client = (Client)key.attachment();
                if (!read(client)){
                    key.cancel();
                }
            }
            keyIterator.remove();
        }
    }

    private void handleAccept() throws ChatServerException {
        SocketChannel channel = null;
        try {
            channel = listener.accept();
            channel.configureBlocking(false);

            Client client = new NIOClient(channel);
            channel.register(selector, SelectionKey.OP_READ, client);

            accepted(client);
        } catch (IOException e) {
            if (channel != null)
                try {
                    channel.close();
                } catch (IOException e1) {
                    System.err.println ("Cascading failure in handleAccept");
                }
            throw new ChatServerException("Error when accepting client connection", e);
        } catch (ChatServerException e) {
            if (channel != null)
                try {
                    channel.close();
                } catch (IOException e1) {
                    System.err.println ("Cascading failure in handleAccept");
                }
            throw e;
        }
    }
}
