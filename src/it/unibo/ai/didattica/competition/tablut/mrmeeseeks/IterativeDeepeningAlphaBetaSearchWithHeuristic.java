package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class IterativeDeepeningAlphaBetaSearchWithHeuristic extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

    public IterativeDeepeningAlphaBetaSearchWithHeuristic(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }

    /**
     * We need to override this to put heuristic in intermediate states and not only in final states
     * @param state the current state
     * @param turn the player playing
     * @return the score of this state
     */
    @Override
    protected double eval(State state, State.Turn turn) {
        super.eval(state, turn);

        return game.getUtility(state, turn);
    }


}
