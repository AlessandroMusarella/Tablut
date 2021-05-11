package aima.core.search.adversarial;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;

import java.io.PrintStream;

public class RevisedIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>{
    public RevisedIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    protected double eval(State state, State.Turn player) {
        super.eval(state, player);
        return this.game.getUtility(state, player);
    }

    public Action makeDecision(State state) {
        Action a = super.makeDecision(state);
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + this.getMetrics().get(METRICS_MAX_DEPTH));
        return a;
    }
}
