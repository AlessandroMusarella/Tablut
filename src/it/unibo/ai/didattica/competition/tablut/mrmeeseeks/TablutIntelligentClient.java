package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.*;

import java.io.IOException;
import java.net.UnknownHostException;

public class TablutIntelligentClient extends TablutClient {

    protected int timeout;

    public TablutIntelligentClient(String player, String name, int gameChosen, int timeout, String ipAddress) throws UnknownHostException, IOException {
        super(player, name, timeout, ipAddress);
        this.timeout = timeout;
    }

    public TablutIntelligentClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
        this(player, name, 4, timeout, ipAddress);
    }

    public TablutIntelligentClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
        this(player, "random", 4, timeout, ipAddress);
    }

    public TablutIntelligentClient(String player) throws UnknownHostException, IOException {
        this(player, "random", 4, 60, "localhost");
    }


    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        int gametype = 4;
        String role = "";
        String name = "random";
        String ipAddress = "localhost";
        int timeout = 60;
        // TODO: change the behavior?
        if (args.length < 1) {
            System.out.println("You must specify which player you are (WHITE or BLACK)");
            System.exit(-1);
        } else {
            System.out.println(args[0]);
            role = (args[0]);
        }
        if (args.length == 2) {
            System.out.println(args[1]);
            timeout = Integer.parseInt(args[1]);
        }
        if (args.length == 3) {
            ipAddress = args[2];
        }
        System.out.println("Selected client: " + args[0]);

        TablutIntelligentClient client = new TablutIntelligentClient(role, name, gametype, timeout, ipAddress);
        client.run();
    }

    @Override
    public void run() {

        try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        State state = new StateTablut();
        state.setTurn(State.Turn.WHITE);
        GameAshtonTablut rules = new GameAshtonTablut(0, -1, "log", "white", "black");
        System.out.println("Ashton Tablut game");

        System.out.println("You are player " + this.getPlayer().toString() + "!");

        while (true) {
            try {
                this.read();
            } catch (ClassNotFoundException | IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
            System.out.println("Current state:");
            state = this.getCurrentState();
            System.out.println(state.toString());


            if (this.getPlayer().equals(State.Turn.WHITE)) {
                // Mio turno
                if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {

                    IterativeDeepeningAlphaBetaSearchWithHeuristic search = new IterativeDeepeningAlphaBetaSearchWithHeuristic(rules, Double.MIN_VALUE, Double.MAX_VALUE, this.timeout - 2);

                    Action a = search.makeDecision(state);

                    System.out.println("Mossa scelta: " + a.toString());
                    try {
                        this.write(a);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }

                }
                // Turno dell'avversario
                else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
                    System.out.println("Waiting for your opponent move... ");
                }
                // ho vinto
                else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                }
                // ho perso
                else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                }
                // pareggio
                else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }

            } else {

                // Mio turno
                if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {

                    IterativeDeepeningAlphaBetaSearchWithHeuristic search = new IterativeDeepeningAlphaBetaSearchWithHeuristic(rules, Double.MIN_VALUE, Double.MAX_VALUE, this.timeout - 2);

                    Action a = search.makeDecision(state);

                    System.out.println("Mossa scelta: " + a.toString());
                    try {
                        this.write(a);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }

                }

                else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
                    System.out.println("Waiting for your opponent move... ");
                } else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                } else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                } else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }

            }
        }

    }
}