package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import aima.core.search.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ParallellDeepeningAlphaBetaSearchWithHeuristic extends IterativeDeepeningAlphaBetaSearchWithHeuristic{
    public ParallellDeepeningAlphaBetaSearchWithHeuristic(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
        this.timer = new Timer(time);
    }

    @Override
    public Action makeDecision(State state) {
        java.util.Date date = new java.util.Date();
        StringBuffer logText = null;
        State.Turn player = game.getPlayer(state);
        List<Action> results = orderActions(state, game.getActions(state), player, 0);
        timer.start();
        int NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;
        int NUM_THREADS_USED = 4;
        System.out.println("Num processors: " + NUM_THREADS);
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS_USED);
        currDepthLimit = 0;
        do {
            incrementDepthLimit();
            heuristicEvaluationUsed = false;
            ActionStore<Action> newResults = new ActionStore<>();
            Set<Future<List<ActionValue>>> set = new HashSet<>();
            int actionPerThread = results.size()/NUM_THREADS_USED;
            for(int i = 0; i < NUM_THREADS_USED; i++) {
                int maxNum = i == NUM_THREADS_USED - 1 ? results.size() : actionPerThread * (i + 1);
                Callable<List<ActionValue>> callable1 = new MyTask(state, results.subList(actionPerThread*i, maxNum), player);
                Future<List<ActionValue>> future1 = pool.submit(callable1);
                set.add(future1);
                //System.out.println(actionPerThread*i + "->" + maxNum);
            }

            try {
                for (Future<List<ActionValue>> future : set) {
                    List<ActionValue> actionValues = future.get();
                    for(ActionValue actionValue: actionValues)
                        newResults.add(actionValue.action, actionValue.value);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            //System.out.println("actions: " + results.size() + " newResults: " + newResults.size() + " curDepth: " + currDepthLimit);
            if (newResults.size() > 0) {
                results = newResults.actions;
                if (!timer.timeOutOccurred()) {
                    if (hasSafeWinner(newResults.utilValues.get(0)))
                        break; // exit from iterative deepening loop
                    else if (newResults.size() > 1
                            && isSignificantlyBetter(newResults.utilValues.get(0), newResults.utilValues.get(1)))
                        break; // exit from iterative deepening loop
                }
            }
        } while (!timer.timeOutOccurred() && heuristicEvaluationUsed);
        java.util.Date dateEnd = new java.util.Date();
        int time = (int)(dateEnd.getTime()-date.getTime())/1000;
        System.out.println("Expanded nodes = " + getMetrics().get(METRICS_NODES_EXPANDED) + " , maximum depth = " + getMetrics().get(METRICS_MAX_DEPTH) + ", time = " + time + " s");
        System.out.println("Expanded nodes (atomic) = " + counter.get());
        System.out.println(dateEnd);
        return results.get(0);
    }
    public class MyTask implements Callable<List<ActionValue>> {
        private State state;
        private List<Action> action;
        private State.Turn player;

        public MyTask(State state, List<Action> action, State.Turn player) {
            this.state = state;
            this.action = action;
            this.player = player;
        }


        @Override
        public List<ActionValue> call() {
            List<ActionValue> actionValues = new ArrayList<>();
            for(Action act: action) {
                double value = minValue(game.getResult(state, act), player, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, 1);
                actionValues.add(new ActionValue(act, value));
            }
            return actionValues;
        }
    }
    public class ActionValue {
        public Action getAction() {
            return action;
        }

        public double getValue() {
            return value;
        }

        private final Action action;
        private final double value;
        public ActionValue(Action action, double value) {
            this.action = action;
            this.value = value;
        }

    }

}
