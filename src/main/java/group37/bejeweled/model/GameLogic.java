package main.java.group37.bejeweled.model;

import java.util.ArrayList;
import java.util.List;

import main.java.group37.bejeweled.Board.Board;
import main.java.group37.bejeweled.Board.FlameTile;
import main.java.group37.bejeweled.Board.HypercubeTile;
import main.java.group37.bejeweled.Board.StarTile;
import main.java.group37.bejeweled.Board.Tile;
import main.java.group37.bejeweled.model.Combination.Type;
import main.java.group37.bejeweled.view.Animation;
import main.java.group37.bejeweled.view.Main;

public class GameLogic {
  private static final int SIZE = Game.SIZE;
  private Board board;
  private Main boardPanel;
  private CombinationFinder finder;
  private Game game;

  public GameLogic(Game game) {
    this.game = game;
  }
  
  public void setFinder(CombinationFinder finder) {
    this.finder = finder;
  }

  public void setBoard(Board board) {
    this.board = board;
  }
  
  public void setBoardPanel(Main boardPanel) {
    this.boardPanel = boardPanel;
  }
  /**
   * Delete all combinations found on the board.
   */
  public void deleteChains() {
    List<Combination> chains = finder.getAllCombinationsOnBoard();
    List<Tile> tiles = new ArrayList<Tile>();
    
    for (Combination comb: chains) {
      game.updateScore(comb.getType());
      tiles.addAll(comb.getTiles());
      
      if (comb.containsSpecialGem() != null) {
        List<Tile> gemtiles = getTilesToDeleteSpecialGem(comb);
        for (Tile t1 : gemtiles) {
          if (!tiles.contains(t1)) {
            tiles.add(t1);
          }
        }
      }
      
      if (comb.isSpecialCombination()) {          //als er speciale combi is
        generateSpecialGem(comb);                 //maak dan een special gem
        //tiles.remove(comb.getTiles().get(2));
      }
    }
    
    deleteTiles(tiles);
  }
  
  /**
   * Delete all the tiles in 'tiles' from the board.
   * @param tiles list of tiles to delete.
   */
  public void deleteTiles(List<Tile> tiles) {
    for (Tile tile: tiles) {
      board.getTileAt(tile.getX(), tile.getY()).delete = true;
      Logger.log("Delete Tile: " + tile);
      for (int i = tile.getY() - 1; i >= 0; i--) {
        board.getTileAt(tile.getX(), i).increaseLevel();
      }
    }

    boardPanel.animations.setType(Animation.Type.REMOVE);
    boardPanel.animations.startRemove(tiles);
  }

  /**
   * If there are empty spaces, this method 'drops' the tile above this space into this space.
   */
  public void dropTiles() {    
    int level = 0;
    for (int row = SIZE - 1; row >= 0; row--) {
      for (int col = 0; col < SIZE; col++) {
        level = board.getTileAt(col, row).getLevel();
     
        if (level > 0) {
          board.setTileAt(board.getTileAt(col, row).clone(col, row + level), col, row + level);
          board.getTileAt(col, row + level).setLevel(0);
          board.getTileAt(col, row).delete = true;
          board.getTileAt(col, row + level).delete = false;
          board.getTileAt(col, row).setLevel(0);
        }
      }
    }
    for (int row = SIZE - 1; row >= 0; row--) {
      for (int col = 0; col < SIZE; col++) {
        if (board.getTileAt(col, row).delete) {
          board.setTileAt(game.setRandomTile(col,row), col, row);
          board.getTileAt(col, row).delete = false;
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
    if (combi.getType() == Type.FLAME) {
      generateSpecialGemFlame(combi);
    }
    if (combi.getType() == Type.STAR) {
      generateSpecialGemStar(combi);
    }
    if (combi.getType() == Type.HYPERCUBE) {
      generateSpecialGemFlameHypbercube(combi);
    }
  }
  
  private void generateSpecialGemFlameHypbercube(Combination combi) {
    // TODO Auto-generated method stub
  }

  private void generateSpecialGemStar(Combination combi) {
    // TODO Auto-generated method stub
  }

  public void generateSpecialGemFlame(Combination combi) {
    // TODO Auto-generated method stub
  }
  
  public List<Tile> getTilesToDeleteSpecialGem(Combination combi) {
    List<Tile> tiles = new ArrayList<Tile>();
    
    if (combi.containsSpecialGem() instanceof FlameTile) {
      tiles = board.getTilesToDeleteFlame(combi.containsSpecialGem());
    }
    if (combi.containsSpecialGem() instanceof StarTile) {
      tiles = board.getTilesToDeleteStar(combi.containsSpecialGem());
    }
    if (combi.containsSpecialGem() instanceof HypercubeTile) {
      tiles = board.getTilesToDeleteHypercube(combi.containsSpecialGem());
    }
    
    return tiles;
  }
  

}