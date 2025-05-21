import pygame
import numpy as np
import draw
from colors import RED, BLUE


class Charge:
    ELEMENTAL_CHARGE = 1.602e-19  # C
    K = 8.988e9  # Nm^2/C^2
    PROTON_MASS = 1.673e-27  # kg
    SCALE = 1e-9  # pixel to nm
    RADIUS = 10  # render purposes only
    HEIGHT = 9e-9  # height difference between fixed and free particles

    # SABA(10,6,4) symplectic integrator coefficients
    c = np.array(
        [
            0.03809449742241219545697532230863756534060,  # c0
            0.1452987161169137492940200726606637497442,  # c1
            0.2076276957255412507162056113249882065158,  # c2
            0.4359097036515261592231548624010651844006,  # c3
            -0.6538612258327867093807117373907094120024,  # c4
            0.4359097036515261592231548624010651844006,  # c3
            0.2076276957255412507162056113249882065158,  # c2
            0.1452987161169137492940200726606637497442,  # c1
        ]
    )
    d = np.array(
        [
            0.09585888083707521061077150377145884776921,  # d0
            0.2044461531429987806805077839164344779763,  # d1
            0.2170703479789911017143385924306336714532,  # d2
            -0.01737538195906509300561788011852699719871,  # d3
            -0.01737538195906509300561788011852699719871,  # d3
            0.2170703479789911017143385924306336714532,  # d2
            0.2044461531429987806805077839164344779763,  # d1
            0.09585888083707521061077150377145884776921,  # d0
        ]
    )
    pos = 0

    def __init__(self, x, y, charge, fixed, mass=1.0):
        self.color = RED if charge > 0 else BLUE
        self.charge = charge * self.ELEMENTAL_CHARGE
        self.mass = mass * self.PROTON_MASS
        self.fixed = fixed
        self.reset(x, y)

    def reset(self, x, y):
        self.position = np.array([float(x), float(y)])
        self.velocity = np.array([0.0, 0.0])
        self.acceleration = np.array([0.0, 0.0])
        self.force = np.array([0.0, 0.0])

    def apply_force(self, force):
        """Newton's Second Law"""
        self.force = force
        self.acceleration = force / self.mass

    def update(self, time_step):
        """Update position using SABA(10,6,4) symplectic integration"""
        if self.fixed:
            return
        self.velocity += self.acceleration * time_step * self.d[self.pos]
        self.position += self.velocity * time_step * self.c[self.pos]
        self.pos += 1
        self.pos %= 8

    def superposition(self, charges):
        """Calculate superposition of electrostatic forces"""
        if not charges:
            return np.array([0.0, 0.0])

        positions = np.array([charge.position for charge in charges])
        q = np.array([charge.charge for charge in charges])

        r_vectors = (self.position - positions) * self.SCALE
        r_squared = np.sum(r_vectors**2, axis=1)
        r_magnitudes = np.sqrt(r_squared)

        non_zero = r_magnitudes > 0
        forces = np.zeros((len(charges), 2))

        if np.any(non_zero):
            force_magnitudes = (
                self.K
                * (self.charge * q[non_zero] * r_magnitudes[non_zero])
                / (r_squared[non_zero] + self.HEIGHT**2) ** (3 / 2)
            )

            unit_vectors = r_vectors[non_zero] / r_magnitudes[non_zero].reshape(-1, 1)
            forces[non_zero] = force_magnitudes.reshape(-1, 1) * unit_vectors

        total_force = np.sum(forces, axis=0)

        return total_force

    def render(self, surface, camera_pos=None, screen_size=None):
        """Render charge if it's in view of the camera"""
        if camera_pos is not None and screen_size is not None:
            if not (
                camera_pos[0] - self.RADIUS
                <= self.position[0]
                <= camera_pos[0] + screen_size[0] + self.RADIUS
                and camera_pos[1] - self.RADIUS
                <= self.position[1]
                <= camera_pos[1] + screen_size[1] + self.RADIUS
            ):
                return

        pygame.draw.circle(
            surface,
            self.color,
            (int(self.position[0]), int(self.position[1])),
            self.RADIUS,
        )

    def render_velocity(self, surfce):
        end = self.position + self.velocity / self.SCALE * 1e-15
        draw.draw_arrow(surfce, self.position, end, "blue")

    def render_force(self, surface):
        end = self.position + self.force / self.SCALE * 1e6
        draw.draw_arrow(surface, self.position, end, "red")
