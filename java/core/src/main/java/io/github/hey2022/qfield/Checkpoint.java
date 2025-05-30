package io.github.hey2022.qfield;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** Checkpoint */
public class Checkpoint {
  private static int pointsCount = 0;

  public int sequence;
  private Circle hitbox;

  public Checkpoint(Vector2 pos, float radius) {
    hitbox = new Circle(pos, radius);
    sequence = ++pointsCount;
  }

  public void draw(ShapeDrawer drawer, Viewport viewport) {
    drawer.circle(hitbox.x, hitbox.y, hitbox.radius);
  }

  public boolean overlaps(Charge charge) {
    Circle chargeCircle = new Circle(charge.getPos(), Charge.RADIUS);
    return chargeCircle.overlaps(hitbox);
  }
}
