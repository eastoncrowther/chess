package dataaccess;
import model.GameData;
import java.util.Collection;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO{
     private final Collection<GameData> gamedata = new HashSet<>();;

    @Override
    public void clear () {
        this.gamedata.clear();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        boolean inserted = this.gamedata.add(game);
        if (!inserted) {
            throw new DataAccessException("Game already exists");
        }
    }

    @Override
    public GameData getGame (int gameID) throws DataAccessException {
        for (GameData game : this.gamedata) {
            if (gameID == game.gameID()) {
                return game;
            }
        }
        throw new DataAccessException("gameID not found");
    }

    @Override
    public Collection<GameData> listGames() {
        return new HashSet<>(this.gamedata);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game data is null");
        }

        if (!this.gamedata.removeIf(g -> g.gameID() == game.gameID())) {
            throw new DataAccessException("gameID not found");
        }

        this.gamedata.add(game);
    }
    // function to help with generating new gameIDs
    public boolean gameIDinUse (int gameID) {
        for (GameData game : this.gamedata) {
            if (gameID == game.gameID()) {
                return true;
            }
        }
        return false;
    }
    // checks if the database is empty
    public boolean isEmpty () {
        return this.gamedata.isEmpty();
    }
}
