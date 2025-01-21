package chess;

import java.util.Collection;
import static chess.ChessPiece.PieceType.BISHOP;

public class BishopBehavior implements PieceBehavior{
    // should be a collection of the chess moves.
    private Collection<ChessMove> bishopMoves;

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        // generate moves to the top right corner.
        for (int row = myPosition.getRow() + 1, col = myPosition.getColumn() + 1; row <= 8 && col <= 8; row++, col++) {
            bishopMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), BISHOP));
        }
        // generate moves to the bottom left corner.
        for (int row = myPosition.getRow() - 1, col = myPosition.getColumn() + 1; row <= 8 && col <= 8; row--, col++) {
            bishopMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), BISHOP));
        }
        // generate moves to the top left corner.
        for (int row = myPosition.getRow() + 1, col = myPosition.getColumn() - 1; row <= 8 && col <= 8; row++, col--) {
            bishopMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), BISHOP));
        }
        // generate moves to the bottom left corner.
        for (int row = myPosition.getRow() - 1, col = myPosition.getColumn() - 1; row <= 8 && col <= 8; row--, col--) {
            bishopMoves.add(new ChessMove(myPosition, new ChessPosition(row, col), BISHOP));
        }

        return bishopMoves;
    }
    /* upDown should be entered as + for up and - for down. leftRight should be - for left and + for right */
    public void calculateDirection (char upDown, char leftRight, Collection<ChessMove> moves, ChessPosition myPosition) {

    }
}
