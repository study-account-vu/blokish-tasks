# Task 2: Bottom Circular Rotation Control

## Bug Report

The circular controls around a selected piece can be used to rotate it. Currently, the bottom circular control does not rotate the piece like the other circular controls.

## Reproduce the Bug

1. Start a new game.
2. Drag a piece onto the board.
3. Leave the piece active so that its circular controls are visible.
4. Drag one of the other circular controls and observe that the piece rotates.
5. Drag the **bottom circular control** and compare the behavior.

**Current:** The other circular controls rotate the piece, but the bottom circular control moves the piece instead.

**Expected:** The bottom circular control should rotate the piece like the other circular controls.

## Your Task

**Investigate why the bottom circular control does not rotate the piece correctly, and modify the code to fix the bug.**

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