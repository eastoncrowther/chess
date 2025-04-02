package websocket.commands;

public class Leave extends UserGameCommand {
    public Leave(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
