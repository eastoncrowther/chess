package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.Properties;

public class SqlAuthDao implements AuthDAO {
    public SqlAuthDao () throws Exception {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
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
}
