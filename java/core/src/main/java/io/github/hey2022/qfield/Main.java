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
  private DecimalFormat df = new DecimalFormat("0.000E0");

  private Array<Charge> charges;
  private Charge charge;
  private Array<Checkpoint> checkpoints;
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
      pixmap.drawPixel((int) initalPos.x, (int) initalPos.y);
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      region = new TextureRegion(texture, 0, 0, 1, 1);
      drawer = new ShapeDrawer(batch, region);
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

    checkpoints = new Array<Checkpoint>();
    checkCount = 0;

    inputMode = InputMode.CHARGE;
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

    Draw.drawTargetArrow(
        drawer, camera, charge.getScreenPos(), 25, (float) Math.PI / 4, Color.BLACK);
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
        for (int i = checkpoints.size - 1; i >= 0; i--) {
          if (!foundSelection && checkpoints.get(i).circle.contains(cursorPos)) {
            checkpoints.get(i).select();
            foundSelection = true;
          } else {
            checkpoints.get(i).unselect();
          }
        }
    }
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
    for (int i = 0; i < 8; i++) {
      charge.updateForce(charges);
      charge.update(timeStep);
    }

    for (Checkpoint checkpoint : checkpoints) {
      checkpoint.check(charge);
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
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
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
        checkpoints.add(new Checkpoint(cursorPos, 30));
        break;
      case Input.Keys.G:
        toggleInputMode();
        break;
      case Input.Keys.X:
        delete();
        break;
    }
    return false;
  }

  void toggleInputMode() {
    switch (inputMode) {
      case CHARGE:
        inputMode = InputMode.CHECKPOINT;
        for (Charge q : charges) {
          q.unselect();
        }
        break;
      case CHECKPOINT:
        inputMode = InputMode.CHARGE;
        for (Checkpoint p : checkpoints) {
          p.unselect();
        }
        break;
    }
  }

  public void reset() {
    charge.reset(initalPos.x, initalPos.y);
    centerCamera(charge);
    paused = true;
    for (Checkpoint point : checkpoints) {
      point.setReached(false);
    }
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
        for (int i = checkpoints.size - 1; i >= 0; i--) {
          if (checkpoints.get(i).isSelected()) {
            checkpoints.removeIndex(i);
            break;
          }
        }
        break;
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
          break;
      }
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (checkpoints != null && checkpoints.size > 0 && inputMode == InputMode.CHECKPOINT) {
      checkpoints.peek().enabled = true;
    }

    return false;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    cursorPos.set(x, y);
    viewport.unproject(cursorPos);
    if (checkpoints != null && checkpoints.size > 0 && inputMode == InputMode.CHECKPOINT) {
      Checkpoint point = checkpoints.peek();
      point.resetRadius(cursorPos);
    }
    return false;
  }

  @Override
  public boolean mouseMoved(int x, int y) {
    cursorPos.set(x, y);
    viewport.unproject(cursorPos);
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

  public boolean inCamera(float x, float y) {
    return inCamera(new Vector2(x, y));
  }

  public boolean inCamera(Vector2 Pos) {
    Vector2 screenPos = viewport.project(Pos.cpy());
    return (-10 < screenPos.x && screenPos.x < viewport.getScreenWidth() + 10)
        && (-10 < screenPos.y && screenPos.y < viewport.getScreenHeight() + 10);
  }
}
