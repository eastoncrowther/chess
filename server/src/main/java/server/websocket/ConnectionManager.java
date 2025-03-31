package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    // adds a new connection to the hashmap
    public void add (String userName, Session session) {
        Connection connection = new Connection(userName, session);
        connections.put(userName, connection);
    }
    // remove a connection from the hashmap
    public void remove (String userName) {
        connections.remove(userName);
    }

}
