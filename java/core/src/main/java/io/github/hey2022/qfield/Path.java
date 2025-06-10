package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayDeque;
import java.util.Deque;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Path {
  private final Deque<Vector2> path = new ArrayDeque<Vector2>();
  private final int maxSize;

  public Path(int maxSize) {
    this.maxSize = maxSize;
  }

  public void add(Vector2 point) {
    path.addLast(point);
    if (path.size() > maxSize) {
      path.removeFirst();
    }
  }

  public void drawPath(ShapeDrawer drawer) {
    drawer.setColor(Color.BLACK);
    Array<Vector2> array = new Array<>(path.size());
    for (Vector2 point : path) {
      array.add(point);
    }
    drawer.path(array, 1, true);
  }
}
