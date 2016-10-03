# chatter

The idea of the chatter application is to provide a server which employs a fast select loop to process incoming requests.
This should allow the server to run with a very small number of threads.

The inspiration for this is the more familiar C Socket library combined with a basic select() call, or something like libuv

The server should setup a listener which will accept() requests.
When a socket is making a connection request, the select loop should provide the listener socket and the application will accept that client
and add it to the set of selectable sockets.
When a socket is available to be read from, the select loop will provide the list of readable sockets.
Each socket will be processed one at a time, so there should be very little multi-threading.

The job of each iteration should be very small, so the processing each socket in this way should take very little time.
If there were more work to be done for each request, then we would need to use threads (and use a more complete server library eg. Netty)

Unit tests:
Unit tests aren't provided for classes requiring network connectivity (NIOChatServer, NIOChatClient)
Ideally these would be tested where network connetivity can be guaranteed, so more appropriately in integration tests.
These classes should be almost entirely devoted to network routines and anything not directly related to NIO should be tested in another class.