package dataaccess;
import model.AuthData;
import java.util.ArrayList;
import java.util.Collection;

public class AuthDao {
    private Collection<AuthData> auths = new ArrayList<>();

    public AuthDao () {}

    // adds an authToken to the array
    public void createAuth (AuthData authData) throws DataAccessException {
        if (this.auths.contains(authData)) {
            throw new DataAccessException("authToken already exists");
        }
        this.auths.add(authData);
    }
    // returns an authToken from the array
    public AuthData getAuth (String authToken) throws DataAccessException {
        for (AuthData auth : this.auths) {
            if (authToken.equals(auth.authToken())) {
                return auth;
            }
        }
        // no auth data exists with that token.
        throw new DataAccessException(authToken + " was not found in the database");
    }
    // removes an authToken from the array
    public void deleteAuth (String authToken) {
        // find the AuthDao object in the Array and delete it
        for (AuthData auth : this.auths) {
            if (authToken.equals(auth.authToken())) {
                this.auths.remove(auth);
                return;
            }
        }
    }
    // clears all authData
    public void clear () {
        this.auths.clear();
    }
}
