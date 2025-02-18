package dataaccess;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collection;

public class GameDao {
    private Collection<GameData> games = new ArrayList<>();

    // creates a new game
    public void createGame (GameData game) throws DataAccessException {
        if (games.contains(game)) {
            throw new DataAccessException("game already exists");
        }
        this.games.add(game);
    }

    // retrieve a specified game with the given game ID
    public GameData getGame (int gameID) throws DataAccessException{
        for (GameData game : this.games) {
            if (gameID == game.gameID()) {
                return game;
            }
        }
        throw new DataAccessException("->" + gameID + " was not found in the database");
    }

    // retrieve all games
    public Collection<GameData> listGames () {
        return this.games;
    }

    // updates a chess game. It should replace the chess game string corresponding to a given gameID.
    public void updateGame (GameData game) throws DataAccessException {
        for (GameData savedGame : this.games) {
            if (savedGame.gameID() == game.gameID()) {
                savedGame = game;
                return;
            }
        }
        throw new DataAccessException("game ID not found");
    }

    // clears all games
    public void clear () {
        this.games.clear();
    }
}
