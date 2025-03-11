package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
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
        String statementString = "TRUNCATE gameTable";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing gameTable", e);
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statementString = "INSERT INTO gameTable (gameID, whiteUsername, blackUsername, gameName, chessGame)" +
                "VALUES (?, ?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                String gameJson = new Gson().toJson(game.game());

                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, gameJson);
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error inserting game data");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statementString = "SELECT * FROM gameTable WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setInt(1, gameID);

                try (var results = statement.executeQuery()) {
                    results.next();
                    String whiteUsername = results.getString("whiteUsername");
                    String blackUsername = results.getString("blackUsername");
                    String gameName = results.getString("gameName");

                    Gson serializer = new Gson();
                    ChessGame chessGame = serializer.fromJson(results.getString("chessGame"), ChessGame.class);

                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;
        }
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
            gameID INT NOT NULL,
            whiteUsername VARCHAR(255) NOT NULL,
            blackUsername VARCHAR(255) NOT NULL,
            gameName VARCHAR(255) NOT NULL,
            chessGame TEXT
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
