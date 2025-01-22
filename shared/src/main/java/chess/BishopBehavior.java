package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static chess.ChessPiece.PieceType.BISHOP;

public class BishopBehavior implements PieceBehavior{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();

        // the position is 1 - 8 while the board itself only has indexes 0 - 7

        int[][] steps = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        // calculate my team color
        ChessGame.TeamColor myTeamColor = board.getPiece(myPosition).getTeamColor();

        for (int[] step : steps) {
            int incrementRow = step[0];
            int incrementCol = step[1];

            // initial positions on the board 0 based indexing
            int row = myPosition.getRow() - 1;
            int col = myPosition.getColumn() - 1;

            while (true) {
                // increment positions
                row += incrementRow;
                col += incrementCol;

                // check if out of bounds: 0 based indexing
                if (row < 0 || col < 0 || row > 7 || col > 7) {
                    break;
                }
                // make new position: 1 based indexing
                ChessPosition nextPosition = new ChessPosition(row + 1, col + 1);

                ChessPiece pieceOnSquare = board.getPiece(nextPosition);

                if (pieceOnSquare == null) {
                    bishopMoves.add(new ChessMove(myPosition, nextPosition, BISHOP));
                }
                else {
                    if (pieceOnSquare.getTeamColor() != myTeamColor) {
                        bishopMoves.add(new ChessMove(myPosition, nextPosition, BISHOP));
                    }
                    break;
                }
            }
        }
        return bishopMoves;
    }
}
