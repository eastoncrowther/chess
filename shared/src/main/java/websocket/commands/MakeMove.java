package websocket.commands;

import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.annotations.SerializedName;

public class MakeMove extends UserGameCommand {
    ChessPiece.PieceType promotionPiece;
    
    ChessMove move;

    public MakeMove(String authToken, Integer gameID, ChessMove move, ChessPiece.PieceType promotionPiece) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.promotionPiece = promotionPiece;
        this.move = move;
    }

    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    public ChessMove getChessMove() {
        return move;
    }
}
