package main.java.group37.bejeweled.model;

import main.java.group37.bejeweled.board.Board;
import main.java.group37.bejeweled.board.BoardFactory;
import main.java.group37.bejeweled.board.FlameTile;
import main.java.group37.bejeweled.board.HypercubeTile;
import main.java.group37.bejeweled.board.NormalTile;
import main.java.group37.bejeweled.board.Tile;
import main.java.group37.bejeweled.board.TileFactory;
import main.java.group37.bejeweled.combination.Combination;
import main.java.group37.bejeweled.combination.Combination.Type;
import main.java.group37.bejeweled.combination.CombinationFinder;
import main.java.group37.bejeweled.view.Main;
import main.java.group37.bejeweled.view.StatusPanel;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;

//TODO tile do not drop down anymore, but are replaced by random tiles.
/**
 * Class that represents the current game.
 * @author group37
 */
public class Game {
  private Board board = null;
  public BoardFactory boardFactory;
  public List<Tile> swapTiles;
  private Tile[] swappedTiles; //Use this for special gems
  private Main boardPanel;
  private StatusPanel panel;
  private CombinationFinder finder;
  
  public GameLogic logic;
  public static final int SIZE = Main.SIZE;

  /**
   * Create game object.
   * @param boardPanel object for GUI.
   * @param panel object for updating the labels.
   */
  public Game(Main boardPanel,StatusPanel panel) {
    this.boardPanel = boardPanel;
    this.boardFactory = new BoardFactory(this);
    this.board = new Board(new Tile[Main.SIZE][Main.SIZE]); 
    this.panel = panel;
    this.finder = new CombinationFinder(board);

    this.logic = new GameLogic(this);
    this.logic.setFinder(finder);
    this.logic.setBoard(board);
    this.logic.setBoardPanel(boardPanel);
    
    swapTiles = new ArrayList<Tile>();
    swappedTiles = new Tile[2];
    generateRandomBoard();
  }

  /**
   * Add tile to swapTiles based on location from the mouseEvent.
   * @param loc location of tile
   */
  public void addTile(Point loc) {
    int col = loc.x;
    int row = loc.y;
    if (!swapTiles.contains(board.getTileAt(col, row))) {
      swapTiles.add(board.getTileAt(col, row));
      boardPanel.setFocus(loc);
      if (swapTiles.size() == 2 && canSwap()) {              
        boardPanel.swapTiles(swapTiles);
        swapTiles.clear();
      }
    }
  }

  /**
   * Prints the combinations obtained by getAllCombinationsOnBoard().
   */
  public void printCombinations() {
    List<Combination> res = finder.getAllCombinationsOnBoard();
    System.out.println("chains: " + res.size());
    for (Combination combi : res) {
      System.out.println("\tType: " + combi.getType());
      System.out.println("\t" + combi.getTiles());
    }
  }

  /**
   * Create a board of random jewels without a sequence of 3 or more tiles with the same color.
   */
  public void generateRandomBoard() {
    Logger.log("Create new board");
    for (int i = 0; i < Main.SIZE; i++) {
      for (int j = 0; j < Main.SIZE; j++) {
        board.setTileAt(setRandomTile(i,j), i , j);
      }

      //Redo column if a sequence has been detected
      if (hasSequence(i)) {
        i--;
      }
    }
    finder.setBoard(this.board);
  }
  
  /**
   * makes a random tile.
   * @param xi x coordinate of the new random tile
   * @param yi y coordinate of the new random tile
   * @return a random tile as a Tile object
   */
  public Tile setRandomTile(int xi, int yi) { 
    Tile tile = new NormalTile(xi, yi);
    Random random = new Random();
    tile.setIndex(random.nextInt(7));
    tile.setImage(new ImageIcon(tile.paths[tile.getIndex()]));
    return tile;
  }
  
  /**
   * Set tile to special tile.
   * @param xi x coordinate of the new random tile
   * @param yi y coordinate of the new random tile
   * @param type type of special tile.
   * @return tile object.
   */
  public Tile setSpecialTile(int xi, int yi, Type type) {
    Logger.log("Creating special tile " + type + " at " + xi + "," + yi);
    Tile tile = null;
    tile = TileFactory.generateTile(type, xi, yi);
    tile.setIndex(board.getTileAt(xi, yi).getIndex());
    tile.setImage(new ImageIcon(tile.paths[tile.getIndex()]));
    return tile;
  }

