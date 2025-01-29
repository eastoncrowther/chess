package chess;

import java.util.ArrayList;
import java.util.Collection;


public class KnightBehavior implements PieceBehavior{

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        Collection<ChessMove> knightMoves = new ArrayList<>();

        int[][] moves = {
                {2, 1},
                {2, -1},
                {1, 2},
                {1, -2},
                {-2, 1},
                {-2, -1},
                {-1, 2},
                {-1, -2}
        };

        for (int[] move : moves) {
            int row = myPosition.getRow() + move[0];
            int col = myPosition.getColumn() + move[1];

            if (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);

                // no piece there
                if (board.getPiece(nextPosition) == null) {
                    knightMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
                else if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    knightMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
            }
        }
        return knightMoves;
    }
    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }

}
