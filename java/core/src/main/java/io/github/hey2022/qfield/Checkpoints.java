package io.github.hey2022.qfield;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Checkpoints {
  private Array<Checkpoint> activeCheckpoints = new Array<Checkpoint>();
  private Array<Checkpoint> checkpoints = new Array<Checkpoint>();
  private Checkpoint selectedCheckpoint;

  public int completedCheckpoints() {
    return checkpoints.size - activeCheckpoints.size;
  }

  public int checkpoints() {
    return checkpoints.size;
  }

  public int activeCheckpoints() {
    return activeCheckpoints.size;
  }

  public void select(Vector2 cursorPos) {
    boolean foundSelection = false;
    for (int i = checkpoints.size - 1; i >= 0; i--) {
      if (!foundSelection && checkpoints.get(i).circle.contains(cursorPos)) {
        selectedCheckpoint = checkpoints.get(i);
        selectedCheckpoint.select();
        foundSelection = true;
      } else {
        checkpoints.get(i).unselect();
      }
    }
  }

  public boolean empty() {
    return checkpoints.size == 0;
  }

  public Checkpoint peek() {
    return checkpoints.peek();
  }

  public void unselect() {
    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.unselect();
    }
  }

  public void reset() {
    for (Checkpoint point : checkpoints) {
      point.setReached(false);
    }
    activeCheckpoints = new Array<Checkpoint>(checkpoints);
  }

  public void add(Vector2 position, float radius) {
    Checkpoint newCheckpoint = new Checkpoint(position, radius);
    activeCheckpoints.add(newCheckpoint);
    checkpoints.add(newCheckpoint);
  }

  public void delete() {
    if (selectedCheckpoint != null) {
      checkpoints.removeValue(selectedCheckpoint, true);
      activeCheckpoints.removeValue(selectedCheckpoint, true);
      selectedCheckpoint = null;
    }
  }

  public void draw(ShapeDrawer drawer) {
    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.draw(drawer);
    }
  }

  public void check(Charge charge) {
    for (Checkpoint checkpoint : activeCheckpoints) {
      if (checkpoint.check(charge)) {
        activeCheckpoints.removeValue(checkpoint, true);
      }
    }
  }
}
