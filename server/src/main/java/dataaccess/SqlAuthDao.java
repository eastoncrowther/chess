package dataaccess;

import model.AuthData;
import java.sql.*;

public class SqlAuthDao implements AuthDAO {
    public SqlAuthDao () throws Exception {
        configureDatabase();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {

    }

    private final String[] createStatements = {
            """
            CREATE TABLE if NOT EXISTS authTable
            (
            'username' VARCHAR(255) NOT NULL,
            'authToken' VARCHAR(255) NOT NULL,
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
