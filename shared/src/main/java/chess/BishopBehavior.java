package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopBehavior implements PieceBehavior{
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();

        int[][] diagonals = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] diagonal : diagonals) {
            int row = myPosition.getRow() + diagonal[0];
            int col = myPosition.getColumn() + diagonal[1];

            while (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);
                if (board.getPiece(nextPosition) != null) {
                    if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        // add the move then break
                        bishopMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    break;
                }
                else {
                    // add the move
                    bishopMoves.add(new ChessMove(myPosition, nextPosition, null));
                    row += diagonal[0];
                    col += diagonal[1];
                }
            }
        }
        return bishopMoves;
    }
    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }

}
