package io.github.hey2022.qfield;

import com.badlogic.gdx.math.Vector2;

public class Level {
  private Vector2 cameraPos;
  public int levelNum;

  public Level(int level, Checkpoints checkpoints) {
    levelNum = level;
    switch (level) {
      case 1:
        cameraPos = new Vector2(200, 200);
        checkpoints.add(new Vector2(400, 400), 30, true);
        break;
      case 2:
        cameraPos = new Vector2(300, 300);
        checkpoints.add(new Vector2(300, 300), 30, true);
        checkpoints.add(new Vector2(600, 400), 30, true);
        break;
      case 3:
        cameraPos = new Vector2(300, 300);
        checkpoints.add(new Vector2(200, 100), 30, true);
        checkpoints.add(new Vector2(400, 400), 30, true);
        checkpoints.add(new Vector2(600, 100), 30, true);
    }
  }

  public Vector2 getCameraPos() {
    return cameraPos;
  }
}
