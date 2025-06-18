# Tutorial Plugin - Changelog

## Version 1.1.0 (Latest)

### 🆕 New Features

#### New Quest Types Added

- **Break Quests**: Track blocks broken by players using `BlockBreakEvent`
- **Place Quests**: Track blocks placed by players using `BlockPlaceEvent`
- **Craft Quests**: Track items crafted by players using `CraftItemEvent`
- **Eat Quests**: Track food items consumed by players using `PlayerItemConsumeEvent`
- **Chat Quests**: Trigger completion when players type specific messages using `AsyncPlayerChatEvent`

#### Enhanced Quest System

- Added new fields to `Quest.java` model: `blockType` and `chatMessage`
- Extended quest validation in `QuestManager.java` for all new quest types
- Comprehensive event handling in `QuestListener.java` for new quest types
- Updated boss bar progress display to show progress for all quest types
- Enhanced GUI display to include information for new quest types

### 🐛 Bug Fixes

#### Core Fixes

- **Fixed GUIListener Registration**: The `GUIListener` was not properly registered in the main plugin class, causing GUI interactions to fail
- **Memory Leak Prevention**: Added proper boss bar cleanup on player quit and plugin disable
- **Progress Tracking**: Fixed inaccurate quest progress tracking for all quest types
- **YAML Configuration**: Fixed formatting issues in `config.yml` and `quests.yml`

#### Quest System Improvements

- Enhanced quest progress calculation accuracy
- Improved error handling for invalid material types and entity types
- Added proper validation for all quest configuration fields
- Fixed boss bar not updating correctly for new quest types

### ⚙️ Configuration Updates

#### Config.yml Enhancements

- Extended `gui.item_lore_format` to include lines for all new quest types:
  - `break_quest_lore` for break quests
  - `place_quest_lore` for place quests
  - `craft_quest_lore` for craft quests
  - `eat_quest_lore` for eat quests
  - `chat_quest_lore` for chat quests
- Improved YAML structure and formatting
- Added better default values and documentation

#### Quests.yml Updates

- Replaced with comprehensive quest definitions covering all quest types
- Added 10 progressive tutorial quests featuring:
  - Wood breaking and crafting
  - Building and placement
  - Combat and survival
  - Food consumption
  - Chat interactions
- Ensured proper YAML structure with no duplicate keys
- Added detailed descriptions and boss bar messages

### 🚀 Performance Improvements

#### Event Handling Optimization

- Optimized event listeners for better performance
- Added smart caching for quest lookups
- Improved memory usage for boss bar management
- Enhanced debug logging for troubleshooting

#### Code Quality Enhancements

- Added comprehensive error handling throughout the codebase
- Improved code documentation and comments
- Enhanced validation for all quest types
- Standardized naming conventions and code structure

### 🛠️ Technical Details

#### Files Modified

- `Tutorial.java` - Added GUIListener registration
- `Quest.java` - Added new fields and validation methods
- `QuestManager.java` - Extended quest loading and validation for new types
- `QuestListener.java` - Added event handlers for all new quest types
- `QuestGUI.java` - Updated GUI display for new quest information
- `config.yml` - Extended configuration for new quest types
- `quests.yml` - Complete replacement with new quest definitions
- `README.md` - Updated documentation with new features

#### Build System

- Successfully tested with Maven compilation
- Generated shaded JAR file with all dependencies
- Verified compatibility with Spigot 1.20.1+
- Confirmed proper plugin loading and initialization

### 📋 Quest Type Reference

| Quest Type  | Event Used                   | Configuration Fields          |
| ----------- | ---------------------------- | ----------------------------- |
| break       | BlockBreakEvent              | `block`, `amount`             |
| place       | BlockPlaceEvent              | `block`, `amount`             |
| craft       | CraftItemEvent               | `block`, `amount`             |
| eat         | PlayerItemConsumeEvent       | `block`, `amount`             |
| chat        | AsyncPlayerChatEvent         | `message`, `amount`           |
| kill        | EntityDeathEvent             | `mob`, `amount`               |
| command     | PlayerCommandPreprocessEvent | `command`                     |
| region      | PlayerMoveEvent              | `region`, `world`             |
| placeholder | PlayerMoveEvent              | `placeholder`, `target-value` |

### 🎯 Next Steps

The plugin is now feature-complete with:

- ✅ All requested quest types implemented
- ✅ Proper event handling for each type
- ✅ Comprehensive validation and error handling
- ✅ Updated documentation and configuration
- ✅ Successful build and compilation
- ✅ Memory leak fixes and performance optimizations

### 🔧 Deployment Ready

The plugin is ready for deployment with:

- JAR file built successfully: `Tutorial-1.0-SNAPSHOT.jar`
- All configurations properly formatted
- Complete quest progression system (10 quests)
- Full compatibility with required dependencies

---

## Previous Versions

### Version 1.0.0 (Base)

- Initial plugin with basic quest types (command, region, kill, placeholder)
- Basic GUI and boss bar functionality
- Core quest management system
