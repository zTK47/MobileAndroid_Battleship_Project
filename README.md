Fleet Command: BattleshipBrief ReportFleet Command is an Android client for the classic two-player Battleship game, developed for the FHNW Mobile Applications with Android course.
The application allows two real clients to join the same online game using a shared game key and play a complete match through the official REST server:
http://brad-home.ch:50003
The project was created as an Android Studio Panda project using Kotlin, Jetpack Compose, Material 3, Canvas, ViewModel, Retrofit and OkHttp.
Project MemberName	Contribution
Davide Pham	Individual project: game logic, networking, user interface, testing and documentation

Although the original assignment describes a group project, this submission was completed individually.
Project RequirementsOfficial requirement	Implementation
Openable in Android Studio Panda	Standard Android Gradle project
Kotlin Android application	Entire application written in Kotlin
Complete game against another client	Two clients communicate through the official server
REST communication	Retrofit and OkHttp with JSON
Project documentation	This README is included in the repository
Android 11 compatibility	Minimum SDK is API 30
Internet access	Declared in AndroidManifest.xml
External-code attribution	AI assistance and external libraries are documented

Running the ProjectOpen the project folder in Android Studio Panda.
Wait for Gradle synchronization to finish.
Select an Android emulator or physical device running Android 11 or newer.
Run the app configuration.
Repeat this process on a second device or emulator for an online match.
The server uses HTTP rather than HTTPS. A restricted network-security exception is configured specifically for brad-home.ch.
How to PlayOpen the application on two Android devices.
Select Enter Battle.
On the first device, select the Host option and enter a commander name.
Copy the automatically generated game key.
On the second device, select Join and enter a different name and the exact same game key.
Configure the five ships on both devices.
Use Rotate or Randomize to create a valid fleet.
Select Connect to Online Game on both clients.
The server randomly selects which player begins.
The selected player fires by tapping an unmarked cell on the enemy grid.
After every shot, the client waits for the opponent through the server.
The game continues until one complete fleet is destroyed.
A waiting screen does not represent a computer opponent. It means the real /game/join request is waiting for the other client or for the opponent’s first move.
Battleship RulesThe game uses a 10×10 board. The interface displays columns A–J and rows 1–10, while the server uses coordinates from 0 to 9.
Ship	JSON name	Length
Aircraft Carrier	Carrier	5
Battleship	Battleship	4
Destroyer	Destroyer	3
Submarine	Submarine	3
Patrol Boat	PatrolBoat	2

Ships can be horizontal or vertical. They cannot overlap or extend outside the board. The application validates the complete fleet before connecting.
Application FeaturesHost and Join workflows using a shared game key
Interactive 10×10 fleet-placement board
Friendly and enemy battle grids
Ship rotation and random fleet generation
Hit, miss and sunk-ship indicators
Turn and connection-status messages
Victory and defeat screens
Pause, reconnect, leave and rematch controls
English, Italian, French and German interface languages
Responsive layouts for phones and wider displays
Full-screen naval illustrations, animated sea scenery and ship silhouettes
ArchitectureThe project follows a simple state-driven architecture:
Aggiungi alla chat
MainActivity
    └── BattleshipApp
        ├── BattleshipViewModel
        ├── Screens and reusable UI components
        ├── Game-domain rules
        └── Repository
            └── Retrofit REST API

GameDomain.kt contains ship definitions, placement validation, coordinate conversion and sunk-fleet calculations.
BattleshipViewModel.kt owns the application state and controls navigation, connection status, turns, shots and final results.
BattleshipRepository.kt separates network communication from the user interface and converts HTTP or server errors into application results.
The Compose screens receive immutable state and report user actions back to the ViewModel.
REST ProtocolMethod	Endpoint	Purpose
GET	/ping	Check server availability
POST	/game/join	Register player, game key and fleet
POST	/game/fire	Fire at an enemy coordinate
POST	/game/enemyFire	Wait for and receive the opponent’s move

The application checks both HTTP errors and the JSON Error field because a valid server path may return HTTP 200 together with a game-related error.
Join and enemy-fire requests use long-polling because the server may wait for the opponent. The application never enters the battle screen before receiving a valid server response.
Error HandlingThe application handles:
Names or game keys shorter than three characters
Invalid, overlapping or out-of-board ship positions
Duplicate targets
Incorrect game keys or player names
Games that already contain two players
“Not your turn” server responses
HTTP 418 invalid mappings
Server Error responses
Connection failures and timeouts
Late responses from cancelled game rooms
TestingThe project contains 21 automated local tests covering:
Fleet generation and placement validation
Exact ship names and JSON mappings
Coordinate conversion
Hit, miss and sunk-ship calculations
HTTP and server-error handling
Localization availability
Two clients joining with the same game key
Isolation of games using different keys
Both possible starting-player selections
Alternating turns and complete-game completion
Protection against late responses from cancelled rooms
An Android instrumentation smoke test also checks the home screen and Host/Join navigation.
bash



./gradlew testDebugUnitTest

A final acceptance test should be performed on two devices or emulators using the official server.
Sources and AttributionOfficial Battleship project page
Battleship server source
Course project specification: Battleship_Project.pdf
FHNW Mobile Applications with Android lecture slides
Jetpack Compose
Retrofit
OkHttp
The optional JavaFX client was considered as a protocol and usability reference. No source code or protected artwork was copied from it.
OpenAI Codex was used as an implementation, testing, design and documentation assistant. Significant assisted source files contain attribution comments. The project member remains responsible for understanding and presenting the implementation.
The official project page confirms that Android clients may use Jetpack Compose and that the REST server normally runs on port 50003. (brad-home.ch)
