package io.github.hey2022.qfield;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Level {
  private Vector2 cameraPos;
  public int levelNum;

  public Level(int level, Checkpoints checkpoints) {
    levelNum = level;
    Array<Vector2> checkpointPos = new Array<Vector2>();
    switch (levelNum) {
      case 1:
        cameraPos = new Vector2(200, 200);
        checkpointPos.add(new Vector2(400, 400));
        break;
      case 2:
        cameraPos = new Vector2(300, 300);
        checkpointPos.add(new Vector2(300, 300));
        checkpointPos.add(new Vector2(600, 400));
        break;
      case 3:
        cameraPos = new Vector2(300, 300);
        checkpointPos.add(new Vector2(200, 100));
        checkpointPos.add(new Vector2(400, 400));
        checkpointPos.add(new Vector2(600, 100));
        break;
      case 4:
        cameraPos = new Vector2(350, 350);
        checkpointPos.add(new Vector2(200, 100));
        checkpointPos.add(new Vector2(600, 100));
        checkpointPos.add(new Vector2(200, 500));
        checkpointPos.add(new Vector2(700, 500));
        break;
      default:
        cameraPos = new Vector2(350, 350);
        for (int i = 0; i < levelNum; i++) {
          float x = (float) Math.random() * 680;
          float y = (float) Math.random() * 680;
          checkpointPos.add(new Vector2(x, y));
        }
    }

    // Add all positions to checkpoints
    for (Vector2 pos : checkpointPos) {
      checkpoints.add(pos, 30, true);
    }
  }

  public Vector2 getCameraPos() {
    return cameraPos;
  }
}
