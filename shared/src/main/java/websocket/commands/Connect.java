package websocket.commands;

import chess.ChessGame;

public class Connect extends UserGameCommand {
    private final Integer gameID;

    // to observe pass in a null team color
    public Connect (String authToken, Integer gameID) {
        super(CommandType.CONNECT, authToken, gameID);
        this.gameID = gameID;
    }
    public Integer getGameID() {
        return gameID;
    }
}
