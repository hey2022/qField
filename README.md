# qField

This is a take on the traditional hockey game inspired by
[electric-hockey](https://phet.colorado.edu/en/simulations/electric-hockey) where
the hockey puck is a charged particle, and players control
the game by placing positive and negative charges on the
field to attract or repel the puck. The goal is to maneuver
the puck into the opponent's goal while defending your own.

In addition to the game, an reinforcement learning (RL) is
trained for playing the game.

## Game keybinds

### Game controls

- G: Switch between charge and checkpoint mode.
- C: Clear all charges & checkpoints.
- R: Reset free charge to origin.
- I: Toggle force and velocity arrows
- UP: Increase game speed.
- DOWN: Decrease game speed.
- SPACE: Start/Pause game.
- 1..9: Switch to level. Switching to currently active level changes game to sandbox mode.

### Camera controls

- WASD: Move the camera around.
- F: Toggle camera follow charge.
- +: Zoom in.
- -: Zoom out.
- 0: Reset zoom.

### Charge mode controls

- (shift) Left click: (repeatedly) Add positive charge.
- Middle click: Move free charge to cursor.
- (shift) Right click: (repeatedly) Add negative charge.
- (shift) X: (repeatedly) Delete selected charge under cursor.

### Checkpoint mode controls

- Left click: Add checkpoint.
- Click and drag: Add checkpoint and adjust size.
- (shift) X: (repeatedly) Delete selected checkpoint under cursor.
