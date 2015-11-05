package main.java.group37.bejeweled.model;

import main.java.group37.bejeweled.board.Board;
import main.java.group37.bejeweled.board.Tile;
import main.java.group37.bejeweled.combination.Combination;
import main.java.group37.bejeweled.combination.CombinationFactory;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class PatternFinder {
  private Tile[][] board;
  private static final int SIZE = 8;
  private boolean[][] checked;
  private Map<Tile,Boolean> processed;
  private List<Point[]> patterns = new ArrayList<Point[]>();
  private Queue<Tile> queue;
  private final List<Point> translations =
        Arrays.asList(new Point(1,0), new Point(-1,0),
                      new Point(0,1), new Point(0,-1));

  /**
   * Create PatternFinder object with reference to board.
   * @param board to check for patterns.
   * @throws FileNotFoundException  throw if patterns.txt doesn't exist.
   */
  public PatternFinder(Board board) {
    this.board = board.board;
    this.processed = new HashMap<Tile,Boolean>();
    resetChecked();
    initializePatterns();
  }

  /**
   * Return all chains from the board.
   * @return list of combinations.
   */
  public List<Combination> getAllCombinationsOnBoard() {
    this.processed.clear();
    List<Combination> res = new ArrayList<Combination>();
    Arrays.stream(board).forEach(row -> {
        Arrays.stream(row)
            .filter(tile -> !processed.containsKey(tile))
            .forEach(tile -> {
                List<Tile> cluster = findCluster(tile);
                Combination comb = findPattern(cluster);
                if (comb != null) { 
                  res.add(comb); 
                }
              });
      });
    return res;
  }

  /**
   * Use flood-fill algorithm to find clusters of gems with the same colour as tile.
   * @param tile tile to find the cluster with
   * @return list of points
   */
  private List<Tile> findCluster(Tile tile) {
    resetChecked();
    queue = new LinkedList<Tile>();
    List<Tile> res = new ArrayList<Tile>();
    visitTile(tile,res);
    int index = tile.getIndex();
    while (!queue.isEmpty()) {
      Tile currentTile = queue.poll();
      translations.stream()
        .map(p -> new Point(p.x + currentTile.getX(), p.y + currentTile.getY()))
        .filter(p -> tileNotVisited(p))
        .map(p -> getTile(p))
        .filter(t -> t.getIndex() == index)
        .forEach(t -> visitTile(t,res));
    }
    return res;
  }

  private Combination findPattern(List<Tile> points) {
    if (points.size() <= 2) {
      return null;
    }
    List<Point> cluster = points.stream().map(tile -> tile.loc)
        .collect(Collectors.toList());
    for (int patternIndex = patterns.size() - 1; patternIndex >= 0; patternIndex--) {
      Point[] pattern = patterns.get(patternIndex);
      final int numberOfPoints = pattern.length;
      for (Point point : cluster) {
        List<Tile> matches = Arrays.stream(pattern)
            .map(p -> new Point(point.x + p.x, point.y + p.y))
            .filter(p -> cluster.contains(p))
            .map(p -> points.get(cluster.indexOf(p)))
            .collect(Collectors.toList());
        if (numberOfPoints == matches.size()) {
          return createCombination(patternIndex,matches);
        }
      }
    }
    return null;
  }

  private void resetChecked() {
    this.checked = new boolean[SIZE][SIZE];
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        checked[i][j] = false;
      }
    }
  }

  private void visitTile(Tile tile, List<Tile> res) {
    queue.add(tile);
    processed.put(tile, true);
    res.add(tile);
    checked[tile.getX()][tile.getY()] = true;
  }
  
  private Combination createCombination(int patternIndex, List<Tile> tiles) {
    Combination.Type type = getTileStateFromIndex(patternIndex);
    Logger.log(type + "");
    Combination combination = CombinationFactory.makeCombination(type);
    combination.setTiles(tiles);
    return combination;
  }
  
  private Combination.Type getTileStateFromIndex(int index) {
    Logger.log("INDEX: " + index);
    if (index == 0 || index == 1) {
      return Combination.Type.NORMAL;
    } else if (index == 2 || index == 3) {
      return Combination.Type.FLAME;
    } else if (index >= 4 && index <= 11) {
      return Combination.Type.STAR;
    } else if (index == 12 || index == 13) {
      return Combination.Type.HYPERCUBE;
    }
    return null;
  }

  private Tile getTile(Point point) {
    return board[point.x][point.y];
  }
  
  private boolean tileNotVisited(Point point) {
    return (withinBoundaries(point) && !checked[point.x][point.y]);
  }

  private static boolean withinBoundaries(Point point) {
    return (point.x >= 0 && point.x < SIZE && point.y >= 0 && point.y < SIZE);
  }

  /**
   * Initialize all patterns in specific order.
   */
  private void initializePatterns() {
    //Normal
    patterns.add(new Point[]{new Point(0,0), new Point(0,1), new Point(0,2)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0)});

    //Flame
    patterns.add(new Point[]{new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0)});

    //L shape
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(2,1), new Point(2,2)});
    patterns.add(new Point[]{new Point(0,0), new Point(0,1), new Point(0,2),
        new Point(1,2), new Point(2,2)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(0,1), new Point(0,2)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(2,-1), new Point(2,-2)});
    
    //T shape
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(2,1), new Point(2,-1)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(1,1), new Point(1,2)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(1,-1), new Point(1,-2)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(0,-1), new Point(0,1)});
    
    //Hypercube
    patterns.add(new Point[]{new Point(0,0), new Point(0,1), new Point(0,2),
        new Point(0,3), new Point(0,4)});
    patterns.add(new Point[]{new Point(0,0), new Point(1,0), new Point(2,0),
        new Point(3,0), new Point(4,0)});
  }
}