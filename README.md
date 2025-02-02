# Tutorial Plugin

A Minecraft Spigot/Paper plugin that introduces new players to the server's unique features using a quest-based system.

## Features

- Quest System with Multiple Objective Types:
	- Command-Based Quests
	- Region-Based Quests (WorldGuard integration)
	- Mob Killing Quests
	- Item Collection Quests
	- PlaceholderAPI Condition Quests

- Interactive GUI Quest Tracker
- BossBar Guidance System
- Configurable Reward System
- Quest Progress Persistence

## Requirements

- Spigot/Paper 1.20+
- WorldGuard
- PlaceholderAPI
- Vault

## Installation

1. Download the latest release
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure quests in `plugins/Tutorial/quests.yml`

## Commands

- `/quest` - Opens the quest tracker GUI
- `/quest reload` - Reloads plugin configuration (Requires `tutorial.admin` permission)

## Permissions

- `tutorial.user` - Access to basic tutorial commands (Default: true)
- `tutorial.admin` - Access to admin commands (Default: op)

## Configuration

The plugin uses several configuration files:

- `config.yml` - General plugin settings
- `quests.yml` - Quest definitions and rewards
- `data.yml` - Player progress data

## Author

- Rex
- Website: [rexkraft.com](https://rexkraft.com)

## License

This project is licensed under the MIT License - see the LICENSE file for details.