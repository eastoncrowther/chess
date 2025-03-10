package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.Properties;

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

    // not completely sure what this function is supposed to do...
    private int executeUpdate(String statement, Object... params) throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i ++) {
                    var param = params[i];
                    if (param instanceof String p) {
                        preparedStatement.setString(i + 1, p);
                    }
                }
                preparedStatement.executeUpdate();

                var keys = preparedStatement.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
                return 0;
            }
        }
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
        }
    }
}
