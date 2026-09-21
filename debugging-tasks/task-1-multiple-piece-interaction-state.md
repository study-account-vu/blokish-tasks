# Task 1: Multiple-Piece Interaction State

## Goal

Ensure that the board permits only one active, uncommitted piece interaction at a time. Once a piece is placed or returned, the board should be ready for the next interaction without leaving an earlier piece stuck or unresponsive.

## Background

A selected piece remains active while it is being positioned on the board. The selection area can also contain pieces that have not yet been placed. The implementation must keep these states separate and must not allow a second piece selection to invalidate or hide the first active interaction.

## Reproduction Steps

1. Start a new game.
2. Drag a piece onto the board.
3. Leave that piece active instead of accepting (clicking the check mark) or returning it (clicking the X mark).
4. Interact with the piece-selection area so another piece can be dragged onto the board.
5. Add a second piece, and if useful add a third piece to make the state easier to observe.
6. Try to move or cancel the earlier piece. For example, if you put 2 pieces, you will observe that only the recent piece (piece B) can be moved where as the initial piece placed (piece A) is stuck in place until you return the more recent piece (piece B).

## Expected Behavior

Only one piece should be active at a time. Placing or returning a piece should clear the active selection and restore normal interaction. Selecting another piece must not leave a previous piece in a hidden or conflicting state.

## Acceptance Checks

- A second piece cannot create a competing active selection.
- The current piece can always be cancelled or validated without losing access to it.
- After cancellation or validation, another piece can be selected normally.
- Existing drag, rotation, long-press flip, and legal-move validation behavior remains unchanged.
