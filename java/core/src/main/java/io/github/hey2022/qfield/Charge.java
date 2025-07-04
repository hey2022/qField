package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Charge {
  private static final float[] c = {
    0.03809449742241219545697532230863756534060f,
    0.1452987161169137492940200726606637497442f,
    0.2076276957255412507162056113249882065158f,
    0.4359097036515261592231548624010651844006f,
    -0.6538612258327867093807117373907094120024f,
    0.4359097036515261592231548624010651844006f,
    0.2076276957255412507162056113249882065158f,
    0.1452987161169137492940200726606637497442f,
  };
  private static final float[] d = {
    0.09585888083707521061077150377145884776921f,
    0.2044461531429987806805077839164344779763f,
    0.2170703479789911017143385924306336714532f,
    -0.01737538195906509300561788011852699719871f,
    -0.01737538195906509300561788011852699719871f,
    0.2170703479789911017143385924306336714532f,
    0.2044461531429987806805077839164344779763f,
    0.09585888083707521061077150377145884776921f,
  };
  private final float ELEMENTAL_CHARGE = 1.602e-19f;
  private final float K = 8.988e9f;
  private final float PROTON_MASS = 1.673e-27f;
  public static final float RADIUS = 10;
  private static final float SELECTED_LIGHT_FACTOR = 1 / 1.5f;
  private final float HEIGHT = 9e-6f;
  private final float HEIGHT2 = (float) Math.pow(HEIGHT, 2);
  private final int PATH_LENGTH = 10000;

  private Vector2 position;
  private Vector2 screenPosition;
  private Vector2 force;
  private Vector2 acceleration;
  private Vector2 velocity;
  private float mass;
  private float distanceTraveled;
  private boolean fixed;
  private Color color;
  private float charge;
  private Path path;
  private boolean selected;
  public Circle circle;
  public boolean drawArrow = true;
  public boolean drawPath = true;

  public Charge(float x, float y, float charge, boolean fixed, float mass) {
    this.mass = mass * this.PROTON_MASS;
    this.fixed = fixed;
    this.color = charge > 0 ? Color.RED : Color.BLUE;
    this.charge = charge * this.ELEMENTAL_CHARGE;
    this.reset(x, y);
    this.selected = false;
  }

  public void reset(float x, float y) {
    position = new Vector2(x, y);
    screenPosition = position.cpy().scl(1 / Main.SCALE);
    circle = new Circle(screenPosition, RADIUS);
    velocity = new Vector2(0, 0);
    acceleration = new Vector2(0, 0);
    force = new Vector2(0, 0);
    path = new Path(PATH_LENGTH);
    distanceTraveled = 0.0f;
  }

  public void applyForce(Vector2 force) {
    this.force = force.cpy();
    this.acceleration = force.cpy().scl(1f / this.mass);
  }

  public void updateForce(Array<Charge> charges) {
    applyForce(superposition(charges));
  }

  public void update(Array<Charge> charges, float timeStep) {
    if (fixed) return;
    Vector2 initialPosition = position.cpy();
    for (int i = 0; i < 8; i++) {
      updateForce(charges);
      velocity.add(acceleration.cpy().scl(timeStep * d[i]));
      position.add(velocity.cpy().scl(timeStep * c[i]));
    }
    distanceTraveled += initialPosition.sub(position).len();
    screenPosition = position.cpy().scl(1 / Main.SCALE);
    circle.setPosition(screenPosition);
    path.add(screenPosition);
  }

  public float getCharge() {
    return charge;
  }

  public Vector2 getPos() {
    return position;
  }

  public Vector2 getScreenPos() {
    return screenPosition;
  }

  public void setPos(Vector2 pos) {
    this.position = pos;
  }

  public boolean isSelected() {
    return selected;
  }

  public void select() {
    this.selected = true;
  }

  public void unselect() {
    this.selected = false;
  }

  public Vector2 superposition(Array<Charge> charges) {
    if (charges == null || charges.size == 0) {
      return new Vector2(0, 0);
    }

    Vector2 force = new Vector2(0, 0);
    for (int i = 0; i < charges.size; i++) {
      Charge charge = charges.get(i);
      Vector2 r = position.cpy().sub(charge.getPos());
      float r2 = r.len2();
      if (r2 == 0) continue; // Skip self interaction
      float f =
          (K * charge.getCharge() * this.charge * (float) Math.sqrt(r2))
              / (float) Math.pow((r2 + HEIGHT2), 1.5);
      force.add(r.nor().scl(f));
    }
    return force;
  }

  public float energy(Array<Charge> charges) {
    return kineticEnergy() + electricPotential(charges);
  }

  public float kineticEnergy() {
    float kineticEnergy = (float) (1.0 / 2.0 * mass * velocity.len2());
    return kineticEnergy;
  }

  public float electricPotential(Array<Charge> charges) {
    float electricPotential = 0.0f;
    if (charges == null) {
      return electricPotential;
    }

    float HEIGHT2 = (float) Math.pow(HEIGHT, 2);
    for (int i = 0; i < charges.size; i++) {
      Charge charge = charges.get(i);
      Vector2 r = position.cpy().sub(charge.getPos());
      float r2 = r.len2();
      if (r2 == 0) continue; // Skip self interaction
      float u = (K * charge.getCharge() * this.charge * r.len()) / (r2 + HEIGHT2);
      electricPotential += u;
    }
    return electricPotential;
  }

  public void draw(ShapeDrawer drawer) {
    if (!fixed) {
      if (drawPath) {
        path.drawPath(drawer);
      }
      if (drawArrow) {
        Vector2 end = screenPosition.cpy().mulAdd(velocity, 1e-6f / Main.SCALE);
        Draw.drawArrow(drawer, screenPosition.cpy(), end.cpy(), Color.BLUE);

        end = screenPosition.cpy().mulAdd(force, 1e15f / Main.SCALE);
        Draw.drawArrow(drawer, screenPosition.cpy(), end.cpy(), Color.RED);
      }
    }

    Color tempColor = color.cpy();
    if (selected) {
      tempColor.mul(SELECTED_LIGHT_FACTOR);
    }
    drawer.filledCircle(screenPosition, RADIUS, tempColor);
  }

  public float getDistanceTraveled() {
    return distanceTraveled;
  }
}
