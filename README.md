# LWJGL 3 game

A WIP video game and engine using LWJGL 3.

I'm developing this by following [tutorials](https://www.youtube.com/watch?v=VS8wlS9hF8E&list=PLRIWtICgwaX0u7Rf9zkZhLoLuZVfUksDP) by [ThinMatrix](https://www.youtube.com/user/ThinMatrix). However, LWJGL 3 is used instead of LWJGL 2.

The Gradle build script was adapted from these links:

- http://wiki.lwjgl.org/wiki/Setting_Up_LWJGL_with_Maven
- https://discuss.gradle.org/t/how-to-use-lwjgl-or-how-to-use-native-libraries/7498

# Modules

## Engine

Game engine code

## App

Example video game using the engine

## World-Editor

World/level editor

# Usage

Launch the game with Gradle to detect the correct native libraries.

    gradle -p app run

Launch the editor.

    gradle -p world-editor run

# Future plans

0. Collision detection
0. Non-player characters
0. Interact with the world
0. Save/load created worlds
0. Save/load game

