package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;


public class SqlGameDAO implements GameDAO {
    public SqlGameDAO () throws Exception {
        String[] createStatements = {
                """
            CREATE TABLE if NOT EXISTS gameTable
            (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteUsername VARCHAR(255),
            blackUsername VARCHAR(255),
            gameName VARCHAR(255),
            chessGame TEXT,
            PRIMARY KEY (gameID)
            )
            """
        };
        DatabaseManager.configureDatabase(createStatements);
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
    public GameData createGame(GameData game) throws DataAccessException {
        if (gameIDinUse(game.gameID())) {
            throw new DataAccessException("Game ID already exists");
        }
        String statementString = "INSERT INTO gameTable (whiteUsername, blackUsername, gameName, chessGame) " +
                "VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS)) {

            String gameJson = new Gson().toJson(game.game());

            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gameJson);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Creating game failed, no rows affected.");
            }

            // Retrieve generated gameID
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
                } else {
                    throw new DataAccessException("Creating game failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting game data");
        }
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statementString = "SELECT * FROM gameTable WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {

            statement.setInt(1, gameID);
            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return readGameData(results);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error retrieving game with ID " + gameID);
        }
        throw new DataAccessException("gameID not found");
    }


    @Override
    public Collection<GameData> listGames() {
        Collection<GameData> games = new HashSet<>();
        String sql = "SELECT * FROM gameTable";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql);
             var results = statement.executeQuery()) {

            while (results.next()) {
                games.add(readGameData(results));
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error listing games", e);
        }
        return games;
    }

    private GameData readGameData(ResultSet results) throws SQLException {

        int gameID = results.getInt("gameID");
        String whiteUsername = results.getString("whiteUsername");
        String blackUsername = results.getString("blackUsername");
        String gameName = results.getString("gameName");

        Gson serializer = new Gson();
        ChessGame chessGame = serializer.fromJson(results.getString("chessGame"), ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game data is null");
        }

        if (!gameIDinUse(game.gameID())) {
            throw new DataAccessException("gameID not found");
        }

        var statementString = "UPDATE gameTable SET whiteUsername = ?, blackUsername = ?, gameName = ?, chessGame = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {

            String gameJson = new Gson().toJson(game.game());
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gameJson);
            statement.setInt(5, game.gameID());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("Game update failed, no game found with gameID: " + game.gameID());
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error updating game data");
        }
    }


    // function to help with generating new gameIDs
    @Override
    public boolean gameIDinUse(int gameID) {
        String sql = "SELECT COUNT(*) FROM gameTable WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException | DataAccessException e) {
            return false;
        }
    }


}
