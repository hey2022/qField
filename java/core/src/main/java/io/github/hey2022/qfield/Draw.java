package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Draw {
  public static final float width = 5;
  public static final float L = 5;
  public static final float H = 10;

  public static void drawArrow(ShapeDrawer drawer, Vector2 start, Vector2 end, Color color) {
    Vector2 direction = end.cpy().sub(start);
    if (direction.len2() == 0) {
      return;
    }
    direction.nor();
    drawer.line(start, end, color, width);

    float endX, endY, leftX, leftY, rightX, rightY;

    endX = end.x + H * direction.x;
    endY = end.y + H * direction.y;
    leftX = endX - H * direction.x + L * direction.y;
    leftY = endY - H * direction.y - L * direction.x;
    rightX = endX - H * direction.x - L * direction.y;
    rightY = endY - H * direction.y + L * direction.x;
    drawer.filledTriangle(endX, endY, leftX, leftY, rightX, rightY, color);
  }
}
