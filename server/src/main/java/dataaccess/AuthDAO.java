package dataaccess;
import model.AuthData;

public interface AuthDAO {
    public void clear ();
    public void createAuth (AuthData auth) throws DataAccessException;
    public AuthData getAuth (String authToken);
    public void deleteAuth (AuthData auth) throws DataAccessException;
}
