package service;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import java.util.Collection;
import java.util.HashSet;

public class GameService {
    MemoryGameDAO gameDAO;
    MemoryAuthDAO authDAO;

    public GameService (MemoryGameDAO gameDAO, MemoryAuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }


    public Collection<GameData> list (String authToken) {
        Collection<GameData> games = new HashSet<>();
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            return null;
        }
        return gameDAO.listGames();
    }
    public CreateResult createGame (CreateRequest createRequest) {
        AuthData auth = authDAO.getAuth(createRequest.authToken());
        if (auth == null) {
            return null;
        }
        try {
            int gameID = gameDAO.createGame(createRequest.game());
            return new CreateResult(gameID);
        } catch (DataAccessException e) {
            // why wouldn't it be able to create a new game?


        }
    }
    public void join (JoinRequest joinRequest) throws UnauthorizedException {
        AuthData auth = authDAO.getAuth(joinRequest.authToken());
        if (auth == null) {
            throw new UnauthorizedException("no auth token found");
        }
        try {
            GameData game = gameDAO.getGame(joinRequest.gameID());
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void clear () {
        gameDAO.clear();
        authDAO.clear();
    }
}
