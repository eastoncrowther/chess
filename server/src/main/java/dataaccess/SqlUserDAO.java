package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SqlUserDAO implements UserDAO {
    public SqlUserDAO () throws Exception {
        configureDatabase();
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
        var statementString = "INSERT INTO userTable (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setString(1, user.username());
                statement.setString(2, user.password());
                statement.setString(3, user.email());
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error inserting user data");
        }
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE if NOT EXISTS userTable
            (
            username VARCHAR(255) NOT NULL,
            password VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL
            )
            """
    };

    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
