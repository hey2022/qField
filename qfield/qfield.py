import sys

import pygame
from charge import Charge
from colors import WHITE


class qfield:
    def __init__(self, interactive=True, width=800, height=800):
        self.interactive = interactive
        if self.interactive:
            pygame.init()
            self.width = width
            self.height = height
            self.screen = pygame.display.set_mode((width, height))
            pygame.display.set_caption("Electric field simulation")
            self.clock = pygame.time.Clock()

        self.running = True
        self.paused = True

        self.charge = Charge(self.width // 2, self.height // 2, 1, False)
        self.charges = []
        self.time_step = 3e-8
        self.fps = 120

    def input(self):
        """Handle all input events"""
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                self.running = False
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    self.charges.append(Charge(*pygame.mouse.get_pos(), 1, True))
                elif event.button == 3:
                    self.charges.append(Charge(*pygame.mouse.get_pos(), -1, True))
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE or event.key == pygame.K_q:
                    self.running = False
                elif event.key == pygame.K_r:
                    self.restart()
                elif event.key == pygame.K_c:
                    self.clear()
                elif event.key == pygame.K_SPACE:
                    self.paused = not self.paused

        keys = pygame.key.get_pressed()
        if keys[pygame.K_RIGHT]:
            self.update()

    def update(self):
        """Update game state"""
        for _ in range(4):
            force = self.charge.superposition(self.charges)
            self.charge.apply_force(force)
            self.charge.update(self.time_step)

    def render_frame(self):
        """Render frame"""
        self.screen.fill(WHITE)

        for charge in self.charges:
            charge.render(self.screen)
        self.charge.render(self.screen)

        pygame.display.flip()

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

    def restart(self):
        self.charge = Charge(self.width // 2, self.height // 2, 1, False)
        self.paused = True

    def clear(self):
        self.charges = []
        self.restart()

    def exit(self):
        pygame.quit()
        sys.exit()


if __name__ == "__main__":
    simulation = qfield(True)
    simulation.run()
