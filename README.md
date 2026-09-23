# Blokish Debugging Tasks

Blokish is an Android version of the **Blokus** board game. You will complete two debugging tasks in this repository.

## How the Game Works

Up to four players place pieces on a 20 × 20 board. Pieces from the same player may touch at a corner but may not share an edge. The goal is to place as many squares as possible.

### Starting a Game

To open the game menu, swipe from the left side of the screen toward the right and select **New Game**.

Available pieces appear in the piece-selection area.

### Pieces and Controls

Pieces can be dragged from the selection area onto the board. While a piece is active, it can be moved, rotated, flipped, accepted, or returned.

![Dragging a piece](app/src/main/res/drawable/blokish_dragging.png)

![Rotating a piece](app/src/main/res/drawable/blokish_rotating.png)

![Placing pieces](app/src/main/res/drawable/blokish_2_pieces.png)

- Drag a piece to move it.
- Drag the circular controls to rotate it.
- Long press the piece to flip it.
- Select **✓** to accept the piece.
- Select **X** to return the piece.

## Testing Code Changes

The Android emulator and Blokish will already be running when you begin.

After modifying the code, use the commands provided in the task instructions to rebuild and restart Blokish.

If the emulator closes or becomes unavailable, **notify the researcher**.

## Tasks

Complete the tasks in order:

1. [Task 1: Multiple-Piece Interaction](debugging-tasks/task-1-multiple-piece-interaction-state.md)
2. [Task 2: Bottom Circular Rotation Control](debugging-tasks/task-2-bottom-rotation-control.md)

Please complete Task 1 before beginning Task 2.