package io.github.hey2022.qfield;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Charge {
    private static final double[] c = {
            0.03809449742241219545697532230863756534060,
            0.1452987161169137492940200726606637497442,
            0.2076276957255412507162056113249882065158,
            0.4359097036515261592231548624010651844006,
            -0.6538612258327867093807117373907094120024,
            0.4359097036515261592231548624010651844006,
            0.2076276957255412507162056113249882065158,
            0.1452987161169137492940200726606637497442,
    };
    private static final double[] d = {
            0.09585888083707521061077150377145884776921,
            0.2044461531429987806805077839164344779763,
            0.2170703479789911017143385924306336714532,
            -0.01737538195906509300561788011852699719871,
            -0.01737538195906509300561788011852699719871,
            0.2170703479789911017143385924306336714532,
            0.2044461531429987806805077839164344779763,
            0.09585888083707521061077150377145884776921,
    };
    private final double ELEMENTAL_CHARGE = 1.602e-19;
    private final double K = 8.988e9;
    private final double PROTON_MASS = 1.673e-27;
    private final double SCALE = 1e-9;
    private final double RADIUS = 10;
    private final double HEIGHT = 9e-9;
    private int i = 0;

    private INDArray position;
    private INDArray force;
    private INDArray acceleration;
    private INDArray velocity;
    private double mass;
    private boolean fixed;
    private String color;
    private double charge;

    public Charge(double x, double y, double charge, boolean fixed, double mass) {
        this.reset(x, y);
        this.mass = mass * this.PROTON_MASS;
        this.fixed = fixed;
        this.color = charge > 0 ? "red" : "blue";
        this.charge = charge * this.ELEMENTAL_CHARGE;
    }

    public void reset(double x, double y) {
        position = Nd4j.createFromArray(new Double[] { x, y });
        velocity = Nd4j.zeros(DataType.DOUBLE, 2);
        acceleration = Nd4j.zeros(DataType.DOUBLE, 2);
        force = Nd4j.zeros(DataType.DOUBLE, 2);
    }

    public void applyForce(INDArray force) {
        this.force = force;
        this.acceleration = force.div(this.mass);
    }

    public void update(double time_step) {
        if (fixed)
            return;
        velocity.addi(acceleration.mul(time_step * d[i]));
        position.addi(velocity.mul(time_step * c[i]));
        i++;
        i %= 8; // 8th-order
    }

    public double getCharge() {
        return charge;
    }

    public INDArray getPos() {
        return position;
    }

    public INDArray superposition(Charge[] charges) {
        if (charges.length == 0) {
            return Nd4j.zeros(DataType.DOUBLE, 2);
        }
        int[] shape = { charges.length, 2 };
        INDArray positions = Nd4j.zeros(shape, DataType.DOUBLE);
        double[] q = new double[charges.length];
        for (i = 0; i < q.length; i++) {
            q[i] = charges[i].getCharge();
            positions.putRow(i, charges[i].getPos());
        }
        INDArray r_vectors = positions.rsub(position);
        INDArray r_squared = r_vectors.mul(r_vectors).sum(1);
        INDArray r_magnitude = Transforms.sqrt(r_squared);

        INDArray forces = Nd4j.zeros(shape, DataType.DOUBLE);
        INDArray force_magnitudes = Nd4j.zeros(DataType.DOUBLE, charges.length);
        for (i = 0; i < charges.length; i++) {
            force_magnitudes.putScalar(i, K * q[i] * charge / r_squared.getDouble(i));
            forces.putRow(i, r_vectors.getRow(i).mul(force_magnitudes.getDouble(i) / r_magnitude.getDouble(i)));
        }
        INDArray unit_vectors = r_vectors.div(r_magnitude.reshape(charges.length, 1));

        INDArray total_force = Nd4j.zeros(DataType.DOUBLE, 2);
        NdIndexIterator iter = new NdIndexIterator(forces.shape());
        while (iter.hasNext()) {
            long[] index = iter.next();
            total_force.addi(forces.getRow(index[0]).mul(unit_vectors.getRow(index[0])));
        }
        total_force = total_force.div(charges.length);
        return total_force;
    }

    // TODO draw the charge

}
