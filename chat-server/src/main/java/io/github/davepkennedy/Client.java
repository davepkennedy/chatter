package io.github.davepkennedy;

/**
 * Created by dave on 03/10/2016.
 */
public interface Client {
    class ClientException extends Exception {
        public ClientException (String message) {
            super (message);
        }

        public ClientException (String message, Throwable cause) {
            super (message, cause);
        }
    }

    /**
     * Sends a message to a client
     * @param message The message to send
     * @throws ClientException
     */
    void send (String message) throws ClientException;

    /**
     * Reads a message from a client
     * @return A string read from the client or null if the client disconnected
     * @throws ClientException
     */
    String receive () throws ClientException;
}
