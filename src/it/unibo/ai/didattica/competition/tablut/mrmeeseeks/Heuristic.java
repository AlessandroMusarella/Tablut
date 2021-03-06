package it.unibo.ai.didattica.competition.tablut.mrmeeseeks;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Heuristic {
    private GameAshtonTablut game;
    private State state;
    private State.Turn turn;
    private KingPosition kingPosition = null;

    private double numWhiteOnBoard = 0; //double to force double division
    private double numBlackOnBoard = 0;

    private final static int NUM_BLACK = 16;
    private final static int NUM_WHITE = 8;

    // White weights
    private final double[] whiteWeights = {30, 25, 5, 10};
    private final static int WHITE_REMAINING  = 0;
    private final static int BLACK_EATEN = 1;
    private final static int MOVES_TO_ESCAPE = 2;
    //private final static int PAWNS_NEAR_KING = 3;

    // Black weights
    private final double[] blackWeights = {30, 45, 3, 15};
    private final static int BLACK_REMAINING  = 0;
    private final static int WHITE_EATEN = 1;
    private final static int PROTECT_ESCAPES = 2;
    private final static int PAWNS_NEAR_KING = 3;

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

        calculateNumPawns();

        value += whiteWeights[WHITE_REMAINING] * getWhiteRemaining();
        value += whiteWeights[BLACK_EATEN] * getBlackEaten();
        value += whiteWeights[MOVES_TO_ESCAPE] * kingMovesToEscape();
        value += whiteWeights[PAWNS_NEAR_KING] * pawnsNearKing(true);

        return value;
    }

    private double evaluateStateBlack() {
        double value = 0;

        calculateNumPawns();

        value += blackWeights[BLACK_REMAINING] * getBlackRemaining();
        value += blackWeights[WHITE_EATEN] * getWhiteEaten();
        value += blackWeights[PROTECT_ESCAPES] * getBlackProtectingEscapes();
        value += blackWeights[PAWNS_NEAR_KING] * pawnsNearKing(false);

        return value;
    }

    /**
     * Must be called before get(White|Black)(Eaten|Remaining) to populate this.num(White|Black)OnBoard
     */
    public void calculateNumPawns() {
        for(int i = 0; i < this.state.getBoard().length; i++) {
            for (int j = 0; j < this.state.getBoard().length; j++) {
                if (this.state.getPawn(i, j).equals(State.Pawn.WHITE))
                    this.numWhiteOnBoard++;
                if (this.state.getPawn(i, j).equals(State.Pawn.BLACK))
                    this.numBlackOnBoard++;
                if (this.state.getPawn(i, j).equals(State.Pawn.KING))
                    this.kingPosition = new KingPosition(i, j);
            }
        }
    }

    /*
    ------------------- WHITE HEURISTIC -------------------
     */

    public double getWhiteRemaining() {
        return this.numWhiteOnBoard / NUM_WHITE;
    }

    public double getBlackEaten() {
        return (NUM_BLACK - this.numBlackOnBoard) / NUM_BLACK;
    }

    public double kingMovesToEscape() {
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
                for(int i = state.getBoard().length - 1; i > xK; i--){      //count positions occupied to the right of the king
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
                for(int i = state.getBoard().length - 1; i > xK; i--){      //count positions occupied to the right of the king
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

    public double pawnsNearKing(boolean isWhitePlaying) {
        if(this.kingPosition == null)
            throw new IllegalStateException("King position must be initialized.");

        //if the king is in the castle or adjacent to the castle it must be surrounded by 4/3 black pawns
        //otherwise 2 black pawn or 1 if it is near to a camp

        //we must avoid that the king, to try to maintain his 4 free side, does not leave the castle

        //give a bonus point if in some side there is a white pawn and therefore it is safe on the other side?
        int numBlack = 0;
        int numWhite = 0;
        int numCitadels = 0;
        for(GameAshtonTablut.Direction dir: GameAshtonTablut.Direction.values()) {
            int newx = this.kingPosition.getX() + dir.getXdiff();
            int newy = this.kingPosition.getY() + dir.getYdiff();
            if(newx > 8 || newx < 0 || newy > 8 || newy < 0) {//I'm out of the board

            } else {
                State.Pawn pawn = this.state.getPawn(newx, newy);
                if(pawn.equals(State.Pawn.BLACK))
                    numBlack++;
                else if(pawn.equals(State.Pawn.WHITE))
                    numWhite++;
                else if(this.isCitadel(newx, newy)) //without else, if I'm near a citadel with a black pawn inside this is counted twice
                    numCitadels++;
            }
        }

        //if white player is playing we return 1 if it is free in all sides
        //if black player is playing we return 1 if it is surrounded by all sides

        if(this.kingPosition.isNearThrone()) { //includes king in the throne
            if(this.kingPosition.getX() == 4 && this.kingPosition.getY() == 4)
                numBlack++; //so when it is near the throne and only requires 3 pawns to be eaten the result is the same
            if(isWhitePlaying)
                return (3 - numBlack)/3.0;
            else//black playing
                return numBlack/3.0;
        } else {
            if(isWhitePlaying)
                return (2 - numBlack - numCitadels)/2.0;
            else//black playing
                return (numBlack + numCitadels)/2.0;
        }
    }

    /*
    ------------------- BLACK HEURISTIC -------------------
     */

    public double getBlackRemaining() {
        return this.numBlackOnBoard / NUM_BLACK;
    }

    public double getWhiteEaten() {
        return (NUM_WHITE - this.numWhiteOnBoard) / NUM_WHITE;
    }

    public int getBlackProtectingEscapes() {;
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

    public boolean isCitadel(int x, int y) {
        int[][] citadels = {{0, 3}, {0, 4}, {0, 5}, {1, 4}, {3, 0}, {4, 0}, {5, 0}, {4, 1}, {8, 3}, {8, 4},
                {8, 5}, {7, 4}, {3, 8}, {4, 8}, {5, 8}, {4, 7}};

        for (int[] citadel : citadels) {
            if (citadel[0] == x && citadel[1] == y)
                return true;
        }
        return false;
    }
}