  /**
   * Checks if column i that just has been added doesn't create a sequence of 3 or more colours.
   * @param row column to check for sequences.
   * @return true iff there is found a sequence of three or more jewels.
   */
  private boolean hasSequence(int row) {
    int sum = 0;
    //Find sequence in row i
    for (int j = 1; j < Main.SIZE; j++) {
      if (board.getTileAt(row, j).equalsColor(board.getTileAt(row, j - 1))) {
        sum++;
      } else {
        sum = 0;
      }

      if (sum >= 2) {
        return true;
      }
    }

    //If there are only 1 or 2 rows created, don't check for horizontal sequences
    if (row <= 1) {
      return false; 
    }

    //Find horizonal sequences
    for (int j = 0; j < Main.SIZE; j++) {
      sum = 0;
      sum += (board.getTileAt(row - 1, j).equalsColor(board.getTileAt(row, j)) ? 1 : 0);
      sum += (board.getTileAt(row - 2, j).equalsColor(board.getTileAt(row, j)) ? 1 : 0);

      if (sum == 2) {
        Logger.log("i,j: " + row + "," + j);
        return true;
      }
    }
    return false;
  }

  /**
   * Check if two tiles can be swapped and
   * what kind of jewel should be created based on the size of the found sequence.
   * @param t0 first tile to swap
   * @param t1 second tile to swap
   * @return true iff swapping tiles t0 and t1 results in a valid combination.
   */
  public Tile checktype(Tile t0, Tile t1) {
    //Tile.State res = null;
    Tile res = null;
    String c1 = Tile.colors[board.getTileAt(t0.getX(), t0.getY()).getIndex()];
    String c2 = Tile.colors[board.getTileAt(t1.getX(), t1.getY()).getIndex()];
    Tile tile = null;
    String color = null;
    //swap tiles to look in the rows where the tile will be in case it can be switched
    swapTiles(t0,t1);

    for (int i = 1; i < 3; i++) {

      if (i == 1) {
        tile = t0;
        color = c1;
      }
      if (i == 2) {
        tile = t1;
        color = c2;
      }

      //check x direction
      int sum = 1;
      for (int q = tile.getX() + 1; q < SIZE; q++) {
        if (Tile.colors[board.getTileAt(q, tile.getY()).getIndex()].equals(color)) {
          sum++;
        } else {
          break;
        }
      }
      for (int q = tile.getX() - 1; q >= 0; q--) {
        if (Tile.colors[board.getTileAt(q, tile.getY()).getIndex()].equals(color)) {
          sum++;
        } else {
          break;
        }
      }
      
      if (sum == 3) {
        res = new NormalTile(tile.getX(), tile.getY());
      }
      if (sum == 4) {
        res = new FlameTile(tile.getX(), tile.getY());
      }
      if (sum == 5) {
        res = new HypercubeTile(tile.getX(), tile.getY());
      }

      //check y direction
      sum = 1;
      for (int q = tile.getY() + 1; q < SIZE; q++) {
        if (Tile.colors[board.getTileAt(tile.getX(), q).getIndex()].equals(color)) {
          sum++;
        } else {
          break;
        }
      }
      for (int q = tile.getY() - 1; q >= 0; q--) {
        if (Tile.colors[board.getTileAt(tile.getX(), q).getIndex()].equals(color)) {
          sum++;
        } else {
          break;
        }
      }

      if (sum == 3) {
        res = new NormalTile(tile.getX(), tile.getY());
      }
      if (sum == 4) {
        res = new FlameTile(tile.getX(), tile.getY());
      }
      if (sum == 5) {
        res = new HypercubeTile(tile.getX(), tile.getY());
      }
    }
    //swap the tiles back to original position
    swapTiles(t0,t1);
    return res;
  }

