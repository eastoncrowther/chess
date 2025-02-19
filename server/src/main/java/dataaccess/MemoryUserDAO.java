package dataaccess;
import model.UserData;
import java.util.Collection;
import java.util.HashSet;

public class MemoryUserDAO implements UserDAO{
    private final Collection<UserData> userdata = new HashSet<>();

    @Override
    public void clear () {
        this.userdata.clear();
    }
    @Override
    public void createUser (UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("User data is null");
        }
        boolean inserted = this.userdata.add(user);
        if (!inserted) {
            throw new DataAccessException("User already exists");
        }

    }
    @Override
    public UserData getUser (String username) throws DataAccessException {
        for (UserData user : this.userdata) {
            if (username.equals(user.username())) {
                return user;
            }
        }
        throw new DataAccessException("Username not found");

    }
}
