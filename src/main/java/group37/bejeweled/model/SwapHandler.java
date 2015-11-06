package main.java.group37.bejeweled.model;

import main.java.group37.bejeweled.board.Board;
import main.java.group37.bejeweled.board.FlameTile;
import main.java.group37.bejeweled.board.HypercubeTile;
import main.java.group37.bejeweled.board.StarTile;
import main.java.group37.bejeweled.board.Tile;
import main.java.group37.bejeweled.combination.Combination;
import main.java.group37.bejeweled.view.Animation;
import main.java.group37.bejeweled.view.Main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Contains methods related to swappig tiles.
 * @author Group 37
 *
 */
public class SwapHandler {

  private Board board;
  public List<Tile> swapTiles;
  private Tile[] swappedTiles;
  private PatternFinder finder;
  private Main main;
  
  private static SwapHandler instance = new SwapHandler();

  private SwapHandler() {}
  
  public static SwapHandler get() {
    return instance;
  }

  /**
   * .
   * @param board a board object.
   */
  public void setRefrences(Board board, Main main) {
    this.board = board;
    this.main = main;
    
    swapTiles = new ArrayList<Tile>();
    swappedTiles = new Tile[2];
    
    finder = new PatternFinder(board);
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
      main.setFocus(loc);
      if (swapTiles.size() == 2 && canSwap()) {              
        swapTiles(swapTiles);
        swapTiles.clear();
      }
    }
  }
  
  /**
   * Switch tile t0 and t1 on the board.
   * @param t0 first tile to swap
   * @param t1 second tile to swap
   */
  public void swappedTiles(Tile t0, Tile t1) {
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

    if (t0 instanceof HypercubeTile || t1 instanceof HypercubeTile) {
      return true;
    }

    if (!createsCombination(t0,t1)) {
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
   * Gets the tiles that need to be deleted due to the detonating of the flame gem.
   * @param tile the flame gem
   * @return tiles, the list of tiles to be deleted.
   */
  public List<Tile> getTilesToDeleteFlame(Tile tile) {
    final Point[] translations = {new Point(-1,0), new Point(1,1), new Point(0,1),
        new Point(-1,1), new Point(1,0), new Point(-1,-1),
        new Point(0,-1), new Point(1,-1)};
    tile.detonate = true;
    List<Tile> tiles = Arrays.stream(translations)
        .map(p -> new Point(tile.getX() + p.x, tile.getY() + p.y))
        .filter(p -> board.validBorders(p.x, p.y))
        .map(p -> board.getTileAt(p.x,p.y))
        .filter(t-> !t.detonate)
        .collect(Collectors.toList());
    checkForSpecialTile(tiles);
    if (!tiles.contains(tile)) {
      tiles.add(tile);
    }
    return tiles;
  }
  
  private void checkForSpecialTile(List<Tile> list) {
    List<Tile> res = new ArrayList<Tile>();
    
    List<Tile> tempTiles = null;
    for (Tile t : list) {
      tempTiles = null;
      if (t instanceof FlameTile && !t.detonate) {
        tempTiles = getTilesToDeleteFlame(t);
      } else if (t instanceof StarTile && !t.detonate) {
        tempTiles = getTilesToDeleteStar(t);
      }      

      if (tempTiles != null) {
        for (Tile tt: tempTiles) {
          if (!list.contains(tt)) {
            res.add(tt);
          }
        }
      }
    }
    
    GameLogic.get().addTiles(res,list);
  }
  
  
  /**
   * Gets the tiles that need to be deleted due to the detonating of the hypercube gem.
   * @param t1 the hypercube gem
   * @return tiles, the list of tiles to be deleted.
   */
  public List<Tile> getTilesToDeleteHypercube(Tile t1, Tile hyper) {
    List<Tile> tiles = new ArrayList<Tile>();
    tiles.add(hyper);
    hyper.detonate = true;
    int index = t1.getIndex();
    
    Tile tempTile = null;
    for (int row = 0; row < board.getWidth(); row++) {        //loop through board
      for (int col = 0; col < board.getHeight(); col++) {
        tempTile = board.getTileAt(row, col);
        if (index == tempTile.getIndex() && !tempTile.detonate) {  
          //add tile tile if colors are the same
          tiles.add(tempTile);
        }
      }    
    }
    return tiles;
  }
  
  /**
   * Gets the tiles that need to be deleted due to the detonating of the hypercube gem.
   * @param tile the hypercube gem
   * @return tiles, the list of tiles to be deleted.
   */
  public List<Tile> getTilesToDeleteStar(Tile tile) {
    List<Tile> tiles = new ArrayList<Tile>();
    tile.detonate = true;
    tiles.add(tile);
    
    int tx = tile.getX();
    int ty = tile.getY();
    for (int col = 0; col < board.getHeight(); col++) {
      if (col != tx && !board.getTileAt(col,ty).detonate) {
        tiles.add(board.getTileAt(col,ty));
      }
    }
    for (int row = 0; row < board.getWidth(); row++) {
      if (row != ty && !board.getTileAt(tx,row).detonate) {
        tiles.add(board.getTileAt(tx,row));
      }
    }
    checkForSpecialTile(tiles);
    return tiles;
  }
  
  public List<Tile> getSwapTiles() {
    return swapTiles;
  }
  

  /**
   * Check if tile t0 and t1 can create a valid combination.
   * @param t0 tile 1
   * @param t1 tile 2
   * @return true iff a valid combination was made after the tiles where swapped.
   */
  public boolean createsCombination(Tile t0, Tile t1) {
    swappedTiles(t0,t1);
    Combination comb1 = finder.getSingleCombination(t0);
    Combination comb2 = finder.getSingleCombination(t1);
    swappedTiles(t0,t1);
    return comb1 != null || comb2 != null;
  }

  /**
   * swap the tiles in the list.
   * @param swapTiles the list of (two) tiles that should be swapped.
   */
  public void swapTiles(List<Tile> swapTiles) {
    main.animations.setType(Animation.Type.SWAP);
    main.animations.setSwapTiles(swapTiles.get(0),swapTiles.get(1));
    main.animations.start();
    Logger.log("Swap tiles: " + swapTiles.get(0).getLoc() + ", " + swapTiles.get(1).getLoc());
  }
}