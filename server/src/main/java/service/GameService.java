package service;
import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    MemoryGameDAO gameDAO;
    MemoryAuthDAO authDAO;

    public GameService (MemoryGameDAO gameDAO, MemoryAuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }


    public Collection<GameData> list (String authToken) throws UnauthorizedException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return gameDAO.listGames();
    }
    public CreateResult createGame (String gameName, String authToken) throws DataAccessException, UnauthorizedException {
        if (authDAO.getAuth(authToken )== null) {
            throw new UnauthorizedException("invalid auth token");
        }
        try {
            // create a new chess game
            ChessGame chessGame = new ChessGame();
            ChessBoard chessBoard = new ChessBoard();
            chessBoard.resetBoard();
            chessGame.setBoard(chessBoard);

            // generate a new game ID
            int gameID;
            do {
                gameID = ThreadLocalRandom.current().nextInt(1, 1000);
            } while (gameDAO.gameIDinUse(gameID));

            gameDAO.createGame(new GameData(gameID, null, null, gameName, chessGame));
            return new CreateResult(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException("game already exits");
        }
    }
    public void join (JoinRequest joinRequest) throws UnauthorizedException, DataAccessException {
        if (authDAO.getAuth(joinRequest.authToken()) == null) {
            throw new UnauthorizedException("no auth token found");
        }
        try {
            GameData game = gameDAO.getGame(joinRequest.gameID());
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new DataAccessException("game already exists");
        }
    }
    public void clear () {
        gameDAO.clear();
        authDAO.clear();
    }
}
