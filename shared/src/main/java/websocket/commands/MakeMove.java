package websocket.commands;

import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.annotations.SerializedName;

public class MakeMove extends UserGameCommand {
    ChessPiece promotionPiece;
    
    ChessMove move;

    public MakeMove(String authToken, Integer gameID, ChessMove move, ChessPiece promotionPiece) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.promotionPiece = promotionPiece;
        this.move = move;
    }

    public ChessPiece getPromotionPiece() {
        return promotionPiece;
    }

    public ChessMove getChessMove() {
        return move;
    }
}
