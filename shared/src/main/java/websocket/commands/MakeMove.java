package websocket.commands;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

public class MakeMove extends UserGameCommand {
    // need to add a chessmove member

    // make move a 2d array

    ChessPiece promotionPiece;

    int[][] chessMove;

    public MakeMove(CommandType commandType, String authToken, Integer gameID, int[][] chessMove, ChessPiece promotionPiece) {
        super(commandType, authToken, gameID);
        this.promotionPiece = promotionPiece;
        this.chessMove = chessMove;
    }

    public ChessPiece getPromotionPiece() {
        return promotionPiece;
    }

    public ChessMove getChessMove() {
        ChessPosition starPosition = new ChessPosition(chessMove[0][0], chessMove[0][1]);
        ChessPosition endPosition = new ChessPosition(chessMove[1][0], chessMove[1][1]);

        return new ChessMove(starPosition, endPosition, promotionPiece.getPieceType());
    }
}
