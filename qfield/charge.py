import pygame
import numpy as np
from colors import RED, BLUE


class Charge:
    ELEMENTAL_CHARGE = 1.602e-19  # C
    K = 8.988e9  # Nm^2/C^2
    PROTON_MASS = 1.673e-27  # kg
    SCALE = 1e-9  # pixel to nm
    MIN_RADIUS = 10 * SCALE

    RADIUS = 10  # render purposes only

    def __init__(self, x, y, charge, fixed, mass=1.0):
        self.color = RED if charge > 0 else BLUE

        self.charge = charge * self.ELEMENTAL_CHARGE
        self.mass = mass * self.PROTON_MASS
        self.fixed = fixed

        self.position = np.array([float(x), float(y)])
        self.velocity = np.array([0.0, 0.0])

    def apply_force(self, force):
        """Newton's Second Law"""
        self.acceleration = force / self.mass

    def update(self, time_step):
        """Update position"""
        if self.fixed:
            return

        self.velocity += self.acceleration * time_step
        self.position += self.velocity * time_step

        self.acceleration = np.array([0.0, 0.0])

    def superposition(self, charges):
        """Calculate superposition of electrostatic forces"""
        total_force = np.array([0.0, 0.0])
        for charge in charges:
            total_force += self.electrostatic_force(charge)
        return total_force

    def electrostatic_force(self, other):
        """Calculate electrostatic force with Coulumb's Law"""
        r = (self.position - other.position) * self.SCALE
        r_norm = np.linalg.norm(r)
        if r_norm < self.MIN_RADIUS:
            print(r_norm)
            r *= self.MIN_RADIUS / r_norm
        r_magnitude = np.linalg.norm(r)
        force_magnitude = self.K * (self.charge * other.charge) / (r_magnitude**2)
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
