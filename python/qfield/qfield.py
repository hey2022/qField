import sys

import pygame
import numpy as np
from charge import Charge
from colors import WHITE


class qfield:
    SCALE = 1e-6

    def __init__(self, interactive=True, width=800, height=800):
        self.interactive = interactive
        if self.interactive:
            pygame.init()
            self.screen = pygame.display.set_mode((width, height), pygame.RESIZABLE)
            pygame.display.set_caption("Electric field simulation")
            self.clock = pygame.time.Clock()
            pygame.font.init()
            self.font = pygame.font.Font(None, 36)

        self.running = True

        self.CAMERA_SPEED = 5
        self.SHIFT_CAMERA_SPEED_MULTIPLIER = 2
        self.camera_follow = False
        self.charges = []
        self.time_step = 3e-8
        self.fps = 120
        self.reset()

    def input(self):
        """Handle all input events"""
        world_pos = (self.camera + pygame.mouse.get_pos()) * self.SCALE
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                self.running = False
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    self.add_charge(*world_pos, 1, True)
                elif event.button == 2:
                    self.charge.reset(*world_pos)
                    self.charge.update_force(self.charges)
                elif event.button == 3:
                    self.add_charge(*world_pos, -1, True)
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE or event.key == pygame.K_q:
                    self.running = False
                elif event.key == pygame.K_r:
                    self.reset()
                elif event.key == pygame.K_c:
                    self.clear()
                elif event.key == pygame.K_f:
                    self.camera_follow = not self.camera_follow
                elif event.key == pygame.K_SPACE:
                    self.paused = not self.paused

        keys = pygame.key.get_pressed()
        if keys[pygame.K_RIGHT]:
            self.update()

        camera_velocity = np.array([0, 0])
        camera_speed = self.CAMERA_SPEED
        if keys[pygame.K_w]:
            camera_velocity += np.array([0, -1])
        if keys[pygame.K_a]:
            camera_velocity += np.array([-1, 0])
        if keys[pygame.K_s]:
            camera_velocity += np.array([0, 1])
        if keys[pygame.K_d]:
            camera_velocity += np.array([1, 0])

        if keys[pygame.K_LSHIFT]:
            camera_speed *= self.SHIFT_CAMERA_SPEED_MULTIPLIER
            if pygame.mouse.get_pressed()[0]:
                self.add_charge(*world_pos, 1, True)
            if pygame.mouse.get_pressed()[2]:
                self.add_charge(*world_pos, -1, True)

        self.camera += camera_velocity * camera_speed

    def update(self):
        """Update game state"""
        for _ in range(8):
            self.charge.update_force(self.charges)
            self.charge.update(self.time_step)

    def add_charge(self, x, y, charge, fixed):
        """Add a new charge to the simulation and update force for moving charge"""
        self.charges.append(Charge(x, y, charge, fixed))
        if self.paused:
            self.charge.update_force(self.charges)

    def render_frame(self):
        """Render frame"""
        self.screen.fill(WHITE)

        if self.camera_follow and not self.paused:
            self.center_camera(self.charge)

        for charge in self.charges:
            charge.render(self.screen, self.camera)
        self.charge.render_velocity(self.screen, self.camera)
        self.charge.render_force(self.screen, self.camera)
        self.charge.render(self.screen, self.camera)

        self.render_fps(self.screen)

        pygame.display.flip()

    def render_fps(self, screen, color="black"):
        """Display the current FPS in the top left corner of the screen"""
        fps = int(self.clock.get_fps())
        fps_text = f"FPS: {fps}"
        fps_surface = self.font.render(fps_text, True, color)

        screen.blit(fps_surface, (10, 10))

    def center_camera(self, charge):
        """Center the camera onto the charge"""
        self.camera = charge.position - np.array(self.screen.get_size()) / 2

    def run(self):
        """Main loop"""
        while self.running:
            if self.interactive:
                self.input()
            if not self.paused:
                self.update()
            if self.interactive:
                self.render_frame()
                self.clock.tick(self.fps)
        if self.interactive:
            exit()

    def reset(self):
        self.charge = Charge(0, 0, 1, False)
        self.center_camera(self.charge)
        self.paused = True

    def clear(self):
        self.charges = []
        self.reset()

    def exit(self):
        pygame.quit()
        sys.exit()


if __name__ == "__main__":
    simulation = qfield(True)
    simulation.run()
