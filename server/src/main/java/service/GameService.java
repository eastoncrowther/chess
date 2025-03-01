package service;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import java.util.Collection;

public class GameService {
    MemoryGameDAO gameDAO;
    MemoryAuthDAO authDAO;

    public GameService (MemoryGameDAO gameDAO, MemoryAuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }


    public Collection<GameData> list (String authToken) throws UnauthorizedException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return gameDAO.listGames();
    }
    public CreateResult createGame (CreateRequest createRequest) throws DataAccessException, UnauthorizedException {
        if (authDAO.getAuth(createRequest.authToken()) == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        try {
            int gameID = gameDAO.createGame(createRequest.game());
            return new CreateResult(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException("game already exits");
        }
    }
    public void join (JoinRequest joinRequest) throws UnauthorizedException, DataAccessException {
        if (authDAO.getAuth(joinRequest.authToken()) == null) {
            throw new UnauthorizedException("no auth token found");
        }
        try {
            GameData game = gameDAO.getGame(joinRequest.gameID());
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new DataAccessException("game already exists");
        }
    }
    public void clear () {
        gameDAO.clear();
        authDAO.clear();
    }
}
