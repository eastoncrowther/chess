package chess;

import java.util.ArrayList;
import java.util.Collection;


public class KnightBehavior implements PieceBehavior{

    @Override
    public Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> knightMoves = new ArrayList<>();

        int[][] steps = {
                {2, 1},
                {2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2},
                {-2, 1},
                {-2, -1}
        };
        // positions span 1 - 8 while the board is indexed 0 - 7

        for (int[] step : steps) {
            int incrementRow = step[0];
            int incrementCol = step[1];

            // get initial position
            int row = myPosition.getRow() - 1;
            int col = myPosition.getColumn() - 1;

            row += incrementRow;
            col += incrementCol;

            // check if position is in bounds
            if (row > 0 && col > 0 && row < 7 && col < 7) {
                ChessPosition nextPosition = new ChessPosition(row + 1, col + 1);
                if (board.getPiece(nextPosition) == null) {
                    // there is no piece on the position
                    knightMoves.add(new ChessMove(myPosition, nextPosition, null));
                } else if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    // there is an enemy piece on the position
                    knightMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
            }
        }

        return knightMoves;
    }
}
