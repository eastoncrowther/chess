package ui;

import chess.*;

import java.util.Objects;

import static ui.EscapeSequences.*;

public class PrintBoard {
    private ChessBoard chessBoard;

    public PrintBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public String printWhiteBoard() {
        StringBuilder boardString = new StringBuilder();
        boardString.append(ERASE_SCREEN);
        boardString.append("  a   b   c   d   e   f   g   h\n");
        for (int row = 8; row >= 1; row--) {
            boardString.append(row).append(" ");
            for (int col = 1; col <= 8; col++) {
                placePiece(row, col, boardString, "WHITE");
            }
            boardString.append(" ").append(row).append("\n");
        }
        boardString.append("  a   b   c   d   e   f   g   h\n");
        return boardString.toString();
    }

    public String printBlackBoard() {
        StringBuilder boardString = new StringBuilder();
        boardString.append(ERASE_SCREEN);
        boardString.append("  h   g   f   e   d   c   b   a\n");
        for (int row = 1; row <= 8; row++) {
            boardString.append(row).append(" ");
            for (int col = 8; col >= 1; col--) {
                placePiece(row, col, boardString, "BLACK");
            }
            boardString.append(" ").append(row).append("\n");
        }
        boardString.append("  h   g   f   e   d   c   b   a\n");
        return boardString.toString();
    }

    private void placePiece (int row, int col, StringBuilder boardString, String teamColor) {
        ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
        boardString.append(getCellColor(row, col, teamColor));
        boardString.append(getPieceRepresentation(piece)).append(RESET_BG_COLOR);
    }

    private String getPieceRepresentation(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private String getCellColor(int row, int col, String teamColor) {
        if (Objects.equals(teamColor, "WHITE")) {
            return ((row + col) % 2 == 0) ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
        } else {
            return ((row + col) % 2 == 0) ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        }
    }
    void setChessBoard (ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }
}



