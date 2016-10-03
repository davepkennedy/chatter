package io.github.davepkennedy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by dave on 03/10/2016.
 */
public class NIOClient implements Client {
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final Charset CLIENT_CHARSET = Charset.forName("UTF-8");
    private final SocketChannel channel;

    public NIOClient (SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void send(String message) throws ClientException {
        ByteBuffer buffer;
        try {
            buffer = CLIENT_CHARSET.newEncoder().encode(CharBuffer.wrap(message));
        } catch (CharacterCodingException e) {
            throw new ClientException("Could not encode the message");
        }

        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw new ClientException("Could not write the message to the underlying channel");
        }
    }

    @Override
    public String receive() throws ClientException {
        CharsetDecoder decoder = CLIENT_CHARSET.newDecoder();
        ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        try {
            int read = channel.read (buffer);
            if (read > 0) {
                buffer.flip();
                CharBuffer message = decoder.decode(buffer);
                return message.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new ClientException("An unexpected client state was encountered", e);
        }
    }
}
