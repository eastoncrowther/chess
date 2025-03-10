package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SqlUserDAO implements UserDAO {
    public SqlUserDAO () throws Exception {
        configureDatabase();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE if NOT EXISTS userTable
            (
            'username' VARCHAR(255) NOT NULL,
            'password' VARCHAR(255) NOT NULL,
            'password' VARCHAR(255) NOT NULL
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
