package io.github.hey2022.qfield;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
public class Main implements ApplicationListener {

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

  @Override
  public void create() {
    // Prepare your application here.
    if (Gdx.app.getType() != ApplicationType.HeadlessDesktop) {
      shapeRender = new ShapeRenderer();
      batch = new SpriteBatch();
      font = new BitmapFont();
    }

    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
    viewport = new ScreenViewport(camera);
    camSpeed = 200;

    hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
    hudViewport = new ScreenViewport(hudCamera);

    touchPos = new Vector2();

    charge = new Charge(0, 0, 1, false, 1);
    charges = new Array<Charge>();
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
    logic();
    if (shapeRender != null) {
      draw();
    }
  }

  private void draw() {
    ScreenUtils.clear(Color.WHITE);
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
    camera.update();
    float delta = Gdx.graphics.getDeltaTime();
    for (int i = 0; i < 8; i++) {
      charge.updateForce(charges);
      charge.update(delta);
      // System.out.println(charge.getPos());
    }
  }

  private void input() {
    float displacement = (float) (camSpeed * Gdx.graphics.getDeltaTime());
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      camera.translate(-displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      camera.translate(displacement, 0, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
      camera.translate(0, displacement, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      camera.translate(0, -displacement, 0);
    }

    if (Gdx.input.isTouched()) {
      touchPos.set(Gdx.input.getX(), Gdx.input.getY());
      viewport.unproject(touchPos);
      charges.add(new Charge(touchPos.x, touchPos.y, 1, true, 1));
      System.out.println(touchPos);
    }

    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {}
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
}
