import sys

import pygame
import numpy as np
from charge import Charge
from colors import WHITE


class qfield:
    def __init__(self, interactive=True, width=800, height=800):
        self.interactive = interactive
        if self.interactive:
            pygame.init()
            self.screen = pygame.display.set_mode((width, height), pygame.RESIZABLE)
            self.world = pygame.Surface((10000, 10000))
            pygame.display.set_caption("Electric field simulation")
            self.clock = pygame.time.Clock()
            pygame.font.init()
            self.font = pygame.font.Font(None, 36)

        self.running = True

        self.CAMERA_SPEED = 5
        self.charges = []
        self.time_step = 3e-8
        self.fps = 120
        self.reset()

    def input(self):
        """Handle all input events"""
        world_pos = self.camera + pygame.mouse.get_pos()
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                self.running = False
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    self.charges.append(Charge(*world_pos, 1, True))
                elif event.button == 2:
                    self.charge.reset(*world_pos)
                elif event.button == 3:
                    self.charges.append(Charge(*world_pos, -1, True))
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE or event.key == pygame.K_q:
                    self.running = False
                elif event.key == pygame.K_r:
                    self.reset()
                elif event.key == pygame.K_c:
                    self.clear()
                elif event.key == pygame.K_SPACE:
                    self.paused = not self.paused

        keys = pygame.key.get_pressed()
        if keys[pygame.K_RIGHT]:
            self.update()

        if keys[pygame.K_w]:
            self.camera += np.array([0, -1]) * self.CAMERA_SPEED
        if keys[pygame.K_a]:
            self.camera += np.array([-1, 0]) * self.CAMERA_SPEED
        if keys[pygame.K_s]:
            self.camera += np.array([0, 1]) * self.CAMERA_SPEED
        if keys[pygame.K_d]:
            self.camera += np.array([1, 0]) * self.CAMERA_SPEED
        if keys[pygame.K_LSHIFT]:
            if pygame.mouse.get_pressed()[0]:
                self.charges.append(Charge(*world_pos, 1, True))
            if pygame.mouse.get_pressed()[2]:
                self.charges.append(Charge(*world_pos, -1, True))

    def update(self):
        """Update game state"""
        for _ in range(8):
            force = self.charge.superposition(self.charges)
            self.charge.apply_force(force)
            self.charge.update(self.time_step)

    def render_frame(self):
        """Render frame"""
        self.screen.fill(WHITE)
        pygame.draw.rect(self.world, WHITE, (*self.camera, *self.screen.get_size()))

        for charge in self.charges:
            charge.render(self.world, self.camera, self.screen.get_size())
        self.charge.render_velocity(self.world)
        self.charge.render_force(self.world)
        self.charge.render(self.world)

        self.screen.blit(self.world, (0, 0), (*self.camera, *self.screen.get_size()))

        self.render_fps(self.screen)
        pygame.display.flip()

    def render_fps(self, screen, color="black"):
        """Display the current FPS in the top left corner of the screen"""
        fps = int(self.clock.get_fps())
        fps_text = f"FPS: {fps}"
        fps_surface = self.font.render(fps_text, True, color)

        screen.blit(fps_surface, (10, 10))

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
        self.charge = Charge(*np.array(self.world.get_size()) // 2, 1, False)
        self.camera = (
            np.array(self.world.get_size()) // 2 - np.array(self.screen.get_size()) // 2
        )
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
