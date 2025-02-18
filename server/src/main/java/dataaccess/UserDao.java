package dataaccess;
import model.UserData;
import java.util.ArrayList;
import java.util.Collection;

public class UserDao {
    private Collection<UserData> users = new ArrayList<>();

    public UserDao () {}
    // create a new user
    public void createUser (UserData user) {
        this.users.add(user);
    }
    // retrieve a user with the given username
    public UserData getUser (String username) {
        for (UserData user : this.users) {
            if (username.equals(user.username())) {
                return user;
            }
        }
        // no user exists with that username
        return null;
    }
    // clears all users
    public void clear () {
        this.users.clear();
    }
}
