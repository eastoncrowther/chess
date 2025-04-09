package service;
import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import requestresult.CreateResult;
import requestresult.JoinRequest;
import requestresult.ListResult;

import java.util.Collection;

public class GameService {
    GameDAO gameDAO;
    AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListResult list(String authToken) throws UnauthorizedException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        Collection<GameData> games = gameDAO.listGames();
        return new ListResult(games);
    }

    public CreateResult createGame(String gameName, String authToken) throws DataAccessException, UnauthorizedException {
        if (authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        try {
            // create a new chess game
            ChessGame chessGame = new ChessGame();
            ChessBoard chessBoard = new ChessBoard();
            chessBoard.resetBoard();
            chessGame.setBoard(chessBoard);

            GameData newGame = gameDAO.createGame(new GameData(0, null, null, gameName, chessGame));
            return new CreateResult(newGame.gameID());
        } catch (DataAccessException e) {
            throw new DataAccessException("game already exits");
        }
    }

    public void join(JoinRequest joinRequest, String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("no auth token found");
        }

        GameData game = null;
        try {
            game = gameDAO.getGame(joinRequest.gameID());
        } catch (DataAccessException e) {
            throw new BadRequestException("game ID not found");
        }

        String whiteTeam = game.whiteUsername();
        String blackTeam = game.blackUsername();
        String gameName = game.gameName();

        switch (joinRequest.playerColor()) {
            case "WHITE":
                if (whiteTeam == null || whiteTeam.equals(auth.username())) {
                    whiteTeam = auth.username();
                } else {
                    throw new DataAccessException("another player occupies this spot");
                }
                break;
            case "BLACK":
                if (blackTeam == null || blackTeam.equals(auth.username())) {
                    blackTeam = auth.username();
                } else {
                    throw new DataAccessException("another player occupies this spot");
                }
                break;
            default:
                throw new BadRequestException("invalid team color requested");
        }
        ;

        try {
            gameDAO.updateGame(new GameData(joinRequest.gameID(), whiteTeam, blackTeam, gameName, game.game()));
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public GameData fetchGameData (int gameID) throws DataAccessException {
        return gameDAO.getGame(gameID);
    }
    public void updateGame (GameData game) {
        try {
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    public void clear() {
        gameDAO.clear();
        authDAO.clear();
    }
}
