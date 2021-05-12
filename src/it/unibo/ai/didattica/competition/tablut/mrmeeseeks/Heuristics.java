package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Heuristics {
    private State state;
    private State.Turn turn;
    public Heuristics(State state, State.Turn turn) {
        this.state = state;
        this.turn = turn;
    }

    public double evaluateState() {
        if(this.turn.equals(State.Turn.WHITE))
            return evaluateStateWhite();
        else
            return evaluateStateBlack();
    }

    private double evaluateStateBlack() {
        return 0;
    }

    private double evaluateStateWhite() {
        return 0;
    }
}
