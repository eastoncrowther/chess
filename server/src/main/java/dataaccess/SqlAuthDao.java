package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;
import java.sql.*;

public class SqlAuthDao implements AuthDAO {
    public SqlAuthDao () throws Exception {
        configureDatabase();
    }

    @Override
    public void clear() {
        var statementString = "TRUNCATE authTable";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {}
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        System.out.println("createAuth() called with username: " + auth.username() + " and authToken: " + auth.authToken());


        var statementString = "INSERT INTO authTable (username, authToken) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setString(1, auth.username());
                statement.setString(2, auth.authToken());

                System.out.println("Executing: " + statement);
                int rowsAffected = statement.executeUpdate();
                System.out.println("Rows inserted: " + rowsAffected);

                if (rowsAffected == 0) {
                    throw new DataAccessException("No rows inserted!");
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error inserting auth data");
        }
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
            username VARCHAR(255) NOT NULL,
            authToken VARCHAR(255) NOT NULL,
            PRIMARY KEY (authToken)
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
