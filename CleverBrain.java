import java.util.Arrays;
import cs5044.tetris.*;

// -------------------------------------------------------------------------
/**
 *  This class attempts to select the best move possible, given a Tetris board
 *  and piece.  The decision of which move to make has a layer of abstraction
 *  provided by strictly comparing each move's attributes against the other
 *  moves' attributes.  Then, each captured field is ranked, and the final
 *  score is derived from each move's ranking in each category except the
 *  number of rows eliminated, which I kept as an absolute score.
 *
 *  @author stevr76
 *  @version 2016.07.20
 */
public class CleverBrain
    implements Brain
{
    //Fields
    private final double oldHolesWeight = -1.0;
    private final double newHolesWeight = -1.8;
    private final double caveWeight = -2.2;
    private final double heightWeight = -1.7;
    private final double rowWeight = -50;
    /**
     * This is the default constructor
     */
    public CleverBrain() {
        //this is the required default constructor. All data is specific to
        //the variables passed to bestMove(), so there was no reason to have
        //any instance variables.
    }
    /**
     * This method attempts to determine the best move available. It takes the
     * board and first examines it to record the pre-move state of the board.
     * Then, it calls tryAllColumns for each rotation of the piece in question.
     * After the scores are received for each possible move in each possible
     * column, the scores are then ranked against each other in the fields of
     * new holes, old holes, post-move height, and a score for caves generated.
     * The move with the minimum score is determined. After using some algebra
     * to turn the ordinal number of the move into a rotation number and a
     * column number, those numbers are applied to the move variable, and the
     * method ends.
     *
     *  @param board is the current board
     *  @param piece is the current piece
     *  @param heightLimit is the height limit of the board.
     *  @param move is the current move
     *
     */
    public void bestMove(
        Board board, Piece piece, int heightLimit, Move move)
    {
        //set up the data regarding the current board
        int[] preHoles = this.getPreMoveHoles(board);
        int[] preHeights = this.getPreMoveHeights(board);
        int maxPreHeight = 0;
        for (int i = 0; i < preHeights.length; i++ ) {
            if (preHeights[i] > maxPreHeight) {
                maxPreHeight = preHeights[i];
            }
        }
        //this holds the number of columns tested for the piece during
        //the given rotation
        int[] rotationHolder = new int[piece.numRotations()];
        //this declares the array that will hold the scores
        //with one entry per column that will be tested
        int[][] scoreArray = new int[piece.numRotations()
            * board.getWidth()][11];
        int rotationCount = 0;
        int totalMoves = 0;
        //the main while loop
        while (rotationCount < piece.numRotations()) {
            rotationHolder[rotationCount] = this.tryAllColumns(board, piece,
                heightLimit, preHoles, preHeights, totalMoves, scoreArray) -
                totalMoves;
            //totalMoves keeps track of columns tested
            totalMoves += rotationHolder[rotationCount];
            piece = piece.nextRotation();
            rotationCount++;
        }
        //start with a large min score
        double min = 10000;
        int moveNumber = 0;
        double score = 0;
        //rank the scores that were added to indices 1-4 of the scores array
        //for that move and put the rankings in indices 5-9 for each move
        this.rankScores(scoreArray);
        //tally each score and put it in index 10 of the array for each
        //move
        for (int i = 0; i < totalMoves; i++) {
            score = newHolesWeight *
                (totalMoves - scoreArray[i][6]) / totalMoves +
                oldHolesWeight *
                (totalMoves - scoreArray[i][7]) / totalMoves +
                caveWeight *
                (totalMoves - scoreArray[i][8]) / totalMoves +
                heightWeight *
                (totalMoves - scoreArray[i][9]) / totalMoves +
                rowWeight * scoreArray[i][10];
            if (scoreArray[i][5] >= heightLimit) {
                score  = (3 - (heightLimit - scoreArray[i][5])) * 100;
            }
            if (score < min) {
                min = score;
                moveNumber = i;
            }
        }
        //extract the rotation count and column number from the ordinal
        //value of the move number using the rotationHolder array that kept
        //track of the number of columns tested in each rotation
        boolean done = false;
        int count = 0;
        int finalRotations = 0;
        int finalColumns = 0;
        while (!done) {
            if (moveNumber - rotationHolder[count] < 0) {
                finalRotations = count;
                finalColumns = moveNumber;
                done = true;
            }
            else {
                moveNumber -= rotationHolder[count];
                count++;
            }
        }
        //set the move
        piece = piece.nthRotation(finalRotations);
        move.setPiece(piece);
        move.setX(finalColumns);
        move.setY(board.rowAfterDrop(piece, finalColumns));
    }
    /**
     * This method gathers the number of holes in the board before the move and
     * inserts the total per column into a corresponding entry in an array.
     * This data will be used by other methods to compare the post-move board
     * with the data collected in this method, the pre-move board.
     *
     * @param board is of type Board. The return data will be extracted from
     * the board.
     * @return int[] holds the number of holes in each column. The size of the
     * array is the board width.
     */
    public int[] getPreMoveHoles(Board board)
    {
        int[] preHoles = new int[board.getWidth()];
        for (int i = 0; i < board.getWidth(); i++) {
            int countPerColumn = 0;
            for (int j = 0; j < board.getColumnHeight(i); j++) {
                if (!board.hasBlockAt(i, j)) {
                    countPerColumn++;
                }
            }
            preHoles[i] = countPerColumn;
        }
        return preHoles;
    }
    /**
     * This method captures the height of each column pre-move in an array.
     *
     * @param board is the board that will provide the data
     * @return value is the int[] with 1 entry per column. The entry is the
     * height of that column.
     */
    public int[] getPreMoveHeights(Board board) {
        int[] preHeights = new int[board.getWidth()];
        for (int i = 0; i < board.getWidth(); i++) {
            preHeights[i]  = board.getColumnHeight(i);
        }
        return preHeights;
    }
    /**
     * This method returns the number of new holes created by a move. It uses
     * the data created in getPreMoveHoles and compares the columns under the
     * current move. If the number of holes is now different, the counter is
     * incremented. The return value is the total number of new holes. This
     * count may be negative.
     *
     * @param board is the board after the move has been placed.
     * @param preHoles contains the data about the board pre-move
     * @param column is the left-most columns of the piece in the move
     * @param pieceWidth is the number of columns in the current piece
     * @return newHoles is the number of new holes created during this
     * move.  The number of new holes will be ranked against other possible
     * moves.  That ranking will be a factor in the final score of this move.
     */
    public int getNewHoleCount(Board board, int[] preHoles, int column,
        int pieceWidth) {
        int newHoles = 0;
        //find all holes under the piece as it is placed
        //subtract the number of holes that were already found and stored
        //in the preHoles array
        for (int i = column; i < column + pieceWidth; i++) {
            int columnHoleCount = 0;
            for (int j = 0; j < board.getColumnHeight(i); j++) {
                if (!board.hasBlockAt(i, j)) {
                    columnHoleCount++;
                }
            }
            newHoles += columnHoleCount - preHoles[i];
        }
        return newHoles;
    }
    /**
     * This method returns the number of pre-move holes that remain covered
     * during the current move. A negative value of new holes means this data
     * will need to be reduced at the end tally method, since the negative new
     * holes will be subtracted from these pre-move holes.
     *
     * @param preHoles contains the pre-move hole data
     * @param column is the left-most column of the current piece
     * @param pieceWidth is the length of the current piece, in columns
     * @return oldHoles returns the number of old holes, which will be
     * ranked against the covered holes of other moves. That rank will be a
     * factor in the final score of the move.
     *
     */
    public int getOldHoleCount(int[] preHoles, int column, int pieceWidth) {
        int oldHoles = 0;
        //sum the columns of the array of preHoles for the span of the piece
        for (int i = column; i < column + pieceWidth; i++) {
            oldHoles += preHoles[i];
        }
        return oldHoles;
    }
    /**
     * This method returns the max height of the board.
     *
     * @param board is the board in question
     * @return value that is the max column height of the board
     */
    public int getMaxHeight(Board board) {
        return board.getLargestHeight();
    }
    /**
     * This method returns the average depth(or height) of up to 4 columns
     * surrounding the piece at its current placement.
     *
     * @param board is the board in question
     * @param column is the left-most column of the current piece
     * @param pieceWidth is the width of the piece
     * @return average depth that will be ranked against the other possible
     * moves and that rank will be a factor in the score of the move
     */
    public int getCaveLevel(Board board, int column, int pieceWidth) {
        int score = 0;
        int count = 0;
        boolean firstLGreaterThan0 = false;
        boolean firstRGreaterThan0 = false;
        //count the score of the farthest right and left columns twice to
        //make up for the lack of surrounding columns due to the boundary
        int factorL = (column == 1) ? 2 : 1;
        int factorR =
            (column + pieceWidth - 1 == board.getWidth() - 2) ? 2 : 1;
        //to avoid caves of width 1, if the first column to the left or right
        //that is checked is a cave, set the flag so that the next columns
        //are ignored. That way, those columns can't cancel the cave score of
        //the first by averaging them and coming up with a score of 0 when
        //there could be a cave 7 spaces deep in the closest column and
        //the next 2 columns are 3 and 4 above the piece
        for (int i = 1; i < 4; i++) {
            if (column - i >= 0) {
                int tempL = (board.getColumnHeight(column) -
                    board.getColumnHeight(column - i));
                if (i == 1 && tempL > 0) {
                    score += tempL * factorL;
                    count += 1;
                    firstLGreaterThan0 = true;
                }
                else if (i == 1) {
                    score += tempL * factorL;
                    count += 1;
                }
                if (i != 1) {
                    if (firstLGreaterThan0) {
                        if (tempL > 0) {
                            score += tempL * factorL;
                            count += 1;
                        }
                    }
                    else {
                        score += tempL * factorL;
                        count += 1;
                    }
                }
            }
            if (column + pieceWidth - 1 + i < board.getWidth()) {
                int tempR = (board.getColumnHeight(column + pieceWidth - 1) -
                    board.getColumnHeight(column + pieceWidth - 1 + i));
                if (i == 1 && tempR > 0) {
                    score += tempR * factorR;
                    count += 1;
                    firstRGreaterThan0 = true;
                }
                else if (i == 1) {
                    score += tempR * factorR;
                    count += 1;
                }
                if (i != 1) {
                    if (firstRGreaterThan0) {
                        if (tempR > 0) {
                            score += tempR * factorR;
                            count += 1;
                        }
                    }
                    else {
                        score += tempR * factorR;
                        count += 1;
                    }
                }
            }
        }
        score = (int)Math.round(1.0 * score / count);
        return score;
    }
    /**
     * This method returns the strict change in max height of the board in a
     * given move.
     *
     * @param board is the board in question
     * @param preMoveHeight is the height prior to calling the clearRows method
     * @return int value of how many rows were cleared by this move
     *
     */
    public int getHeightScore(Board board, int preMoveHeight) {
        return preMoveHeight - board.getLargestHeight();
    }
    /**
     * This method returns the number of rows eliminated by a move. It scans
     * each row with nested for loops.  Every row that does not contain a hole
     * counts as a row eliminated by the move in question.
     *
     * @param board is the board in question
     * @return int value of the number of rows eliminated by the move
     */
    public int getRowKillCount(Board board) {
        int rowKill = 0;
        for (int i = 0; i < board.getLargestHeight(); i++) {
            boolean rowHasHoles = false;
            for (int j = 0; j < board.getWidth(); j++) {
                if (!board.hasBlockAt(j, i)) {
                    rowHasHoles = true;
                }
            }
            if (!rowHasHoles) {
                rowKill++;
            }
        }
        return rowKill;
    }
    /**
     * This method ranks the moves against each other on the basis of cave
     * depth, new holes, and old holes.  The data is kept in a 2d array.  The
     * first array entry corresponds to the ordinal number of the move. The
     * second entry is and array that should contain the scores for each move
     * for 1. new holes 2. old holes 3. cave score 4. height score 5. max height
     * (that score will be the greater than the worst score possible if the
     *  max height of a move is out of bounds).  This method will store entries
     *  for (1),(2),(3), and (4) in a separate array and then sort that holder.
     *  The scores will then be compared to each element in the sorted array
     *  until a match is found. The counter at that time will be the rank of
     *  that score for that move, and those ranks will be saved in elements(6),
     *  (7),(8) and (9) of the 2d array.  Array element (10) will be save for
     *  the row score, which will be absolute, not relative to the other moves
     *  available on the current board.
     *
     *  @param scoreArray is the 2d array that is used to record the score
     *  for each move in cols 1-4. This method records the rank of the
     *  scores in cols 1-4 in the array cols 5-9.
     */
    public void rankScores(int[][] scoreArray) {
        //the score in the 4th column is the height score, which is better as
        //it gets larger. To make up for this, I multiplied the array elements
        //for the column by -1 and sorted them lowest to highest. Then I
        //multiplied by -1 again after sorting, so that the ranking is
        //consistent with the other rankings. The lowest rank number is still
        //the best score.
        int entries = scoreArray.length;
        int[][] holder = new int[5][entries];
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < entries; j++) {
                if (i != 4) {
                    holder[i][j] = scoreArray[j][i];
                }
                else {
                    holder[i][j] = (-1) * scoreArray[j][i];
                }
            }
        }
        for ( int i = 1; i < 5; i++) {
            Arrays.sort(holder[i]);
        }
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < entries; j++) {
                boolean done = false;
                int count = 0;
                while ( !done ) {

                    if (i != 4) {
                        if (scoreArray[j][i]  <= holder[i][count]) {
                            scoreArray[j][i + 5] = count;
                            done = true;
                        }
                    }
                    else {
                        if (scoreArray[j][i]  >= (-1) * holder[i][count]) {
                            scoreArray[j][i + 5] = count;
                            done = true;
                        }
                    }
                    count++;
                }
            }
        }
    }
    /**
     * This method records the scores of a given move for a given roation
     * number in each column of the given board.  The scoring requires the
     * records of the pre-move board that were created in bestMove().  The
     * scores are recored in the scoreArray, which contians one integer array
     * for each column, for each rotation.  The array indexes 6-9 are left
     * vacant, and will hold the rankings of each score in indexes 1-4,
     * respectively.
     *
     * @param board is the board in question
     * @param piece is the piece in question
     * @param heightLimit is the heightlimit of the board
     * @param preHoles contains the number of pre-move holes in each column of
     * the board, with 1 entry per column
     * @param preHeights contains the height of each column of the board before
     * this move
     * @param moveCount tracks the ordinal value of each move.  The
     * characteristics of the move can be determined by the order in which it
     * was tested
     * @param scoreArray is a 2d array for recording the scores of each move
     * @return moveCount is returned to bestMove for tabulating how many moves
     * there were in each rotation.
     */
    public int tryAllColumns(Board board, Piece piece, int heightLimit,
        int[] preHoles, int[] preHeights, int moveCount, int[][] scoreArray) {
        int column = 0;
        int pieceWidth = piece.getWidth();
        while (column < board.getWidth() - pieceWidth + 1) {
            int destRow = board.rowAfterDrop(piece, column);
            int oldHeight = board.getLargestHeight();
            board.place(piece, column, destRow);
            scoreArray[moveCount][1] = this.getNewHoleCount(board, preHoles,
                column, pieceWidth);
            scoreArray[moveCount][2] = this.getOldHoleCount(preHoles, column,
                pieceWidth);
            scoreArray[moveCount][3] = this.getCaveLevel(board, column,
                pieceWidth);
            scoreArray[moveCount][4] = this.getHeightScore(board, oldHeight);
            scoreArray[moveCount][5] = this.getMaxHeight(board);
            scoreArray[moveCount][10] = this.getRowKillCount(board);
            board.undo();
            column++;
            moveCount++;
        }
        return moveCount;
    }
}
