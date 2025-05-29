package io.github.hey2022.qfield;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener, InputProcessor {

  static final float MIN_WORLD_WIDTH = 800;
  static final float MIN_WORLD_HEIGHT = 800;

  private ShapeRenderer shapeRender;
  private SpriteBatch batch;
  private OrthographicCamera camera;
  private OrthographicCamera hudCamera;
  private Viewport viewport;
  private Viewport hudViewport;
  private double camSpeed;
  private Vector2 touchPos;
  private BitmapFont font;

  private Array<Charge> charges;
  private Charge charge;
  private boolean init = false;
  private boolean cameraFollow = false;

  @Override
  public void create() {
    // Prepare your application here.
    if (Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
      shapeRender = new ShapeRenderer();
      batch = new SpriteBatch();
      font = new BitmapFont();
    }

    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    viewport = new ScreenViewport(camera);
    camSpeed = 200;

    hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    hudViewport = new ScreenViewport(hudCamera);

    touchPos = new Vector2();

    charge = new Charge(0, 0, 1, false, 1);
    charges = new Array<Charge>();

    Gdx.input.setInputProcessor(this);
  }

  @Override
  public void resize(int width, int height) {
    // Resize your application here. The parameters represent the new window size.
    viewport.update(width, height, true);
    hudViewport.update(width, height, true);
    if (!init) {
      centerCamera(charge);
      init = true;
    }
  }

  @Override
  public void render() {
    // Draw your application here.
    input();
    logic();
    if (shapeRender != null) {
      draw();
    }
  }

  private void draw() {
    ScreenUtils.clear(Color.WHITE);
    if (cameraFollow) {
      centerCamera(charge);
    }
    camera.update();
    hudCamera.update();
    shapeRender.setProjectionMatrix(camera.combined);
    batch.setProjectionMatrix(hudCamera.combined);
    Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);

    viewport.apply();
    shapeRender.begin(ShapeType.Filled);
    charge.draw(shapeRender);
    for (Charge q : charges) {
      q.draw(shapeRender);
    }
    shapeRender.end();

    viewport.apply();
    hudCamera.update();
    batch.setProjectionMatrix(hudCamera.combined);
    batch.begin();
    font.setColor(Color.BLACK);
    font.getData().setScale(2.0f);
    font.draw(batch, "FPS=" + Gdx.graphics.getFramesPerSecond(), 0, hudCamera.viewportHeight);
    batch.end();
  }

  private void logic() {
    float delta = Gdx.graphics.getDeltaTime();
    for (int i = 0; i < 8; i++) {
      charge.updateForce(charges);
      charge.update(delta);
      // System.out.println(charge.getPos());
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
      if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
        charges.add(new Charge(touchPos.x, touchPos.y, 1, true, 1));
      } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        charges.add(new Charge(touchPos.x, touchPos.y, -1, true, 1));
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
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
      case Input.Keys.SPACE:
        break;
      case Input.Keys.F:
        cameraFollow ^= true;
        break;
    }
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    touchPos.set(x, y);
    viewport.unproject(touchPos);
    if (button == Input.Buttons.LEFT) {
      charges.add(new Charge(touchPos.x, touchPos.y, 1, true, 1));
    } else if (button == Input.Buttons.RIGHT) {
      charges.add(new Charge(touchPos.x, touchPos.y, -1, true, 1));
    }
    return false;
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    return false;
  }

  @Override
  public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean mouseMoved(int x, int y) {
    return false;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    return false;
  }

  public void centerCamera(Charge charge) {
    Vector2 pos = charge.getPos();
    camera.position.set(pos.x, pos.y, camera.position.z);
  }
}
