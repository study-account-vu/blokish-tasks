# Blokish Debugging Tasks

This folder contains a short orientation to Blokish and two focused debugging tasks.

The tasks are intended to let participants explore the repository and develop their own code-navigation and debugging strategy. This page only explains how the game works and describes the behavior to investigate.

## How the Game Works

Blokish is an Android version of the **Blokus** board game. Up to four players place pieces on a 20 x 20 board. A piece must touch one of the player's own pieces at a corner, but pieces from the same player may not share an edge. The goal is to place as many squares as possible.

### Starting a Game

Start a new game from the app menu. The game opens with the board and the available pieces shown in the piece-selection area. The app can use AI opponents, or AI can be disabled for local human play.

### Pieces

Each player begins with 21 polyomino pieces, made from one to five connected squares. Pieces begin in the piece-selection area and can be dragged onto the board.

![Piece placement](../app/src/main/res/drawable/blokish_2_pieces.png)

### Piece Controls

While a piece is selected, it can be moved around the board. The circular controls rotate it, a long press flips it, and the confirmation or cancel control accepts or returns it.

![Dragging a piece](../app/src/main/res/drawable/blokish_dragging.png)

![Rotating a piece](../app/src/main/res/drawable/blokish_rotating.png)

## Testing a Change

After making a code change, rebuild and reinstall the app before testing it. Two terminals are recommended: the emulator keeps running in the first terminal while the build and Android commands run in the second.

### Terminal 1: Start the Emulator

Run this command and leave it running:

```bash
emulator -avd Blokish_API35
```

### Terminal 2: Build, Install, and Launch

From the repository root, confirm that the emulator is available:

```bash
adb devices
```

The emulator should appear in the device list. Then build and install the latest code:

```bash
./gradlew clean :app:installDebug
```

Launch the app:

```bash
adb shell am start -n org.scoutant.blokish/.UI
```

Repeat the build, install, and launch commands after each code change you want to test.

## Tasks

You will begin the following tasks in order. 

- [Task 1: Multiple-piece interaction state](task-1-multiple-piece-interaction-state.md)
- [Task 2: Bottom circular rotation control](task-2-bottom-rotation-control.md)
