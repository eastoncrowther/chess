package dataaccess;
import model.AuthData;
import java.util.Collection;
import java.util.HashSet;

public class MemoryAuthDAO implements AuthDAO{
    private final Collection<AuthData> authdata = new HashSet<>();

    @Override
    public void clear () {
        this.authdata.clear();
    }
    @Override
    public void createAuth (AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("Auth data is null");
        }
        boolean inserted = this.authdata.add(auth);
        if (!inserted) {
            throw new DataAccessException("Auth already exists");
        }
    }
    @Override
    public AuthData getAuth (String authToken) {
        for (AuthData auth : this.authdata) {
            if (authToken.equals(auth.authToken())) {
                return auth;
            }
        }
        return null;
    }
    @Override
    public void deleteAuth (AuthData auth) throws DataAccessException{
        if (!this.authdata.remove(auth)) {
            throw new DataAccessException("auth not found");
        }
    }
    // checks if the database is empty
    public boolean isEmpty () {
        return this.authdata.isEmpty();
    }
}
