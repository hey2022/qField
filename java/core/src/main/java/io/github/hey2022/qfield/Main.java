package io.github.hey2022.qfield;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.text.DecimalFormat;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends InputAdapter implements ApplicationListener {
  static final float MIN_WORLD_WIDTH = 800;
  static final float MIN_WORLD_HEIGHT = 800;
  static final Vector2 initalPos = new Vector2(0, 0);

  private SpriteBatch hudBatch;
  private PolygonSpriteBatch batch;
  private OrthographicCamera camera;
  private OrthographicCamera hudCamera;
  private Viewport viewport;
  private Viewport hudViewport;
  private double camSpeed;
  private Vector2 touchPos;
  private Vector2 cursorPos;
  private BitmapFont font;
  private TextureRegion region;
  private ShapeDrawer drawer;
  private ShapeDrawer hudDrawer;
  private DecimalFormat df = new DecimalFormat("0.000E0");

  private Array<Charge> charges;
  private Charge charge;
  private Checkpoints checkpoints;
  private boolean cameraFollow = false;
  private boolean paused = true;
  private boolean started;
  private boolean finished;
  private float timeStep = 3e-8f;
  public static final float SCALE = 1e-6f;
  private float accumulator = 0.0f;
  private final float SPT = 0.01f;
  private float gameSpeed = 1.0f;

  private enum InputMode {
    CHARGE,
    CHECKPOINT
  }

  private enum GameMode {
    SANDBOX,
    GAME
  }

  InputMode inputMode;
  GameMode gameMode;
  Level level;

  @Override
  public void create() {
    // Prepare your application here.
    if (Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
      hudBatch = new SpriteBatch();
      batch = new PolygonSpriteBatch();
      font = new BitmapFont();

      // shape drawer
      Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
      pixmap.setColor(Color.WHITE);
      pixmap.drawPixel((int) initalPos.x, (int) initalPos.y);
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      region = new TextureRegion(texture, 0, 0, 1, 1);
      drawer = new ShapeDrawer(batch, region);
      hudDrawer = new ShapeDrawer(hudBatch, region);
    }

    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    viewport = new ScreenViewport(camera);
    camSpeed = 200;

    hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    hudViewport = new ScreenViewport(hudCamera);

    touchPos = new Vector2();
    cursorPos = new Vector2();

    charge = new Charge(0, 0, 1, false, 1);
    charges = new Array<Charge>();
    camera.update();

    checkpoints = new Checkpoints();

    inputMode = InputMode.CHARGE;
    gameMode = GameMode.SANDBOX;
    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void resize(int width, int height) {
    // Resize your application here. The parameters represent the new window size.
    viewport.update(width, height, false);
    hudViewport.update(width, height, true);
  }

  @Override
  public void render() {
    // Draw your application here.
    // System.out.println(started);
    input();
    if (!paused) {
      float delta = Gdx.graphics.getDeltaTime();
      float SPT = this.SPT / gameSpeed;
      accumulator += delta;
      while (accumulator >= SPT) {
        logic();
        accumulator -= SPT;
      }
    }
    if (Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
      draw();
    }
  }

  private void draw() {
    ScreenUtils.clear(Color.WHITE);
    if (cameraFollow) {
      centerCamera(charge);
    }
    viewport.apply();
    camera.update();
    Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();

    batch.setColor(Color.WHITE);
    drawer.update();

    // draw checkpoints first to make them under the charge
    checkpoints.draw(drawer);

    for (Charge q : charges) {
      if (inCamera(q.getScreenPos())) q.draw(drawer);
    }
    charge.draw(drawer);
    batch.end();
    drawHud();
  }

  private void select() {
    boolean foundSelection = false;
    switch (inputMode) {
      case CHARGE:
        for (int i = charges.size - 1; i >= 0; i--) {
          if (!foundSelection && charges.get(i).circle.contains(cursorPos)) {
            charges.get(i).select();
            foundSelection = true;
          } else {
            charges.get(i).unselect();
          }
        }
        break;
      case CHECKPOINT:
        checkpoints.select(cursorPos);
    }
  }

  private void drawHud() {
    hudViewport.apply();
    hudCamera.update();
    hudBatch.setProjectionMatrix(hudCamera.combined);
    hudBatch.begin();
    hudDrawer.update();
    Draw.drawTargetArrow(
        hudDrawer, hudCamera, camera, charge.getScreenPos(), 25, (float) Math.PI / 4, Color.BLACK);

    font.setColor(Color.BLACK);
    font.draw(
        hudBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, hudCamera.viewportHeight - 10);
    font.draw(
        hudBatch, String.format("Game speed: %.3f", gameSpeed), 10, hudCamera.viewportHeight - 30);
    font.draw(hudBatch, "Charges: " + charges.size, 10, hudCamera.viewportHeight - 50);
    font.draw(
        hudBatch,
        "Active Checkpoints: " + checkpoints.activeCheckpoints(),
        10,
        hudCamera.viewportHeight - 70);
    font.draw(
        hudBatch,
        "Completed Checkpoints: " + checkpoints.completedCheckpoints(),
        10,
        hudCamera.viewportHeight - 90);
    if (gameMode == GameMode.GAME) {
      font.draw(
          hudBatch,
          "Game Status: " + (finished ? "Finished" : (paused ? "Paused" : "Running")),
          10,
          hudCamera.viewportHeight - 110);
    }

    font.draw(
        hudBatch,
        String.format("Total energy: %s J", df.format(charge.energy(charges))),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 10,
        0,
        Align.right,
        false);
    font.draw(
        hudBatch,
        String.format("Kinetic energy: %s J", df.format(charge.kineticEnergy())),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 30,
        0,
        Align.right,
        false);
    font.draw(
        hudBatch,
        String.format("Potential energy: %s J", df.format(charge.electricPotential(charges))),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 50,
        0,
        Align.right,
        false);
    Vector2 position = charge.getPos();
    font.draw(
        hudBatch,
        String.format("Position: (%s m, %s m)", df.format(position.x), df.format(position.y)),
        hudCamera.viewportWidth - 10,
        15,
        0,
        Align.right,
        false);
    font.setColor(Color.GREEN);
    font.draw(
        hudBatch,
        inputMode == InputMode.CHARGE ? "" : "Checkpoint Mode",
        10,
        15,
        0,
        Align.left,
        false);
    hudBatch.end();
  }

  private void logic() {
    charge.update(charges, timeStep);
    checkpoints.check(charge);
    if (gameMode == GameMode.GAME) {
      if (checkpoints.allCompleted()) {
        finished = true;
        paused = true;
      }
    }
  }

  private void input() {
    float dt = Gdx.graphics.getDeltaTime();
    float displacement = (float) (camSpeed * dt);
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      camera.translate(-displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      camera.translate(displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      camera.translate(0, displacement, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      camera.translate(0, -displacement, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
      adjustZoom(1.0f * dt);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
      adjustZoom(-1.0f * dt);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && isPrep()) {
      if (Gdx.input.isKeyPressed(Input.Keys.X)) {
        delete();
      } else if (inputMode == InputMode.CHARGE && Gdx.input.isTouched()) {
        {
          touchPos.set(Gdx.input.getX(), Gdx.input.getY());
          viewport.unproject(touchPos);
          touchPos.scl(SCALE);
          if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            addCharge(touchPos.x, touchPos.y, 1, true, 1);
          } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            addCharge(touchPos.x, touchPos.y, -1, true, 1);
          }
        }
      }
    }

    select();
  }

  @Override
  public void pause() {
    // Invoked when your application is paused.
  }

  @Override
  public void resume() {
    // Invoked when your application is resumed after pause.
  }

  @Override
  public void dispose() {
    // Destroy application's resources here.
    if (Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
      batch.dispose();
      hudBatch.dispose();
      font.dispose();
      region.getTexture().dispose(); // Dispose texture here
    }
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.Q:
        Gdx.app.exit();
        break;
      case Input.Keys.SPACE:
        if (finished) {
          break;
        }
        paused ^= true;
        started = true;
        break;
      case Input.Keys.DOWN:
        if (gameSpeed > 0.125) {
          gameSpeed /= 2.0f;
        }
        break;
      case Input.Keys.UP:
        gameSpeed *= 2.0f;
        break;
      case Input.Keys.NUM_0:
        camera.zoom = 1.0f;
        break;
      case Input.Keys.F:
        cameraFollow ^= true;
        break;
      case Input.Keys.O:
        cameraFollow = false;
        centerCamera(0, 0);
        break;
      case Input.Keys.R:
        reset();
        break;
      case Input.Keys.C:
        if (gameMode == GameMode.GAME) {
          gameInit(level.levelNum);
          break;
        }
        clear();
        break;
      case Input.Keys.I:
        charge.drawArrow ^= true;
        break;
      case Input.Keys.P:
        if (gameMode == GameMode.GAME) {
          break;
        }
        checkpoints.add(cursorPos, 30, false);
        break;
      case Input.Keys.G:
        toggleInputMode();
        break;
      case Input.Keys.X:
        if (Gdx.input.isKeyPressed(Input.Keys.X) && isPrep()) {
          delete();
        }
        break;
      default:
        if (Input.Keys.NUM_1 <= keycode && keycode <= Input.Keys.NUM_9) {
          toggleGameMode(keycode - Input.Keys.NUM_0);
        }
    }
    return false;
  }

  boolean isPrep() {
    return !(gameMode == GameMode.GAME && started);
  }

  void toggleGameMode(int levelNum) {
    switch (gameMode) {
      case SANDBOX:
        gameInit(levelNum);
        break;
      case GAME:
        if (levelNum != this.level.levelNum) {
          gameInit(levelNum);
        } else {
          gameExit();
        }
        break;
    }
  }

  void gameInit(int levelNum) {
    clear();
    gameMode = GameMode.GAME;
    inputMode = InputMode.CHARGE;
    started = false;
    finished = false;
    level = new Level(levelNum, checkpoints);
    reset();
  }

  void gameExit() {
    gameMode = GameMode.SANDBOX;
    level = null;
    clear();
  }

  void toggleInputMode() {
    if (gameMode == GameMode.SANDBOX) {
      switch (inputMode) {
        case CHARGE:
          inputMode = InputMode.CHECKPOINT;
          for (Charge q : charges) {
            q.unselect();
          }
          break;
        case CHECKPOINT:
          if (gameMode == GameMode.SANDBOX) {
            inputMode = InputMode.CHARGE;
            checkpoints.unselect();
          }
          break;
      }
    }
  }

  public void reset() {
    charge.reset(initalPos.x, initalPos.y);
    if (gameMode == GameMode.SANDBOX) {
      centerCamera(charge);
    } else if (gameMode == GameMode.GAME) {
      centerCamera(level.getCameraPos());
    }
    paused = true;
    started = false;
    finished = false;
    checkpoints.reset();
  }

  public void delete() {
    switch (inputMode) {
      case CHARGE:
        for (int i = charges.size - 1; i >= 0; i--) {
          if (charges.get(i).isSelected()) {
            charges.removeIndex(i);
            break;
          }
        }
        if (paused) {
          this.charge.updateForce(charges);
        }
        break;
      case CHECKPOINT:
        checkpoints.delete();
        break;
    }
  }

  public void clear() {
    charges = new Array<Charge>();
    checkpoints = new Checkpoints();
    reset();
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    if (!isPrep()) {
      return false;
    }
    touchPos.set(x, y);
    viewport.unproject(touchPos);
    if (inputMode == InputMode.CHARGE) {
      touchPos.scl(SCALE);
      switch (button) {
        case Input.Buttons.LEFT:
          addCharge(touchPos.x, touchPos.y, 1, true, 1);
          break;
        case Input.Buttons.RIGHT:
          addCharge(touchPos.x, touchPos.y, -1, true, 1);
          break;
        case Input.Buttons.MIDDLE:
          charge.reset(touchPos.x, touchPos.y);
          break;
      }
    } else {
      switch (button) {
        case Input.Buttons.LEFT:
          checkpoints.add(touchPos, 30, false);
          break;
      }
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (inputMode == InputMode.CHECKPOINT && !checkpoints.empty()) {
      checkpoints.peek().enabled = true;
    }

    return false;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    cursorPos.set(x, y);
    viewport.unproject(cursorPos);
    if (inputMode == InputMode.CHECKPOINT && !checkpoints.empty() && !checkpoints.peek().enabled) {
      checkpoints.peek().resetRadius(cursorPos);
    }
    return false;
  }

  @Override
  public boolean mouseMoved(int x, int y) {
    cursorPos.set(x, y);
    viewport.unproject(cursorPos);
    return false;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    adjustZoom(amountY * 0.02f);
    return false;
  }

  public void centerCamera(Charge charge) {
    Vector2 pos = charge.getScreenPos();
    centerCamera(pos.x, pos.y);
  }

  public void centerCamera(float x, float y) {
    camera.position.set(x, y, camera.position.z);
    camera.update();
  }

  public void centerCamera(Vector2 pos) {
    centerCamera(pos.x, pos.y);
  }

  public void addCharge(float x, float y, float charge, boolean fixed, float mass) {
    charges.add(new Charge(x, y, charge, fixed, mass));
    if (paused) {
      this.charge.updateForce(charges);
    }
  }

  public boolean inCamera(float x, float y) {
    return inCamera(new Vector2(x, y));
  }

  public boolean inCamera(Vector2 Pos) {
    Vector2 screenPos = viewport.project(Pos.cpy());
    return (-10 < screenPos.x && screenPos.x < viewport.getScreenWidth() + 10)
        && (-10 < screenPos.y && screenPos.y < viewport.getScreenHeight() + 10);
  }

  private void adjustZoom(float delta) {
    if (camera.zoom > 0.1 || delta > 0) {
      camera.zoom += delta;
    }
  }
}
