package service;
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
        // check the authToken
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            return null;
        }
        // fetch the games
        return gameDAO.listGames();
    }
}
