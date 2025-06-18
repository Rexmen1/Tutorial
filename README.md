# Tutorial Plugin

A comprehensive quest-based tutorial system for Minecraft Spigot servers. This plugin provides an interactive way to guide new players through your server with customizable quests, GUI interface, boss bar progress tracking, and integration with popular plugins.

## Features

### 🎯 Quest Types

- **Command Quests**: Complete by executing specific commands
- **Region Quests**: Complete by entering WorldGuard regions
- **Kill Quests**: Complete by killing specific mobs
- **Break Quests**: Complete by breaking specific blocks
- **Place Quests**: Complete by placing specific blocks
- **Craft Quests**: Complete by crafting specific items
- **Eat Quests**: Complete by consuming specific items
- **Chat Quests**: Complete by sending specific messages in chat
- **Placeholder Quests**: Complete based on PlaceholderAPI values (statistics, economy, etc.)

### 🎮 User Interface

- **Interactive GUI**: Beautiful inventory-based quest browser
- **Boss Bar Progress**: Real-time progress tracking with customizable boss bars
- **Color-coded Status**: Visual indicators for completed, current, and locked quests

### 🔧 Integrations

- **WorldGuard**: Region-based quest completion
- **PlaceholderAPI**: Dynamic progress tracking using placeholders
- **Vault**: Economy and permission integration

### ⚙️ Customization

- **Fully Configurable**: Customize all messages, GUI layout, and quest parameters
- **Quest Rewards**: Support for commands, items, money, and more
- **Progress Persistence**: Player progress saved across server restarts
- **Performance Optimized**: Efficient event handling and memory management

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin files in `plugins/Tutorial/`

## Dependencies

### Required

- **Spigot/Paper**: 1.20.1 or higher
- **Java**: 17 or higher

### Optional

- **WorldGuard**: For region-based quests
- **PlaceholderAPI**: For statistic and placeholder-based quests
- **Vault**: For economy integration

## Commands

### Player Commands

- `/quest` - Open quest GUI
- `/tutorial` - Alias for `/quest`

### Admin Commands

- `/quest reload` - Reload configuration and quests

## Permissions

- `tutorial.user` - Access to basic quest commands (default: true)
- `tutorial.admin` - Access to admin commands like reload (default: op)

## Quick Setup

1. Edit `plugins/Tutorial/quests.yml` to define your quests
2. Customize `plugins/Tutorial/config.yml` for your server
3. Use `/quest reload` to apply changes without restart

## Support

- Report issues on GitHub
- Check console for debug information when `debug: true` in config
- Ensure all dependencies are properly installed

For detailed configuration and troubleshooting, see the wiki documentation.

## Recent Updates

### New Quest Types Added

- **Break Quests**: Track blocks broken by players
- **Place Quests**: Track blocks placed by players
- **Craft Quests**: Track items crafted by players
- **Eat Quests**: Track food items consumed by players
- **Chat Quests**: Trigger completion when players type specific messages

### Bug Fixes

- Fixed GUIListener registration issue
- Improved quest progress tracking accuracy
- Enhanced boss bar display for all quest types
- Fixed memory leak issues with boss bar cleanup
- Updated configuration file formatting

### Performance Improvements

- Optimized event handling for new quest types
- Improved quest validation and error handling
- Enhanced debug logging capabilities

## Author

- Rex
- Website: [rexkraft.com](https://rexkraft.com)

## License

This project is licensed under the MIT License - see the LICENSE file for details.
