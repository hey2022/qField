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

  private SpriteBatch hudBatch;
  private PolygonSpriteBatch batch;
  private OrthographicCamera camera;
  private OrthographicCamera hudCamera;
  private Viewport viewport;
  private Viewport hudViewport;
  private double camSpeed;
  private Vector2 touchPos;
  private BitmapFont font;
  private TextureRegion region;
  private ShapeDrawer drawer;
  private DecimalFormat df = new DecimalFormat("0.000E0");

  private Array<Charge> charges;
  private Charge charge;
  private Array<Checkpoint> checkpoints;
  private boolean addingCheckpoint;
  private int checkCount;
  private boolean cameraFollow = false;
  private boolean paused = true;
  private float timeStep = 3e-8f;
  public static final float SCALE = 1e-6f;

  enum InputMode {
    CHARGE,
    CHECKPOINT
  }

  InputMode inputMode;

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
      pixmap.drawPixel(0, 0);
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      region = new TextureRegion(texture, 0, 0, 1, 1);
      drawer = new ShapeDrawer(batch, region);
      checkCount = 0;
    }

    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    viewport = new ScreenViewport(camera);
    camSpeed = 200;

    hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    hudViewport = new ScreenViewport(hudCamera);

    touchPos = new Vector2();

    charge = new Charge(MIN_WORLD_WIDTH / 2 * SCALE, MIN_WORLD_HEIGHT / 2 * SCALE, 1, false, 1);
    charges = new Array<Charge>();

    checkpoints = new Array<Checkpoint>();
    addingCheckpoint = false;

    inputMode = InputMode.CHARGE;
    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void resize(int width, int height) {
    // Resize your application here. The parameters represent the new window size.
    viewport.update(width, height, true);
    hudViewport.update(width, height, true);
  }

  @Override
  public void render() {
    // Draw your application here.
    input();
    if (!paused) {
      logic();
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

    for (Checkpoint point : checkpoints) {
      point.draw(drawer);
    }

    for (Charge q : charges) {
      if (inCamera(q.getScreenPos())) q.draw(drawer);
    }
    charge.draw(drawer);

    for (Checkpoint point : checkpoints) {
      point.draw(drawer);
    }

    Draw.drawTargetArrow(
        drawer, camera, charge.getScreenPos(), 25, (float) Math.PI / 4, Color.BLACK);
    batch.end();
    drawHud();
  }

  private void drawHud() {
    hudViewport.apply();
    hudCamera.update();
    hudBatch.setProjectionMatrix(hudCamera.combined);
    hudBatch.begin();
    font.setColor(Color.BLACK);
    font.draw(
        hudBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, hudCamera.viewportHeight - 10);
    font.draw(
        hudBatch,
        "Total energy: " + df.format(charge.energy(charges)),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 10,
        0,
        Align.right,
        false);
    font.draw(
        hudBatch,
        "Kinetic energy: " + df.format(charge.kineticEnergy()),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 30,
        0,
        Align.right,
        false);
    font.draw(
        hudBatch,
        "Potential energy: " + df.format(charge.electricPotential(charges)),
        hudCamera.viewportWidth - 10,
        hudCamera.viewportHeight - 50,
        0,
        Align.right,
        false);
    Vector2 position = charge.getPos();
    font.draw(
        hudBatch,
        "Position: (" + df.format(position.x) + ", " + df.format(position.y) + ")",
        hudCamera.viewportWidth - 10,
        15,
        0,
        Align.right,
        false);
    hudBatch.end();
  }

  private void logic() {
    for (int i = 0; i < 8; i++) {
      charge.updateForce(charges);
      charge.update(timeStep);
    }

    for (int i = checkpoints.size - 1; i >= 0; i--) {
      checkpoints.get(i).check(charge);
    }
  }

  private void input() {
    float displacement = (float) (camSpeed * Gdx.graphics.getDeltaTime());
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
      camera.translate(-displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
      camera.translate(displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
      camera.translate(0, displacement, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
      camera.translate(0, -displacement, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isTouched()) {
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
        paused ^= true;
        break;
      case Input.Keys.F:
        cameraFollow ^= true;
        break;
      case Input.Keys.R:
        reset();
        break;
      case Input.Keys.C:
        clear();
        break;
      case Input.Keys.P:
        Vector2 cursorPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(cursorPos);
        checkpoints.add(new Checkpoint(cursorPos, 30));
        break;
      case Input.Keys.G:
        toggleInputMode();
        break;
    }
    return false;
  }

  void toggleInputMode() {
    switch (inputMode) {
      case CHARGE:
        inputMode = InputMode.CHECKPOINT;
        break;
      case CHECKPOINT:
        inputMode = InputMode.CHARGE;
        break;
    }
  }

  public void reset() {
    charge.reset(MIN_WORLD_WIDTH / 2 * SCALE, MIN_WORLD_HEIGHT / 2 * SCALE);
    centerCamera(charge);
    paused = true;
    for (Checkpoint point : checkpoints) {
      point.setChecked(false);
    }
  }

  public void clear() {
    switch (inputMode) {
      case CHARGE:
        charges = new Array<Charge>();
        break;
      case CHECKPOINT:
        checkpoints = new Array<Checkpoint>();
        break;
    }
    reset();
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
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
          addCheckpoint(touchPos.x, touchPos.y, 30);
          addingCheckpoint = true;
          break;
      }
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (inputMode == InputMode.CHECKPOINT) {
      addingCheckpoint = false;
    }

    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    Vector2 curPos = new Vector2(screenX, screenY);
    viewport.unproject(curPos);
    if (addingCheckpoint) {
      Checkpoint point = checkpoints.peek();
      point.resetRadius(curPos);
    }
    return false;
  }

  public void addCheckpoint(float x, float y, float radius) {
    checkpoints.add(new Checkpoint(x, y, radius));
  }

  public void centerCamera(Charge charge) {
    Vector2 pos = charge.getScreenPos();
    camera.position.set(pos.x, pos.y, camera.position.z);
    camera.update();
  }

  public void addCharge(float x, float y, float charge, boolean fixed, float mass) {
    charges.add(new Charge(x, y, charge, fixed, mass));
    if (paused) {
      this.charge.updateForce(charges);
    }
  }

  public boolean isInCamera(float x, float y) {
    return inCamera(new Vector2(x, y));
  }

  /**
   * @param Pos
   * @return whether the positon can be viewed by camera
   */
  public boolean inCamera(Vector2 Pos) {
    Vector2 screenPos = viewport.project(Pos.cpy());
    if (screenPos.x < -10
        || screenPos.x > viewport.getScreenWidth() + 10
        || screenPos.y < -10
        || screenPos.y > viewport.getScreenHeight() + 10) {
      return false;
    }
    return true;
  }
}
