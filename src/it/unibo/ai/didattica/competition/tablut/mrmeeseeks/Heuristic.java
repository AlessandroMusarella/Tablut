package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Heuristic {
    private GameAshtonTablut game;
    private State state;
    private State.Turn turn;
    private KingPosition kingPosition = null;

    private final static int NUM_BLACK = 16;
    private final static int NUM_WHITE = 8;

    // White weights
    private final double[] whiteWeights = {15, 25, 5};
    private final static int WHITE_REMAINING  = 0;
    private final static int BLACK_EATEN = 1;
    private final static int MOVES_TO_ESCAPE = 2;

    // White weights
    private final double[] blackWeights = {20, 15, 19};
    private final static int BLACK_REMAINING  = 0;
    private final static int WHITE_EATEN = 1;
    private final static int PROTECT_ESCAPES = 2;


    public Heuristic(State state, State.Turn turn) {
        this.state = state;
        this.turn = turn;
    }

    public double evaluateState() {
        if(this.turn.equals(State.Turn.WHITE))
            return evaluateStateWhite();
        else
            return evaluateStateBlack();
    }

    private double evaluateStateWhite() {
        double value = 0;

        value += whiteWeights[WHITE_REMAINING] * getWhiteRemaining();
        value += whiteWeights[BLACK_EATEN] * getBlackEaten();
        value += whiteWeights[MOVES_TO_ESCAPE] * kingMovesToEscape();

        return value;
    }

    private double evaluateStateBlack() {
        double value = 0;

        value += blackWeights[BLACK_REMAINING] * getBlackRemaining();
        value += blackWeights[WHITE_EATEN] * getWhiteEaten();
        value += blackWeights[PROTECT_ESCAPES] * getBlackProtectingEscapes();

        return value;
    }


    /*
    ------------------- WHITE HEURISTIC -------------------
     */

    public int getWhiteRemaining() {
        int white = 0;
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.WHITE))
                    white++;
                if (this.state.getPawn(i, j).equals(State.Pawn.KING))
                    this.kingPosition = new KingPosition(i, j);
            }
        }

        return white;
    }

    public int getBlackEaten() {
        int black = 0;
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.BLACK))
                    black++;
                if (this.state.getPawn(i, j).equals(State.Pawn.KING))
                    this.kingPosition = new KingPosition(i, j);
            }
        }

        return NUM_BLACK - black;
    }

    public int kingMovesToEscape() {
        int cont = 0;

        if(this.kingPosition == null)
            throw new IllegalStateException("King position must be initialized.");

        if (!kingPosition.isNearThrone()){
            int xK = kingPosition.getX();
            int yK = kingPosition.getY();
            int occupiedPositionsDOWN = -1;
            int occupiedPositionsUP = -1;
            int occupiedPositionsLEFT = -1;
            int occupiedPositionsRIGHT = -1;

            if (!(xK >= 3 && xK <= 5) && !(yK >= 3 && yK <= 5)){        //no safe position
                occupiedPositionsDOWN = 0;
                occupiedPositionsUP = 0;
                occupiedPositionsLEFT = 0;
                occupiedPositionsRIGHT = 0;
                for (int i = 0; i < yK; i++){       //count positions occupied above the king
                    if (!state.getPawn(xK, i).equals(State.Pawn.EMPTY))
                        occupiedPositionsUP++;
                }
                for(int i = state.getBoard().length - 1; i > yK; i--){      //count positions occupied under the king
                    if (!state.getPawn(xK, i).equals(State.Pawn.EMPTY))
                        occupiedPositionsDOWN++;
                }
                for (int i = 0; i < xK; i++){       //count positions occupied to the left of the king
                    if (!state.getPawn(i, yK).equals(State.Pawn.EMPTY))
                        occupiedPositionsLEFT++;
                }
                for(int i = state.getBoard().length - 1; i < xK; i--){      //count positions occupied to the right of the king
                    if (!state.getPawn(i, yK).equals(State.Pawn.EMPTY))
                        occupiedPositionsRIGHT++;
                }
            }
            if (xK >= 3 && xK <= 5){        //safe row
                occupiedPositionsDOWN = 0;
                occupiedPositionsUP = 0;
                for (int i = 0; i < yK; i++){       //count positions occupied above the king
                    if (!state.getPawn(xK, i).equals(State.Pawn.EMPTY))
                        occupiedPositionsUP++;
                }
                for(int i = state.getBoard().length - 1; i > yK; i--){      //count positions occupied under the king
                    if (!state.getPawn(xK, i).equals(State.Pawn.EMPTY))
                        occupiedPositionsDOWN++;
                }
            }
            if(yK >= 3 && yK <= 5) {        //safe col
                occupiedPositionsLEFT = 0;
                occupiedPositionsRIGHT = 0;
                for (int i = 0; i < xK; i++){       //count positions occupied to the left of the king
                    if (!state.getPawn(i, yK).equals(State.Pawn.EMPTY))
                        occupiedPositionsLEFT++;
                }
                for(int i = state.getBoard().length - 1; i < xK; i--){      //count positions occupied to the right of the king
                    if (!state.getPawn(i, yK).equals(State.Pawn.EMPTY))
                        occupiedPositionsRIGHT++;
                }
            }

            if (occupiedPositionsDOWN == 0)
                cont++;
            if (occupiedPositionsUP == 0)
                cont++;
            if (occupiedPositionsLEFT == 0)
                cont++;
            if (occupiedPositionsRIGHT == 0)
                cont++;

        }

        return cont;
    }

    public double pawnsNearKing() {
        if(this.kingPosition == null)
            throw new IllegalStateException("King position must be initialized.");

        //if the king is in the castle or adjacent to the castle it must be surrounded by 4/3 black pawns
        //otherwise 2 black pawn or 1 if it is near to a camp

        //we must avoid that the king, to try to maintain his 4 free side, does not leave the castle

        //give a bonus point if in some side there is a white pawn and therefore it is safe on the other side?
        int numBlack = 0;
        int numWhite = 0;
        for(GameAshtonTablut.Direction dir: GameAshtonTablut.Direction.values()) {
            int newx = this.kingPosition.getX() + dir.getXdiff();
            int newy = this.kingPosition.getY() + dir.getYdiff();
            if(newx > 8 || newx < 0 || newy > 8 || newy < 0) {//I'm out of the board

            } else {
                State.Pawn pawn = this.state.getPawn(newx, newy);
                if(pawn.equals(State.Pawn.BLACK))
                    numBlack++;
                if(pawn.equals(State.Pawn.WHITE))
                    numWhite++;
            }
        }


        if(this.kingPosition.isNearThrone()) { //includes king in the throne
            return 0.0;
        } else {
            return 0.0;
        }
    }

    /*
    ------------------- BLACK HEURISTIC -------------------
     */

    public int getBlackRemaining() {
        int black = 0;
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.BLACK))
                    black++;
            }
        }

        return black;
    }

    public int getWhiteEaten() {
        int white = 0;
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.WHITE))
                    white++;
            }
        }

        return NUM_WHITE - white;
    }




    public double getBlackProtectingEscapes() {
        int[][] protectPositions = {{1, 2}, {2, 1}, {6, 1}, {7, 2}, {1, 6}, {2, 7}, {6, 7}, {7, 6}};
        int num = 0;
        for(int[] position : protectPositions) {
            if(this.state.getPawn(position[0], position[1]).equals(State.Pawn.BLACK))
                num++;
        }
        return num;
    }

    /*
    ------------- KingPosition ----------------
     */

    public class KingPosition {
        private final int x, y;
        public KingPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int[] get() {
            return new int[] {x, y};
        }

        public boolean isNearThrone(){
            // 3 -> 5 inclusive is near the throne
            return getX() >= 3 && getX() <= 5 &&
                    getY() >= 3 && getY() <= 5;
        }
        
        @Override
        public String toString() {
            return "KingPosition{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
