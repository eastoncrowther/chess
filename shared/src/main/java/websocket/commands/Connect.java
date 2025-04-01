package websocket.commands;

import chess.ChessGame;

public class Connect extends UserGameCommand {
    public enum Role {
        PLAYER,
        OBSERVER
    }

    private final Integer gameID;
    private final Role role;


    // to observe pass in a null team color
    public Connect (String authToken, Integer gameID, Role role) {
        super(CommandType.CONNECT, authToken, gameID);
        this.gameID = gameID;
        this.role = role;
    }

    public Integer getGameID() {
        return gameID;
    }
    public Role getRole() {
        return role;
    }
}
