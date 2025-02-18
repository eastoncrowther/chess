package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class AuthDao {
    private Collection<AuthData> authData = new ArrayList<>();

    public AuthDao () {}

    // adds an authToken to the array
    public void createAuth (AuthData authData) {
        this.authData.add(authData);
    }
    // returns an authToken from the array
    public AuthData getAuth (String authToken) {
        for (AuthData auth : this.authData) {
            if (authToken.equals(auth.authToken())) {
                return auth;
            }
        }
        // no auth data exists with that token.
        return null;
    }
    // removes an authToken from the array
    public void deleteAuth (String authToken) {
        // find the AuthDao object in the Array and delete it
        for (AuthData auth : this.authData) {
            if (authToken.equals(auth.authToken())) {
                this.authData.remove(auth);
                return;
            }
        }
    }
    // clears all authData
    public void clear () {
        this.authData.clear();
    }
}
