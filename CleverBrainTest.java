import static org.junit.Assert.*;
import cs5044.tetris.*;
import java.util.Arrays;
// -------------------------------------------------------------------------
/**
 *  This is the test class for CleverBrain
 *
 *  @author stevr76
 *  @version 2016.07.20
 */
public class CleverBrainTest
    extends student.TestCase
{
    /**
     * This class has a CleverBrain object and a board object as its instance
     * variables.
     */

    private CleverBrain cleverBrain;
    private Board start1;
    /**
     * This contains a cleverBrain object and a board object although most
     * of the tests alter the board object.
     */
    public void setUp()
        throws Exception
    {
        cleverBrain = new CleverBrain();
        start1 = new Board(10, 24,
            "    #     ",
            "#### #####"
        );
    }
    /**
     * This test ensures that the count of existing holes in a board is
     * accurate.  There is 1 entry per column.
     */
    public void testGetPreMoveHoles() {
        start1 = new Board(10, 24,
            "    #     ",
            "#### #####"
        );
        int[] answer = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        int[] testAnswer = new int[10];
        testAnswer = cleverBrain.getPreMoveHoles(start1);
        assertTrue(Arrays.equals(answer, testAnswer));
    }
    /**
     * This test ensures that the recording of pre-move heights in each column
     * are recorded accurately, with 1 entry per column.
     */
    public void testGetPreMoveHeights() {
        start1 = new Board(10, 24,
            "    ##    ",
            "####  ####"
        );
        int[] answer = {1, 1, 1, 1, 2, 2, 1, 1, 1, 1};
        int[] testAnswer = new int[10];
        testAnswer = cleverBrain.getPreMoveHeights(start1);
        assertTrue(Arrays.equals(answer, testAnswer));
    }
    /**
     * This test ensures that new holes are determined correctly. Only the
     * columns beneath the current piece should be counted, and old holes
     * should not be included.  The integer returned should be the count
     * of new holes created by the potential move.
     */
    public void testGetNewHoleCount() {
        start1 = new Board(10, 24,
            "    ####  ",
            "    #     ",
            "#### #####"
        );
        int[] preHoles = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0};
        int testAnswer = cleverBrain.getNewHoleCount(start1, preHoles, 4, 4);
        assertEquals(testAnswer, 3);
    }
    /**
     * This is a test to ensure that old holes under a given piece are summed
     * correctly.  The integer returns is the sum of the columns under a piece
     * from the preHoles array.
     */
    public void testGetOldHolesCount() {
        int[] preHoles = {0, 0, 0, 0, 1, 0, 2, 0, 0, 3};
        int testAnswer = cleverBrain.getOldHoleCount(preHoles, 4, 4);
        assertEquals(testAnswer, 3);
    }
    /**
     * This test ensures that the post-move max height is returned correctly.
     */
    public void testGetMaxHeight() {
        start1 = new Board(10, 24,
            "    ####  ",
            "    #     ",
            "#### #####"
        );
        int testAnswer = cleverBrain.getMaxHeight(start1);
        assertEquals(testAnswer, 3);
    }

    /**
     * This test ensures 3 things. 1. If the situation is regular, the
     * getCaveLevel method should return the average difference in depth
     * between the piece and its neighboring columns. 2. If the neighboring
     * columns with distance 1 are lower than the piece, but the further
     * neighbors are above the piece, the higher numbers are ignored, so that
     * pits do not form because the average of the first columns and others
     * send a good score for a bad situation. 3. If there is 1 column between
     * the edge of the piece and the board boundary, that column receives
     * double the depth, so that a bad score in the column closest to the wall
     * cannot be muted as much by 4 columns toward the inside of the board.
     */
    public void testGetCaveLevel() {
        start1 = new Board(10, 24,
            "    ####  ",
            "    #     ",
            "    #     ",
            "#### #####"
        );
        int testAnswer = cleverBrain.getCaveLevel(start1, 4, 4);
        assertEquals(testAnswer, 3);
        start1 = new Board(10, 24,
            "##       #",
            "##  #### #",
            "##  #    #",
            "##  #    #",
            "#### #####"
        );
        int testAnswer2 = cleverBrain.getCaveLevel(start1, 4, 4);
        assertEquals(testAnswer2, 3);
        start1 = new Board(10, 24,
            "####    ##",
            "###### ###",
            "#####   ##",
            "#####   ##",
            "#### #####"
        );
        int testAnswer3 = cleverBrain.getCaveLevel(start1, 4, 4);
        assertEquals(testAnswer3, -1);
        start1 = new Board(10, 24,

            " ######## ",
            " ######## ",
            " ######## "
        );
        int testAnswer4 = cleverBrain.getCaveLevel(start1, 1, 1);
        assertEquals(testAnswer4, 2);
        int testAnswer5 = cleverBrain.getCaveLevel(start1, 8, 1);
        assertEquals(testAnswer5, 2);
    }

    /**
     * This test ensures the correctness of the height score, which is the
     * difference beween the pre-move and post-move heights.
     */
    public void testGetHeightScore() {
        start1 = new Board(10, 24,
            "    ####  ",
            "# ########",
            "## #######",
            "#### #####"
        );
        int preHeight = cleverBrain.getMaxHeight(start1);
        start1 = new Board(10, 24,
            "    ####  ",
            "#### #####"
        );
        int testAnswer = cleverBrain.getHeightScore(start1, preHeight);
        assertEquals(testAnswer, 2);
    }
    /**
     * This test ensures that the number of rows eliminated by a move is
     * recorded accurately.
     */
    public void testGetRowKillCount() {
        Board start2 = new Board(10, 24,
            "    ######",
            "######## #",
            "## #######",
            "######## #"
        );
        Piece myPiece = Piece.getPiece(Piece.STICK, 1);
        start2.place(myPiece, 0, 3);
        int[] preHeights = {3, 3, 3, 3, 4, 4, 4, 4, 3, 4};
        int testAnswer =
            cleverBrain.getRowKillCount(start2);
        assertEquals(testAnswer, 1 );
        start1 = new Board(10, 24,
            "    ######",
            "## #######",
            "## #######",
            "## #######"
        );
        myPiece = Piece.getPiece(Piece.STICK, 0);
        start1.place(myPiece, 2, 0);
        int[] preHeights2 = {3, 3, 0, 3, 4, 4, 4, 4, 4, 4};
        int testAnswer2 =
            cleverBrain.getRowKillCount(start1);
        assertEquals(testAnswer2, 3);
    }
    /**
     * This test ensures that score ranking is accurate and correctly compares
     * indexes 1-4 and puts the ranks in columns 5-9, respectively. Note that
     * the 4th score, the value determines by getHeightScore, ranks from
     * highest to lowest, while 1-3 are ranked from lowest score as the best
     * to the highest score as the worst.
     */
    public void testRankScores() {
        int[][] testArray = new int[6][10];
        for (int i = 0; i < 3; i++) {
            testArray[i][1] = 6;
            testArray[i][2] = 6;
            testArray[i][3] = 6;
            testArray[i][4] = 6;
        }
        for (int i = 3; i < 6; i++) {
            testArray[i][1] = 3;
            testArray[i][2] = 3;
            testArray[i][3] = 3;
            testArray[i][4] = 3;
        }
        testArray[0][1] = 1;
        testArray[0][2] = 1;
        testArray[0][3] = 1;
        testArray[0][4] = 1;
        cleverBrain.rankScores(testArray);
        int[] test1 = {0, 6, 6, 6, 6, 0, 4, 4, 4, 0};
        int[] test2 = {0, 3, 3, 3, 3, 0, 1, 1, 1, 2};
        int[] test3 = {0, 1, 1, 1, 1, 0, 0, 0, 0, 5};
        assertTrue(Arrays.equals(testArray[1], test1));
        assertTrue(Arrays.equals(testArray[4], test2));
        assertTrue(Arrays.equals(testArray[0], test3));
    }
    /**
     * This test ensures that the correct moveNumber is recored by
     *tryAllColumns.
     */
    public void testTryAllColumns() {
        start1 = new Board(5, 6,
            "     ",
            "#### "
        );
        int[] preHoles = cleverBrain.getPreMoveHoles(start1);
        int[] preHeights = cleverBrain.getPreMoveHeights(start1);
        int[][] scoreArray = new int[20][11];

        Piece piece = Piece.getPiece(Piece.STICK, 0);

        int testAnswer = cleverBrain.tryAllColumns(start1, piece, 6, preHoles,
            preHeights, 0, scoreArray);
        assertEquals(testAnswer, 5);
    }
    /**
     * This test ensures that the best move is found in a very basic situation.
     */
    public void testBestMove() {
        start1 = new Board(10, 24,

            "    ######"
        );
        Move move = new Move();
        Piece thePiece = Piece.getPiece(Piece.STICK, 1);
        thePiece = thePiece.nextRotation();
        int heightLimit = 24;
        cleverBrain.bestMove(start1, thePiece, heightLimit, move);
        assertEquals(move.piece().getHeight(), 1);
        assertEquals(move.x(), 0);
        assertEquals(move.y(), 0);
        start1 = new Board(10, 12,

            "          ",
            "          ",
            "##  ######",
            "### ######",
            "# # ######",
            "### ######",
            "# ########"
        );
        move = new Move();
        thePiece = Piece.getPiece(Piece.STICK, 1);
        thePiece = thePiece.nextRotation();
        heightLimit = 8;
        cleverBrain.bestMove(start1, thePiece, heightLimit, move);
        assertEquals(move.piece().getHeight(), 4);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 1);
    }
    /**
     * This contains the suggested test cases for board 1
     */
    public void testBoard1() {
        start1 = new Board(10, 12,
            "          ",
            "          ",
            "          ",
            "#### #####"
        );
        Move move = new Move();
        Piece thePiece = Piece.getPiece(Piece.STICK, 0);
        cleverBrain.bestMove(start1, thePiece, 8, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.LEFT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.RIGHT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.LEFT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 2);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.RIGHT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.T, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        move = new Move();
        thePiece = Piece.getPiece(Piece.SQUARE, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 0);
        assertEquals(move.y(), 1);
    }
    /**
     * This test method contains the suggested test cases for board 2
     */
    public void testBoard2() {
        start1 = new Board(10, 12,

            "          ",
            "####  ####"
        );
        Move move =  new Move();
        Piece thePiece = Piece.getPiece(Piece.RIGHT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.LEFT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.LEFT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.RIGHT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.STICK, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 0);
        assertEquals(move.y(), 1);
    }
    /**
     * This test method contains the suggested tests for board 3
     */
    public void testBoard3() {
        start1 = new Board(10, 12,

            "          ",
            "###   ####"
        );
        Move move =  new Move();
        Piece thePiece = Piece.getPiece(Piece.LEFT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.RIGHT_L, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.T, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.RIGHT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 4);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.LEFT_DOG, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 2);
        assertEquals(move.y(), 0);
        thePiece = Piece.getPiece(Piece.STICK, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 6);
        assertEquals(move.y(), 1);
    }
    /**
     * This test method contains the suggested test for board 4
     */
    public void testBoard4() {
        start1 = new Board(10, 12,

            "          ",
            "###    ###"
        );
        Move move =  new Move();
        Piece thePiece = Piece.getPiece(Piece.STICK, 0);
        cleverBrain.bestMove(start1, thePiece, 12, move);
        assertEquals(move.x(), 3);
        assertEquals(move.y(), 0);
    }
}
