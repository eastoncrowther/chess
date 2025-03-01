package dataaccess;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    public void clear ();
    public void createGame (GameData game) throws DataAccessException;
    public GameData getGame (int gameID) throws DataAccessException;
    public Collection<GameData> listGames();
    public void updateGame (GameData game) throws DataAccessException;
}
