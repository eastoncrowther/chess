package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SqlUserDAO implements UserDAO {
    public SqlUserDAO () throws Exception {
        String[] createStatements = {
                """
            CREATE TABLE if NOT EXISTS userTable
            (
            username VARCHAR(255) NOT NULL,
            password VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            PRIMARY KEY (username)
            )
            """
        };
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void clear() {
        String statementString = "TRUNCATE userTable";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing userTable", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("User data is null");
        }

        // Check if user already exists
        if (getUser(user.username()) != null) {
            throw new DataAccessException("User already exists");
        }

        var statementString = "INSERT INTO userTable (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.setString(1, user.username());
            statement.setString(2, encryptPassword(user.password()));
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user data");
        }
    }

    @Override
    public UserData getUser(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        var statementString = "SELECT username, password, email FROM userTable WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.setString(1, username);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return new UserData(
                            results.getString("username"),
                            results.getString("password"),
                            results.getString("email")
                    );
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;
        }

        return null;
    }

    private String encryptPassword (String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }
}
