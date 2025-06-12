package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

  public static void drawTargetArrow(
      ShapeDrawer drawer,
      OrthographicCamera hudCamera,
      OrthographicCamera camera,
      Vector2 end,
      float radius,
      float central_angle,
      Color color) {
    float hw = camera.zoom * camera.viewportWidth / 2;
    float hh = camera.zoom * camera.viewportHeight / 2;
    Vector2 start = new Vector2(camera.position.x, camera.position.y);
    if ((start.x - hw <= end.x && end.x <= start.x + hw)
        && (start.y - hh <= end.y && end.y <= start.y + hh)) {
      return;
    }
    Vector2 direction = end.cpy().sub(start);
    float xt = Math.abs(hw / (end.x - start.x));
    float yt = Math.abs(hh / (end.y - start.y));
    float min_t = Math.min(xt, yt);
    Vector2 hudPos = new Vector2(hudCamera.position.x, hudCamera.position.y);
    Vector2 intersect =
        start.cpy().mulAdd(direction, min_t).sub(start).scl(1.0f / camera.zoom).add(hudPos);
    float angle = (float) Math.atan2(start.y - end.y, start.x - end.x);
    drawer.sector(
        intersect.x, intersect.y, radius, angle - central_angle / 2, central_angle, color, color);
  }
}
