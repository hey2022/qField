import numpy as np
import pygame


def draw_arrow(screen, start, end, color, width=5, L=5, H=10):
    """Draw an arrow from start to end with arrowhead of half-length L and height H."""
    direction = end - start
    length = np.linalg.norm(direction)
    if length == 0:
        return
    pygame.draw.line(screen, color, start, end, width)

    unit = direction / length
    perp = np.array([-unit[1], unit[0]])
    end = end + H * unit
    left = end - H * unit - L * perp
    right = end - H * unit + L * perp
    pygame.draw.polygon(screen, color, [end, left, right])
