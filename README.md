# Fleet Command: Battleship

An Android Battleship client created for the FHNW Mobile Applications with Android course.
The application plays a complete two-player game through the course REST server and is built
with Kotlin, Jetpack Compose, Material 3, Canvas drawing, ViewModel state, Retrofit and OkHttp.

## Project Member

- Davide Pham - individual project

## Open the Project

1. Open this repository in Android Studio Panda.
2. Allow Gradle synchronization to finish.
3. Select an Android emulator or physical device running Android 11 (API 30) or newer.
4. Run the `app` configuration.

After updating the project, uninstall any older Battleship build and rebuild the app before
testing. Do not reuse an APK generated before the online-only networking changes.

The application requires internet access. It connects to:

`http://brad-home.ch:50003`

The course server uses unencrypted HTTP, so cleartext traffic is enabled only because it is
required by the supplied protocol.

## Requirement Compliance

| Requirement | Implementation |
|---|---|
| Android Studio Panda, Kotlin, Jetpack Compose | Empty Activity project with Compose-only UI |
| Android 11 support | `minSdk = 30` |
| Complete two-client game | Join, alternating fire/enemyFire loop and final result handling |
| All required REST mappings | `/ping`, `/game/join`, `/game/fire`, `/game/enemyFire` |
| Official 10x10 fleet | Carrier 5, Battleship 4, Destroyer 3, Submarine 3, PatrolBoat 2 |
| Valid placement | Bounds, overlap, orientation and complete-fleet checks |
| Local game state | Friendly/enemy shots, sunk ships, turn, pause, status and outcome |
| Error handling | HTTP errors, JSON `Error`, invalid input, unavailable server and retry |
| Course UI concepts | Scaffold, Column, Row, Box, TextField, Button, lists, Canvas and themes |
| Documentation and attribution | This README plus author/AI comments in significant files |

## How to Play

1. Start the app on two Android devices or emulators.
2. Select **Enter Battle** from the full-screen home menu.
3. Enter a player name (e.g., Davide on Client 1, Viet on Client 2) and the same game key (e.g., `test123`).
4. Configure the fleet on both devices. Choose a vessel and tap the grid to move its bow.
5. Use **Rotate**, or generate a valid layout with **Randomize**.
6. Select **Connect to Online Game** on both clients.
7. The server determines the first player. When it's **Your turn**, tap an unmarked sector on the targeting grid.
8. Continue until the server reports that the game is over.

## Structure

- `model/`: Pure Battleship rules and REST models.
- `network/`: Retrofit API and Repository handling with 60s long-polling timeout.
- `ui/screens/`: Jetpack Compose screens for every stage of the game.
- `ui/components/`: Custom Canvas board and reusable UI panels.

## Testing

The project includes 21 unit tests covering:
- Random fleet legality and placement rules.
- Coordinate conversion (A-J 1-10 to 0-9).
- Two-client alternating turn flow.
- Server error and JSON `Error` field handling.

Run tests via: `./gradlew testDebugUnitTest`

## Sources and Attribution

- Game requirements and REST protocol: supplied `Battleship_Project.pdf`
- Course concepts: supplied FHNW Android course slides.
- OpenAI Codex was used as an implementation and design assistant.
