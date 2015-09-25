package main.java.group37.bejeweled.board;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * board object for the tiles.
 * 
 * @author Group 37
 *
 */
public class Board {
  
  /**
   * The grid of squares with board[x][y] being the square at column x, row y.
   */
  public Tile[][] board;
  
  /**
   * Creates a new board.
   * @param bi matrix with tiles
   */
  public Board(Tile[][] bi) {
    assert bi != null;
    this.board = bi;
  }
  
  /**
   * gets the amount of columns.
   * @return The width of this board
   */
  public int getWidth() {
    return board.length;
  }

  /**
   * gets the amount of rows.
   * @return The height of this board
   */
  public int getHeight() {
    return board[0].length;
  }
  
  /**
   * get the tile at coordinates x and y.
   * @param xi column position of the tile
   * @param yi row position of the tile
   * @return the tile at (x,y)
   */
  public Tile getTileAt(int xi, int yi) {
    Tile result = null;
    if  (validBorders(xi, yi)) {
      result = board[xi][yi];
    }
    return result;
  }
  
  /**
   * methos to set a tile in a specific place.
   * @param tile the tile to be placed at position (x,y)
   * @param xi column position of the tile
   * @param yi row position of the tile
   */
  public void setTileAt(Tile tile, int xi, int yi) {
    if  (validBorders(xi, yi)) {
      board[xi][yi] = tile;
    }
  }
  
  /**
   * checks if the given coordinates are on the board.
   * @param xi integer position column
   * @param yi integer position row
   * @return true iff the coordinates exist on the board
   */
  public boolean validBorders(int xi, int yi) {
    return (xi >= 0 && xi < getWidth() && yi >= 0 && yi < getHeight());
  }
  
  /**
   * checks if a square on the board is empty.
   * @param xi x coordinate to be checked
   * @param yi y coordinate to be checked
   * @return true iff there is no tile on coordinates (x,y)
   */
  public boolean isEmpty(int xi, int yi) {
    if (validBorders(xi, yi)) {
      if (getTileAt(xi, yi) == null) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * removes a tile from the board.
   * @param xi x coordinate of the tile
   * @param yi y coordinate of the tile
   */
  public void clear(int xi, int yi) {
    board[xi][yi] = null;
  }
  
  /**
   * Gets the tiles that need to be deleted due to the detonating of the flame gem.
   * @param tile the flame gem
   * @return tiles, the list of tiles to be deleted.
   */
  public List<Tile> getTilesToDeleteFlame(Tile tile) {
    List<Tile> tiles = new ArrayList<Tile>();
    final Point[] translations = {new Point(1,0), new Point(1,1), new Point(0,1),
                                  new Point(-1,1), new Point(-1,0), new Point(-1,-1),
                                  new Point(0,-1), new Point(1,-1)};
    int tx = tile.getX();
    int ty = tile.getY();
    tiles.add(tile);
    for (Point translation: translations) {
      if (validBorders(tx + translation.x, ty + translation.y)) {
        tiles.add(getTileAt(tx + translation.x, ty + translation.y));
      }
    }
    return tiles;
  }
  
  /**
   * Gets the tiles that need to be deleted due to the detonating of the hypercube gem.
   * @param t1 the hypercube gem
   * @return tiles, the list of tiles to be deleted.
   */
  public List<Tile> getTilesToDeleteHypercube(Tile t1, Tile hyper) {
    List<Tile> tiles = new ArrayList<Tile>();
    tiles.add(hyper);
    int index = t1.getIndex();
    
    for (int row = 0; row < board.length; row++) {        //loop through board
      for (int col = 0; col < board[0].length; col++) {
        if (index == getTileAt(row, col).getIndex()) {    //add tile tile if colors are the same
          tiles.add(getTileAt(row, col));
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
    tiles.add(tile);
    
    int tx = tile.getX();
    int ty = tile.getY();
    for (int col = 0; col < getHeight(); col++) {
      if (col != tx) {
        tiles.add(getTileAt(col,ty));
      }
    }
    for (int row = 0; row < getWidth(); row++) {
      if (row != ty) {
        tiles.add(getTileAt(tx,row));
      }
    }
    return tiles;
  }
    
   /** method to determine if to boards are equal.
   * @param obj object to be compared
   * @return true iff this board is the same as object obj
   */
  public boolean equals(Object obj) {
    if (obj instanceof Board) {
      Board bo = (Board)obj;
      if (this.getHeight() == bo.getHeight() && this.getWidth() == bo.getWidth()) {
        
        for (int row = 0; row < board.length; row++) {        //loop through board
          for (int col = 0; col < board[0].length; col++) {
            if (bo.getTileAt(row, col) == null && !(this.getTileAt(row, col) == null)) {
              return false;
            }
            if (this.getTileAt(row, col) == null && !(bo.getTileAt(row, col) == null)) {
              return false;
            }
            if (!(this.getTileAt(row, col) == null && bo.getTileAt(row, col) == null)) {
              if (!(bo.getTileAt(row, col).equals(this.getTileAt(row, col)))) {
                return false;
              }    
            }
          }    
        }
        return true;
      }

    }
    return false; 
  }
  
  /**
   * Override the hashcode, because we also override the equals method.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(board);
    return result;
  }
}