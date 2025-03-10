package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class SqlGameDAO implements GameDAO {
    public SqlGameDAO () throws Exception {
        configureDatabase();
    }

    @Override
    public void clear() {

    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public boolean gameIDinUse(int gameID) {
        return false;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE if NOT EXISTS gameTable
            (
            'gameID' INT NOT NULL,
            'whiteUsername' VARCHAR(255) NOT NULL,
            'blackUsername' VARCHAR(255) NOT NULL,
            'gameName' VARCHAR(255) NOT NULL,
            'chessGame' TEXT
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
