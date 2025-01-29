package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingBehavior implements PieceBehavior {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        Collection<ChessMove> kingMoves = new ArrayList<>();

        int[][] moves = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] move : moves) {
            int row = myPosition.getRow() + move[0];
            int col = myPosition.getColumn() + move[1];

            if (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);

                // no piece there
                if (board.getPiece(nextPosition) == null) {
                    kingMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
                else if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    kingMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
            }
        }
        return kingMoves;
    }
    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }
}
