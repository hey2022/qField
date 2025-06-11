package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** Checkpoint */
public class Checkpoint {
  private static int pointsCount = 0;
  private static final float SELECTED_LIGHT_FACTOR = 1 / 1.5f;

  public int index;
  public Circle circle;

  public boolean enabled;

  private boolean reached;
  private boolean selected;

  private Color color;

  public boolean isReached() {
    return reached;
  }

  public void setReached(boolean checked) {
    this.reached = checked;
    if (!checked) {
      color = Color.LIME;
    } else {
      color = Color.LIGHT_GRAY;
    }
  }

  public Checkpoint(Vector2 pos, float radius) {
    this(pos.x, pos.y, radius);
  }

  public Checkpoint(Vector2 pos, float radius, boolean enabled) {
    this(pos.x, pos.y, radius);
    this.enabled = enabled;
  }

  public void select() {
    selected = true;
  }

  public void unselect() {
    selected = false;
  }

  public boolean isSelected() {
    return selected;
  }

  public Checkpoint(float x, float y, float radius) {
    circle = new Circle(x, y, radius);
    index = pointsCount;
    reached = false;
    color = Color.LIME;
    pointsCount++;
    enabled = false;
  }

  public void draw(ShapeDrawer drawer) {
    Color tempColor = color.cpy();
    if (selected) {
      tempColor.mul(SELECTED_LIGHT_FACTOR);
    }
    drawer.setColor(tempColor);
    float defaultLineWidth = drawer.getDefaultLineWidth();
    drawer.setDefaultLineWidth(5);
    drawer.circle(circle.x, circle.y, circle.radius);
    drawer.setDefaultLineWidth(defaultLineWidth);
  }

  public boolean overlaps(Charge charge) {
    return charge.circle.overlaps(circle);
  }

  public boolean check(Charge charge) {
    if (!reached && overlaps(charge) && enabled) {
      setReached(true);
      return true;
    }
    return false;
  }

  public void resetRadius(Vector2 edgePos) {
    circle.radius = edgePos.cpy().sub(circle.x, circle.y).len();
  }
}