  /**
   * Method to check whether there are possible moves left in the game
   * @return true if there are possible moves, false if there are none.
   */
  public boolean possibleMove() {
    boolean possiblemove = false;
    Tile t0 = null;
    Tile t1 = null;

    //check combinations in x direction
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < 7; j++) {
        t0 = board.getTileAt(j, i);
        t1 = board.getTileAt(j + 1, i);
        if (!(checktype(t0,t1) == null)) {
          possiblemove = true;
        }
      }
    }

    //check combinations in y direction
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < 7; j++) {
        t0 = board.getTileAt(i, j);
        t1 = board.getTileAt(i, j + 1);
        if (!(checktype(t0,t1) == null)) {
          possiblemove = true;
        }
      }
    }
    return possiblemove;
  }

  /**
   * Switch tile t0 and t1 on the board.
   * @param t0 first tile to swap
   * @param t1 second tile to swap
   */
  public void swapTiles(Tile t0, Tile t1) {
    Tile temp = board.getTileAt(t0.getX(), t0.getY());
    board.setTileAt(board.getTileAt(t1.getX(), t1.getY()), t0.getX(), t0.getY());
    board.setTileAt(temp, t1.getX(), t1.getY());

    int xc = t0.getX();
    int yc = t0.getY();
    t0.setLoc(t1.getX(),t1.getY());
    t1.setLoc(xc, yc);
    
    swappedTiles[0] = t0;
    swappedTiles[1] = t1;
  }

  /**
   * Swap two tiles if it result in a sequence of 3 of more tiles with the same color.
   */
  public boolean canSwap() {
    Tile t0 = board.getTileAt(swapTiles.get(0).getX(), swapTiles.get(0).getY());
    Tile t1 = board.getTileAt(swapTiles.get(1).getX(), swapTiles.get(1).getY());

    swapTiles(t0,t1);
    Combination combiX0 = finder.getSingleCombinationX(t0);
    Combination combiX1 = finder.getSingleCombinationX(t1);
    Combination combiY0 = finder.getSingleCombinationY(t0);
    Combination combiY1 = finder.getSingleCombinationY(t1);
    swapTiles(t0,t1);

    Type type = null;
    if (!(combiX0 == null)) {
      type = combiX0.getType();
    } else if (!(combiX1 == null)) {
      type = combiX1.getType();
    } else if (!(combiY0 == null)) {
      type = combiY0.getType();
    } else if (!(combiY1 == null)) {
      type = combiY1.getType();
    }

    if (t0 instanceof HypercubeTile || t1 instanceof HypercubeTile) {
      return true;
    }

    if (type == null) {
      return false;
    }

    if (!isNeighbour(t0,t1)) {
      Logger.error("t0 and t1 are not neighbours.");
      return false;
    }
    return true;
  }

  /**
   * Return true if t0 and t1 are neighbours.
   * @param t0 tile 1.
   * @param t1 tile 2.
   * @return true if t0 and t1 are next to each other.
   */
  public boolean isNeighbour(Tile t0, Tile t1) {
    if (Math.abs(t0.getX() - t1.getX()) == 1 && Math.abs(t0.getY() - t1.getY()) == 0) {
      return true;
    }
    if (Math.abs(t0.getX() - t1.getX()) == 0 && Math.abs(t0.getY() - t1.getY()) == 1) {
      return true;
    }
    return false;
  }
  
  /**
   * End game if there is no possible combination.
   
  public void endGame() {
    JLabel label1 = new JLabel("No possible combination",JLabel.CENTER);
    label1.setVerticalTextPosition(JLabel.TOP);
    label1.setHorizontalTextPosition(JLabel.CENTER);
    if (!(possibleMove())) {
      this.boardPanel.add(label1);
      return;
    }
  }
*/
  /**
   * Reset game.
   */
  public void reset() {
    logic.getScore().resetScore();
    swapTiles = new ArrayList<Tile>();
    generateRandomBoard();
    boardPanel.repaint();
  }

  /**
   * Get board object.
   * @return the board
   */
  public Board getBoard() {
    return this.board;
  }
  
  /**
   * set board object.
   * @return the board
   */
  public void setBoard(Board bo) {
    this.board = bo;
  }
  
//  /**
//   * Get the current level number.
//   * @return the level number.
//   */
//  public int getLevel() {
//    return this.newlevel;
//  }
  
  /**
   * Get the list with the selected tiles to swap.
   * @return the list swapTiles
   */
  public List<Tile> getSwaptiles() {
    return this.swapTiles;
  }
  
  /**
   * Get the CombinationFinder of the game.
   * @return CombinationFinder
   */
  public CombinationFinder getFinder() {
    return finder;
  }
  
  /**
   * sets the CombinationFinder.
   * @param cf CombinationFinder object
   * @return a CombinationFinder object
   */
  public void setFinder(CombinationFinder cf) {
    this.finder = cf;
  }

  /**
   * Set the current level number.
   * @param level1 the level number to be set.
   */
//  public void setLevel(Integer level1) {
//    this.newlevel = level1; 
//  }
}