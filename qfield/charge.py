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
        total_force = np.array([0.0, 0.0])
        for charge in charges:
            total_force += self.electrostatic_force(charge)
        return total_force

    def electrostatic_force(self, other):
        """Calculate electrostatic force with Coulomb's Law"""
        r = (self.position - other.position) * self.SCALE
        r_magnitude = np.linalg.norm(r)
        force_magnitude = (
            self.K
            * (self.charge * other.charge * r_magnitude)
            / (r_magnitude**2 + self.HEIGHT**2) ** (3 / 2)
        )
        force = force_magnitude * (r / r_magnitude)
        return force

    def render(self, screen):
        """Render charge"""
        pygame.draw.circle(
            screen,
            self.color,
            (int(self.position[0]), int(self.position[1])),
            self.RADIUS,
        )

    def render_velocity(self, screen):
        end = self.position + self.velocity / self.SCALE / 1e15
        draw.draw_arrow(screen, self.position, end, "blue")

    def render_force(self, screen):
        end = (
            self.position
            + self.force / self.SCALE * screen.get_size() * screen.get_size()
        )
        draw.draw_arrow(screen, self.position, end, "red")
