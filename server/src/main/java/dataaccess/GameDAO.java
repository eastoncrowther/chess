package dataaccess;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    public void clear ();
    public GameData createGame (GameData game) throws DataAccessException;
    public GameData getGame (int gameID) throws DataAccessException;
    public Collection<GameData> listGames();
    public void updateGame (GameData game) throws DataAccessException;
    public boolean gameIDinUse (int gameID);
}
