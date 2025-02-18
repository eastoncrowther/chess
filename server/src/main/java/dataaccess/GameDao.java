package dataaccess;
import model.GameData;
import java.util.ArrayList;
import java.util.Collection;

public class GameDao {
    private Collection<GameData> games = new ArrayList<>();

    // creates a new game
    public void createGame (GameData game) {
        this.games.add(game);
    }

    // retrieve a specified game with the given game ID
    public GameData getGame (int gameID) {
        for (GameData game : this.games) {
            if (gameID == game.gameID()) {
                return game;
            }
        }
        return null;
    }

    // retrieve all games
    public Collection<GameData> listGames () {
        return this.games;
    }

    // updates a chess game. It should replace the chess game string corresponding to a given gameID.
    public void updateGame (GameData game) {
        for (GameData savedGame : this.games) {
            if (savedGame.gameID() == game.gameID()) {
                savedGame = game;
                return;
            }
        }
    }

    // clears all games
    public void clear () {
        this.games.clear();
    }
}
