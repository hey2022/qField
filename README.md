# qField

It is a picturesque electric charge simulation inspired by
[electric-hockey](https://phet.colorado.edu/en/simulations/electric-hockey).
You can play it [online](https://hey2022.github.io/qField/).

In addition to the game, a reinforcement learning (RL) model is planned to be trained on the game.

## Demo
[![qField](https://img.youtube.com/vi/l5Vjn7yWPfQ/0.jpg)](https://www.youtube.com/watch?v=l5Vjn7yWPfQ "qField")

## Game keybinds

> [!IMPORTANT]
> [In Firefox, if you hold down the `shift` key while right-clicking, then the context menu is shown without the contextmenu event being fired.](https://developer.mozilla.org/en-US/docs/Web/API/Element/contextmenu_event#canceling_the_contextmenu_event)
> This prevents us from preventing the context menu from appearing when shift right-clicking, a workaround is to set `dom.event.contextmenu.shift_suppresses_event` to `false` in `about:config`.

### Game controls

- G: Switch between charge and checkpoint mode.
- C: Clear all charges & checkpoints.
- R: Reset free charge to origin.
- I: Toggle force and velocity arrows
- P: Toggle charge path
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
