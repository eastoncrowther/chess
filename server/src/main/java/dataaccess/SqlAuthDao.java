package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;
import java.sql.*;

public class SqlAuthDao implements AuthDAO {
    public SqlAuthDao () throws Exception {
        String[] createStatements = {
                """
            CREATE TABLE if NOT EXISTS authTable
            (
            username VARCHAR(255) NOT NULL,
            authToken VARCHAR(255) NOT NULL,
            PRIMARY KEY (authToken)
            )
            """
        };
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void clear() {
        String statementString = "TRUNCATE authTable";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing authTable", e);
        }
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var statementString = "INSERT INTO authTable (username, authToken) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setString(1, auth.username());
                statement.setString(2, auth.authToken());
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error inserting auth data");
        }
    }

    @Override
    public AuthData getAuth(String authToken) {
        var statementString = "SELECT username, authToken FROM authTable WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setString(1, authToken);

                try (var results = statement.executeQuery()) {
                    if (results.next()) {
                        String username = results.getString("username");
                        return new AuthData(authToken, username);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;
        }
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        var statementString = "DELETE FROM authTable WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
               statement.setString(1, auth.authToken());
               int rowsAffected = statement.executeUpdate();
               if (rowsAffected == 0) {
                   throw new DataAccessException("auth not found");
               }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error deleting auth data");
        }
    }

}
