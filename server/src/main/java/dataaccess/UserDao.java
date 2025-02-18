package dataaccess;
import model.UserData;
import java.util.ArrayList;
import java.util.Collection;

public class UserDao {
    private Collection<UserData> users = new ArrayList<>();

    public UserDao () {}
    // create a new user
    public void createUser (UserData user) throws DataAccessException {
        // check if the user already exists
        if (users.contains(user)) {
            throw new DataAccessException("User with this ID already exists.");
        }
        this.users.add(user);
    }
    // retrieve a user with the given username
    public UserData getUser (String username) throws DataAccessException {
        for (UserData user : this.users) {
            if (username.equals(user.username())) {
                return user;
            }
        }
        // no user exists with that username
        throw new DataAccessException(username + " was not found in the database");
    }
    // clears all users
    public void clear () {
        this.users.clear();
    }
}
