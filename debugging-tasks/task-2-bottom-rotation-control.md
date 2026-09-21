# Task 2: Bottom Circular Rotation Control

## Goal

Make the bottom circular control rotate a selected piece in the same way as every other circular rotation handle.

## Background

A selected piece displays circular controls around its bounds. These controls are touch targets for rotation. The visual reference below shows the intended rotation interaction:

![Rotating a piece](../app/src/main/res/drawable/blokish_rotating.png)


## Reproduction Steps

1. Start a new game.
2. Drag a piece onto the board.
3. Select or manipulate the piece until its circular controls are visible.
4. Drag one of the other circular rotation controls and confirm that it rotates as expected.
5. Drag the bottom circular control.
6. Compare the result with the other controls.

## Expected Behavior

The bottom circular control should be a valid rotation handle. Dragging it should rotate the selected piece consistently with the other circular controls and should not move the piece incorrectly.

## Acceptance Checks

- Each circular control rotates the selected piece consistently.
- The bottom control no longer causes incorrect translation or movement.
- The piece remains selectable and movable after rotation.
- Existing drag, flip, cancel, validation, and board-rule behavior remains unchanged.
