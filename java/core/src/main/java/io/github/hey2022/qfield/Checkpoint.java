package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** Checkpoint */
public class Checkpoint {
  private static int pointsCount = 0;

  public int sequence;
  private Circle hitbox;

  public Checkpoint(Vector2 pos, float radius) {
    hitbox = new Circle(pos, radius);
    sequence = pointsCount;
    pointsCount++;
  }

  public void draw(ShapeDrawer drawer) {
    drawer.filledCircle(hitbox.x, hitbox.y, hitbox.radius, Color.GREEN);
  }

  public boolean overlaps(Charge charge) {
    Circle chargeCircle = new Circle(charge.getScreenPos(), Charge.RADIUS);
    return chargeCircle.overlaps(hitbox);
  }
}
