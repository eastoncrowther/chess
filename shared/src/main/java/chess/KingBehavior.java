package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingBehavior implements PieceBehavior {

    @Override
    public Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> kingMoves = new ArrayList<>();

        int[][] steps = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1},
                {0, 1},
                {0, -1},
                {1, 0},
                {-1, 0}
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
            if (row >= 0 && col >= 0 && row <= 7 && col <= 7) {
                // get new position
                ChessPosition nextPosition = new ChessPosition(row + 1, col + 1);

                //check if there is a piece at the new position
                if (board.getPiece(nextPosition) == null) {
                    kingMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
                // if there is an enemy piece at the position
                else if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    kingMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
                // no add if the piece is on the same team as the king
            }
        }
        return kingMoves;
    }
}
