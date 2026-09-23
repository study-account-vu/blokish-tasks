# Task 1: Multiple-Piece Interaction

## Bug Report

Only one unconfirmed piece should be on the board at a time. Currently, it is possible to drag another piece onto the board while the first piece is still active.

## Reproduce the Bug

1. Start a new game.
2. Drag a piece (**Piece A**) onto the board.
3. Leave Piece A active. **Do not click ✓ or X.**
4. While Piece A is still active, **force another piece (Piece B) from the selection area onto the board by dragging from the center area between ✓ and X.**

**Current:** Piece B can be added while Piece A is still active. This can also leave Piece A stuck until Piece B is returned.

**Expected:** While a piece is active on the board, another piece should **not** be selectable or draggable onto the board.

## Your Task

**Investigate why another piece can be added while one is already active, and modify the code to fix the bug.**

## Test Your Fix

After making a code change, run the following from the Blokish repository root:

```bash
./gradlew clean :app:installDebug
```

Then restart Blokish:

```bash
adb shell am force-stop org.scoutant.blokish
adb shell am start -n org.scoutant.blokish/.UI
```

Repeat the reproduction steps above to check whether your fix works.