package picross;

import java.util.ArrayList;
import java.util.Random;

import static picross.Game.dimension;

/**
 * @author Neil Kingdom
 * @version 1.1
 * @since 2021-09-15
 *
 * GameModel is responsible for the center matrix of tiles in
 * the main game. It has methods for initializing and returning
 * random layouts.
 */
public class GameModel {

    /* ---------------------- Constants -------------------- */
    private static final String INITIAL_CONFIG = "0010000100111110111001010";

    /* ---------------------- Member Variables -------------------- */

    private String bitStream;
    private boolean[][] board;
    private static ArrayList<ArrayList<Integer>> leftHintCol;
    private static ArrayList<ArrayList<Integer>> topHintRow;

    /**
     * @since 2021-10-09
     *
     * Unparameterized constructor which initializes the
     * random layout of the board, as well as the top row
     * and left column of hints.
     */
    public GameModel() {
        bitStream = INITIAL_CONFIG;
        board = tokenizeBitStream(bitStream);
        leftHintCol = genLeftHints(board);
        topHintRow = genTopHints(board);
    }

    /**
     * @since 2021-10-09
     *
     * Generates a sequence of bits which are stored in a string.
     */
    public void genRandBitStream() {

        bitStream = "";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(bitStream);

        for (int i = 0; i < dimension * dimension; i++)
            sb.append((rand.nextBoolean()) ? "1" : "0");

        bitStream = sb.toString();
        board = tokenizeBitStream(bitStream);
        leftHintCol = genLeftHints(board);
        topHintRow = genTopHints(board);
    }

    /**
     * @param bitStream The 1D sequence of random bits to be tokenized
     * @return Returns a 2D array of random booleans
     * @since 2021-10-09
     *
     * Splits the random bit stream into chunks. 1s are treated as
     * true, and 0s as false.
     */
    public boolean[][] tokenizeBitStream(String bitStream) {

        String subst[];
        boolean board[][] = new boolean[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            subst = bitStream.substring(i * dimension, i * dimension + dimension).split("");
            for (int j = 0; j < dimension; j++)
                board[i][j] = (Integer.valueOf(subst[j]) == 1) ? true : false;
        }

        return board;
    }

    /**
     * @param board The 2D array of random boolean values
     * @return Returns a 2D array list of hints for the left column
     * @since 2021-10-09
     *
     * This method scans the 2D array of random boolean values and
     * generates hints for the left column of the game.
     */
    public static ArrayList genLeftHints(boolean[][] board) {

        int count = 0;
        ArrayList<ArrayList<Integer>> leftHintCol = new ArrayList<>(dimension);

        for (int y = 0; y < dimension; y++) {
            ArrayList<Integer> hintCell = new ArrayList<>();
            for (int x = 0; x < dimension; x++) {
                if (board[y][x]) {
                    count++;
                } else if (count > 0) {
                    hintCell.add(count);
                    count = 0;
                } else ;
            }
            //Need one last check if count > 0 since for loop exits too early
            if (count > 0)
                hintCell.add(count);

            leftHintCol.add(hintCell);
            count = 0;
        }

        return leftHintCol;
    }


    /**
     * @param board The 2D array of random boolean values
     * @return Returns a 2D array list of hints for the top row
     * @since 2021-10-09
     *
     * This method scans the 2D array of random boolean values and
     * generates hints for the top row of the game.
     */
    public static ArrayList genTopHints(boolean[][] board) {

        int count = 0;
        ArrayList<ArrayList<Integer>> topHintRow = new ArrayList<>(dimension);

        for (int x = 0; x < dimension; x++) {
            ArrayList<Integer> hintCell = new ArrayList<>();
            for (int y = 0; y < dimension; y++) {
                if (board[y][x]) {
                    count++;
                } else if (count > 0) {
                    hintCell.add(count);
                    count = 0;
                } else ;
            }
            //Need one last check if count > 0 since for loop exits too early
            if (count > 0)
                hintCell.add(count);

            topHintRow.add(hintCell);
            count = 0;
        }

        return topHintRow;
    }

    /* ---------------------- Getters -------------------- */

    public boolean[][] getBoard() {
        return board;
    }

    public static ArrayList getLeftHintCol() {
        return leftHintCol;
    }

    public static ArrayList getTopHintRow() {
        return topHintRow;
    }

    @Override
    public String toString() {
        return bitStream;
    }

    /* ---------------------- Setters -------------------- */

    public void setBoard(boolean[][] board) { this.board = board; }

    public void setLeftHintCol(ArrayList<ArrayList<Integer>> leftHintCol) { this.leftHintCol = leftHintCol; }

    public void setTopHintRow(ArrayList<ArrayList<Integer>> topHintRow) { this.topHintRow = topHintRow; }
}
