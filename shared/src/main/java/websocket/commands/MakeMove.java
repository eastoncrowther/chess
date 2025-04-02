package websocket.commands;

public class MakeMove extends UserGameCommand {
    public MakeMove(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
