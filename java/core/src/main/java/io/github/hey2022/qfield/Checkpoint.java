package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** Checkpoint */
public class Checkpoint {
  private static int pointsCount = 0;

  public int index;
  private Circle hitbox;
  private boolean checked;

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
    if (checked == false) {
      color = Color.GREEN;
    } else {
      color = Color.LIGHT_GRAY;
    }
  }

  private Color color;

  public Checkpoint(Vector2 pos, float radius) {
    hitbox = new Circle(pos, radius);
    index = pointsCount;
    checked = false;
    color = Color.GREEN;
    pointsCount++;
  }

  public Checkpoint(float x, float y, float radius) {
    hitbox = new Circle(x, y, radius);
    index = pointsCount;
    checked = false;
    color = Color.GREEN;
    pointsCount++;
  }

  public void draw(ShapeDrawer drawer) {
    drawer.filledCircle(hitbox.x, hitbox.y, hitbox.radius, color);
  }

  public boolean overlaps(Charge charge) {
    Circle chargeCircle = new Circle(charge.getScreenPos(), Charge.RADIUS);
    return chargeCircle.overlaps(hitbox);
  }

  public void check(Charge charge) {
    if (!checked && overlaps(charge)) {
      setChecked(true);
      System.out.printf("checkpoint %d checked\n", index);
    }
  }
}
