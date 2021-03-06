package main.java.group37.bejeweled.model;

import main.java.group37.bejeweled.board.Board;
import main.java.group37.bejeweled.board.FlameTile;
import main.java.group37.bejeweled.board.StarTile;
import main.java.group37.bejeweled.board.Tile;
import main.java.group37.bejeweled.combination.Combination;
import main.java.group37.bejeweled.combination.Combination.Type;
import main.java.group37.bejeweled.view.Animation;
import main.java.group37.bejeweled.view.Main;
import main.java.group37.bejeweled.view.Panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class GameLogic {
  public Score score;
  public Level level;
  private Board board;
  private Main boardPanel;
  private PatternFinder finder;
  private Game game;
   
  private static GameLogic instance = new GameLogic();
  
  private GameLogic() {}
  
  public static GameLogic get() {
    return instance;
  }

  /**
   * Create a gameLogic object.
   * @param game the game
   * @param board the board
   * @param main the main
   */
  public void setReferences(Game game, Board board, Main main) {
    this.game = game;
    this.board = board;
    this.boardPanel = main;
    this.finder = new PatternFinder(board);
  }

  /**
   * Delete all combinations found on the board.
   */
  public void deleteChains() {
    List<Combination> chains = finder.getAllCombinationsOnBoard();
    List<Tile> tiles = new ArrayList<Tile>();

    for (Combination comb: chains) {
      addTiles(comb.getTiles(), tiles);
      if (!comb.containsSpecialGem()) {
        score.updateScore(comb);         //update normal score
      }
      Logger.log("Comb type: " + comb.getType());
      Logger.log("containsSpecialGem: " + comb.containsSpecialGem());
      
      if (comb.containsSpecialGem()) {
        Logger.log("Special gem in combination: " + comb.getType());
        List<Tile> gemTiles = getTilesToDeleteSpecialGem(comb);
        score.updateScoreSpecialGem(comb, gemTiles); //update score for detonating special gem
        addTiles(gemTiles,tiles);
        Logger.log("Delete " + gemTiles.size() + " additional tiles");
      }

      if (comb.isSpecialCombination()) {          //als er speciale combi is
        generateSpecialGem(comb);                 //maak dan een special gem
      }
    }
    level.updateLevel(score.getScore());
    
//    boardPanel.animations.resetLevelDropTiles();
    Arrays.stream(board.board)
        .forEach(row -> Arrays.stream(row).forEach(tile -> tile.setLevel(0)));
    deleteTiles(tiles);
  }
  
  /**
   * Merge two tiles list together without any duplicates.
   * @param tilesToAdd list to add to another list.
   * @param list list where the tiles will be added.
   */
  public void addTiles(List<Tile> tilesToAdd, List<Tile> list) {
    for (Tile tile : tilesToAdd) {
      if (!list.contains(tile)) {
        list.add(tile);
      }
    }
  }

  /**
   * Delete all the tiles in 'tiles' from the board.
   * @param tiles list of tiles to delete.
   */
  public void deleteTiles(List<Tile> tiles) {
    List<Tile> tilesToDrop = new ArrayList<Tile>();
    for (Tile tile: tiles) {
      Logger.log("Delete Tile: " + tile);
      board.getTileAt(tile.getX(), tile.getY()).delete = true;
      if (tile.getNextType() == Type.NORMAL) {
        for (int i = tile.getY() - 1; i >= 0; i--) {
          board.getTileAt(tile.getX(), i).increaseLevel();
          tilesToDrop.add(board.getTileAt(tile.getX(), i));
        }
      }
    }
    boardPanel.animations.setDropTiles(tilesToDrop);
    boardPanel.animations.setType(Animation.Type.REMOVE);
    boardPanel.animations.setRemoveTiles(tiles);
    boardPanel.animations.start();
  }

  /**
   * If there are empty spaces, this method 'drops' the tile above this space into this space.
   */
  public void dropTiles() {    
    int level = 0;
    for (int row = board.getWidth() - 1; row >= 0; row--) {
      for (int col = 0; col < board.getWidth(); col++) {
        level = board.getTileAt(col, row).getLevel();

        if (level > 0) {
          board.setTileAt(board.getTileAt(col, row).clone(col, row + level), col, row + level);
          board.getTileAt(col, row + level).setLevel(0);
          
          board.getTileAt(col, row).delete = true;
          board.getTileAt(col, row).setNextType(Type.NORMAL);

          board.getTileAt(col, row + level).delete = false;
          
          board.getTileAt(col, row).setLevel(0);
        }
      }
    }

    deleteTilesFromBoard();
  }
  
  private void deleteTilesFromBoard() {
    Tile tile = null;
    for (int row = board.getWidth() - 1; row >= 0; row--) {
      for (int col = 0; col < board.getWidth(); col++) {
        tile = board.getTileAt(col, row);
        if (tile.delete || tile.getNextType() != Type.NORMAL) {
          if (tile.getNextType() == Type.NORMAL) {
            board.setTileAt(game.setRandomTile(col,row), col, row);
          } else if (!(tile.getNextType() == Type.NORMAL) && !(tile.getNextType() == null)) {
            board.setTileAt(game.setSpecialTile(col,row,tile.getNextType()), col, row);
          } 
          tile = board.getTileAt(col,row);
          tile.setNextType(Type.NORMAL);
          tile.delete = false;
        }
        if (board.getTileAt(col, row).remove) {
          board.getTileAt(col, row).remove = false;
        }
      }
    }
    boardPanel.repaint();

    List<Combination> chains = finder.getAllCombinationsOnBoard();
    if (chains.size() != 0) {
      deleteChains();
    }
  }

  /**
   * Finds the type of the special combination and calls the method to generate this special gem.
   * @param combi the combination to find the type of.
   */
  public void generateSpecialGem(Combination combi) {
    Logger.log("Generate special gem");
    combi.setNextType();
  }

  /**
   * Get list of tiles to delete in case of a special gem.
   * @param combi original tiles from the combination.
   * @return list of all tiles.
   */
  public List<Tile> getTilesToDeleteSpecialGem(Combination combi) {
    List<Tile> tiles = new ArrayList<Tile>();
    List<Tile> tempTiles = null;
    for (Tile tile: combi.getSpecialTiles()) {
      tempTiles = null;
      if (tile instanceof FlameTile) {
        tempTiles = SwapHandler.get().getTilesToDeleteFlame(tile);
      } else if (tile instanceof StarTile) {
        tempTiles = SwapHandler.get().getTilesToDeleteStar(tile);
      }
      
      if (tempTiles != null) {
        addTiles(tempTiles,tiles);
      }
    }
    return tiles;
  }
  
  public Score getScore() {
    return score;
  }

  public Level getLevel() {
    return level;
  }
  
  /**
   * Initialize the score and level and set the observer.
   * @param panel the observer to be set.
   */
  public void init(Panel panel) {
    score = new Score();
    score.registerObserver(panel);
    level = new Level();
    level.registerObserver(panel);
  }
  
  /**
   * Method for getting an arrayList with two Tiles, which can be switched to form a combination.
   * @return the arraylist with the tiles.
   */
  public ArrayList<Tile> getHint() {
    ArrayList<ArrayList<Tile>> res = new ArrayList<ArrayList<Tile>>();
    ArrayList<Tile> combi;
    Tile t0 = null;
    Tile t1 = null;
    
  //check combinations in x direction
    for (int i = 0; i < board.getHeight(); i++) {
      for (int j = 0; j < 7; j++) {
        t0 = board.getTileAt(j, i);
        t1 = board.getTileAt(j + 1, i);
        if (SwapHandler.get().createsCombination(t0,t1)) {
          combi = new ArrayList<Tile>();
          combi.add(t0);
          combi.add(t1);
          res.add(combi);
        }
      }
    }
    
  //check combinations in y direction
    for (int i = 0; i < board.getHeight(); i++) {
      for (int j = 0; j < 7; j++) {
        t0 = board.getTileAt(i, j);
        t1 = board.getTileAt(i, j + 1);
        if (SwapHandler.get().createsCombination(t0,t1)) {
          combi = new ArrayList<Tile>();
          combi.add(t0);
          combi.add(t1);
          res.add(combi);
        }
      }
    }
    Random rd = new Random();
    double rand = rd.nextDouble();
    if (!res.isEmpty()) {
      return res.get((int) rand * (res.size() - 1));
    } 
    return null;
  }
}