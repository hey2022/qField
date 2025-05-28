package io.github.hey2022.qfield;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

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
    private final float SCALE = 1e-5f;
    public final float RADIUS = 10;
    private final float HEIGHT2 = 8.1e-17f;
    private int i = 0;

    private Vector2 position;
    private Vector2 force;
    private Vector2 acceleration;
    private Vector2 velocity;
    private float mass;
    private boolean fixed;
    private Color color;

    private float charge;

    public Charge(float x, float y, float charge, boolean fixed, float mass) {
        this.reset(x, y);
        this.mass = mass * this.PROTON_MASS;
        this.fixed = fixed;
        this.color = charge > 0 ? Color.RED : Color.BLUE;
        this.charge = charge * this.ELEMENTAL_CHARGE;
    }

    public void reset(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2(0, 0);
        acceleration = new Vector2(0, 0);
        force = new Vector2(0, 0);
    }

    public void applyForce(Vector2 force) {
        this.force = force;
        this.acceleration = force.scl(1f / this.mass);
    }

    public void updateForce(Array<Charge> charges) {
        applyForce(superposition(charges));
    }

    public void update(float time_step) {
        if (fixed)
            return;
        velocity.add(acceleration.scl(time_step * d[i]));
        position.add(velocity.scl(time_step * c[i]));
        i++;
        i %= 8; // 8th-order
    }

    public float getCharge() {
        return charge;
    }

    public Vector2 getPos() {
        return position;
    }

    public Vector2 superposition(Array<Charge> charges) {
        if (charges == null || charges.size == 0) {
            return new Vector2(0, 0);
        }

        Vector2[] positions = new Vector2[charges.size];
        float[] q = new float[charges.size];
        for (int i = 0; i < charges.size; i++) {
            Charge charge = charges.get(i);
            positions[i] = charge.getPos();
            q[i] = charge.getCharge();
        }

        Vector2 force = new Vector2(0, 0);
        for (int j = 0; j < positions.length; j++) {
            Vector2 r = positions[j].cpy().sub(position).scl(SCALE);
            float r2 = r.len2();
            if (r2 == 0)
                continue; // Skip self interaction
            float f = (K * q[j] * charge * r.len()) / (float) Math.pow((r2 + HEIGHT2), 1.5);
            force.add(r.nor().scl(f));
        }
        return force.scl(-1);
    }

    public void draw(ShapeRenderer shape) {
        shape.setColor(color);
        shape.circle(position.x, position.y, RADIUS);
    }

}
