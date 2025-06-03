package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** Checkpoint */
public class Checkpoint {
  private static int pointsCount = 0;

  public int index;
  private Circle circle;
  private boolean reached;
  public boolean enabled;

  public boolean isReached() {
    return reached;
  }

  public void setReached(boolean checked) {
    this.reached = checked;
    if (checked == false) {
      color = Color.GREEN;
    } else {
      color = Color.LIGHT_GRAY;
    }
  }

  private Color color;

  public Checkpoint(Vector2 pos, float radius) {
    this(pos.x, pos.y, radius);
  }

  public Checkpoint(float x, float y, float radius) {
    circle = new Circle(x, y, radius);
    index = pointsCount;
    reached = false;
    color = Color.GREEN;
    pointsCount++;
    enabled = false;
  }

  public void draw(ShapeDrawer drawer) {
    drawer.setColor(color);
    drawer.setDefaultLineWidth(5);
    drawer.circle(circle.x, circle.y, circle.radius);
  }

  public boolean overlaps(Charge charge) {
    return charge.circle.overlaps(circle);
  }

  public void check(Charge charge) {
    if (!reached && overlaps(charge) && enabled) {
      setReached(true);
    }
  }

  public void resetRadius(Vector2 edgePos) {
    circle.radius = edgePos.cpy().sub(circle.x, circle.y).len();
  }
}
