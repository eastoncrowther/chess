package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.Connect;
import websocket.messages.ServerMessage;

public class ConnectionsManager {
    private final ConcurrentHashMap<Session, Connect> gameSessions = new ConcurrentHashMap<>();

    public void addPlayer(Session session, String authToken, int gameID) {
        gameSessions.put(session, new Connect(authToken, gameID, Connect.Role.PLAYER));
    }

    public void addObserver(Session session, String authToken, int gameID) {
        gameSessions.put(session, new Connect(authToken, gameID, Connect.Role.OBSERVER));
    }

    public void remove(Session session) {
        gameSessions.remove(session);
    }
    public void updateGameID(Session session, int gameID) {
        Connect existing = gameSessions.get(session);
        String authToken = existing.getAuthToken();

        gameSessions.replace(session, new Connect(authToken, gameID, existing.getRole()));
    }
    public void broadcast(Session sender, ServerMessage message, boolean includeSender) throws IOException {
        var removeList = new ArrayList<Session>();

        for (Session session : gameSessions.keySet()) {
            boolean sameGame = gameSessions.get(session).equals(gameSessions.get(sender));
            boolean isSender = session == sender;

            if (session.isOpen()) {
                if ((includeSender || !isSender) && sameGame) {
                    session.getRemote().sendString(message.toJson());
                }
            } else {
                removeList.add(session);
            }
        }

        // remove disconnected sessions
        for (Session session : removeList) {
            gameSessions.remove(session);
        }
    }


}
