package main.java.group37.bejeweled.model;

import main.java.group37.bejeweled.view.Panel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Level extends Observable {
  
  public int level;
  private ArrayList<Observer> obs = new ArrayList<Observer>();
  
  /**
   * Represent the level object for a game.
   */
  public Level() {
    super();
    this.level = 1;
  }
  
  public void registerObserver(Panel panel) {
    obs.add(panel);    
  }

  public void removeObserver(Observer observer) {
    obs.remove(observer);    
  }
  
  /**
   * Increase the levelnumber when a certain score is reached.
   */
  public void updateLevel(int score) {
    
    int oldlevel = level;
    if (score >= 1000) {
      level = (int) (score / 1000) + 1;
    }

    
    if (!(level == oldlevel)) {
      Logger.log("level changed!");
      notifyObservers(this,level);
    }
  }
  
  /**
   * Notify the observers when the score has changed.
   * @param observable the observable
   * @param level the level to be set
   */
  public void notifyObservers(Observable observable, int level) {
    for (Observer ob : obs) {
      ob.update(observable, this);
    }
  }

  public int getLevel() {
    return this.level;
  }

  public void setLevel(Integer level) {
    this.level = level;
    notifyObservers(this,level);
  }
  
}
