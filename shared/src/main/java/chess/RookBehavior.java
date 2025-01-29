package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookBehavior implements PieceBehavior{

    @Override
    public Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> rookMoves = new ArrayList<>();

        int[][] diagonals = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] diagonal : diagonals) {
            int row = myPosition.getRow() + diagonal[0];
            int col = myPosition.getColumn() + diagonal[1];

            while (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);
                if (board.getPiece(nextPosition) != null) {
                    if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        // add the move then break
                        rookMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    break;
                }
                else {
                    // add the move
                    rookMoves.add(new ChessMove(myPosition, nextPosition, null));
                    row += diagonal[0];
                    col += diagonal[1];
                }
            }
        }
        return rookMoves;
    }
    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }
}
