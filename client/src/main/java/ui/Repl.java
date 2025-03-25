package ui;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;
    private State state;

    public Repl(String serverUrl, State state) {
        preLoginClient = new PreLoginClient(serverUrl);
        postLoginClient = new PostLoginClient(serverUrl, null);
        this.state = state;
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
                    System.out.println("IN CHESS GAME REPL");
                }
                else if (state.equals(State.LOGGEDIN)) {

                    postLoginClient.setState(state);
                    postLoginClient.setAuth(preLoginClient.getAuth());
                    result = postLoginClient.eval(line);
                    state = postLoginClient.getState();
                }
                else {
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
}
