package ui;

import chess.ChessBoard;
import server.websocket.NotificationHandler;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;
    private final InGameClient inGameClient;
    private State state;
    private PrintBoard printer;

    public Repl(String serverUrl, State state) {
        preLoginClient = new PreLoginClient(serverUrl);
        postLoginClient = new PostLoginClient(serverUrl, null);
        inGameClient = new InGameClient(serverUrl, null);
        this.state = state;
        printer = new PrintBoard(null);
    }

    public void run() {
        // things to print on startup
        System.out.println("Welcome to chess. Sign in to start.");
        System.out.println(preLoginClient.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            switch (state) {
                case LOGGEDOUT -> System.out.print(SET_TEXT_COLOR_BLUE + "[logged out] > " + RESET_TEXT_COLOR);
                case LOGGEDIN -> System.out.print(SET_TEXT_COLOR_RED + "[logged in] > " + RESET_TEXT_COLOR);
                case INCHESSGAME -> System.out.print(SET_TEXT_COLOR_YELLOW + "[in game] > " + RESET_TEXT_COLOR);
            }
            String line = scanner.nextLine();
            try {
                if (state.equals(State.INCHESSGAME)) {
                    inGameClient.setState(state);
                    inGameClient.setAuth(postLoginClient.getAuth());
                    result = inGameClient.eval(line);
                    state = postLoginClient.getState();
                }
                else if (state.equals(State.LOGGEDIN)) {

                    postLoginClient.setState(state);
                    postLoginClient.setAuth(preLoginClient.getAuth());
                    result = postLoginClient.eval(line);
                    state = postLoginClient.getState();
                }
                else {
                    preLoginClient.setState(State.LOGGEDOUT);
                    result = preLoginClient.eval(line);
                    state = preLoginClient.getState();
                }
                System.out.print(result);
            } catch (Throwable e) {
                String message = e.toString();
                System.out.print(message);
            }
            System.out.println();
        }
    }

    @Override
    public void handleError(ErrorMessage errorMessage) {
        System.out.println(errorMessage.getErrorMessage());
    }

    @Override
    public void handleLoadGame(LoadGameMessage loadGameMessage) {
        ChessBoard currentBoard = loadGameMessage.getGame().getBoard();
        printer.setChessBoard(currentBoard);
        System.out.print(printer.printWhiteBoard());
    }

    @Override
    public void handleNotification(NotificationMessage notificationMessage) {
        System.out.println(notificationMessage.getMessage());
    }
}
