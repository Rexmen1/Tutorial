package com.tutorial.managers;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestManager {
	private final Tutorial plugin;
	private final Map<Integer, Quest> quests;
	private final Map<UUID, Integer> playerProgress;
	private final Map<UUID, Map<Integer, Double>> playerQuestProgress; // Store individual quest progress
	private final File dataFile;
	private FileConfiguration questConfig;
	private FileConfiguration dataConfig;

	public QuestManager(Tutorial plugin) {
		this.plugin = plugin;
		this.quests = new HashMap<>();
		this.playerProgress = new HashMap<>();
		this.playerQuestProgress = new HashMap<>();
		this.dataFile = new File(plugin.getDataFolder(), "data.yml");

		loadQuests();
		loadPlayerData();
	}

	public void loadQuests() {
		File questFile = new File(plugin.getDataFolder(), "quests.yml");
		questConfig = YamlConfiguration.loadConfiguration(questFile);

		if (!questConfig.contains("quests")) {
			plugin.getLogger().warning("No quests section found in quests.yml!");
			return;
		}

		for (String key : questConfig.getConfigurationSection("quests").getKeys(false)) {
			try {
				int questId = Integer.parseInt(key);
				String path = "quests." + key + ".";

				// Validate required fields
				String name = questConfig.getString(path + "name");
				String type = questConfig.getString(path + "type");
				String bossbar = questConfig.getString(path + "bossbar");

				if (name == null || type == null || bossbar == null) {
					plugin.getLogger()
							.warning("Quest " + questId + " is missing required fields (name, type, or bossbar)!");
					continue;
				}

				Quest quest = new Quest(
						questId,
						name,
						type,
						questConfig.getString(path + "message", ""), // For backward compatibility
						bossbar,
						questConfig.getStringList(path + "description"),
						questConfig.getString(path + "gui-icon", "PAPER"),
						questConfig.getStringList(path + "reward"));

				// Set specific quest properties based on type
				switch (quest.getType().toLowerCase()) {
					case "command":
						String command = questConfig.getString(path + "command");
						if (command == null) {
							plugin.getLogger().warning("Command quest " + questId + " is missing command field!");
							continue;
						}
						quest.setCommand(command);
						break;
					case "region":
						String region = questConfig.getString(path + "region");
						String world = questConfig.getString(path + "world");
						if (region == null || world == null) {
							plugin.getLogger()
									.warning("Region quest " + questId + " is missing region or world field!");
							continue;
						}
						quest.setRegion(region);
						quest.setWorld(world);
						break;
					case "kill":
						String mob = questConfig.getString(path + "mob");
						int amount = questConfig.getInt(path + "amount", 1);
						if (mob == null || amount <= 0) {
							plugin.getLogger().warning("Kill quest " + questId + " has invalid mob or amount!");
							continue;
						}
						quest.setMobType(mob);
						quest.setAmount(amount);
						break;
					case "placeholder":
						String placeholder = questConfig.getString(path + "placeholder");
						String targetValue = questConfig.getString(path + "target-value");
						if (placeholder == null || targetValue == null) {
							plugin.getLogger().warning(
									"Placeholder quest " + questId + " is missing placeholder or target-value!");
							continue;
						}
						quest.setPlaceholder(placeholder);
						quest.setTargetValue(targetValue);
						break;
					case "break":
					case "place":
					case "craft":
					case "eat":
						String blockType = questConfig.getString(path + "block");
						int blockAmount = questConfig.getInt(path + "amount", 1);
						if (blockType == null || blockAmount <= 0) {
							plugin.getLogger().warning(
									type + " quest " + questId + " is missing block or amount field!");
							continue;
						}
						quest.setBlockType(blockType);
						quest.setAmount(blockAmount);
						break;
					case "chat":
						String chatMessage = questConfig.getString(path + "message");
						if (chatMessage == null) {
							plugin.getLogger().warning("Chat quest " + questId + " is missing message field!");
							continue;
						}
						quest.setChatMessage(chatMessage);
						quest.setAmount(questConfig.getInt(path + "amount", 1));
						break;
					default:
						plugin.getLogger().warning("Unknown quest type '" + type + "' for quest " + questId);
						continue;
				}

				quests.put(questId, quest);
				plugin.getLogger().info("Loaded quest " + questId + ": " + name);
			} catch (NumberFormatException e) {
				plugin.getLogger().warning("Invalid quest ID: " + key + " (must be a number)");
			} catch (Exception e) {
				plugin.getLogger().severe("Error loading quest " + key + ": " + e.getMessage());
			}
		}

		plugin.getLogger().info("Loaded " + quests.size() + " quests total.");
	}

	private void loadPlayerData() {
		if (!dataFile.exists()) {
			plugin.saveResource("data.yml", false);
		}
		dataConfig = YamlConfiguration.loadConfiguration(dataFile);

		if (dataConfig.contains("players")) {
			for (String uuid : dataConfig.getConfigurationSection("players").getKeys(false)) {
				UUID playerUUID = UUID.fromString(uuid);
				int currentQuest = dataConfig.getInt("players." + uuid + ".current-quest");
				playerProgress.put(playerUUID, currentQuest);

				// Load individual quest progress
				Map<Integer, Double> questProgress = new HashMap<>();
				if (dataConfig.contains("players." + uuid + ".quest-progress")) {
					for (String questId : dataConfig.getConfigurationSection("players." + uuid + ".quest-progress")
							.getKeys(false)) {
						double progress = dataConfig.getDouble("players." + uuid + ".quest-progress." + questId);
						questProgress.put(Integer.parseInt(questId), progress);
					}
				}
				playerQuestProgress.put(playerUUID, questProgress);
			}
		}
	}

	public void saveAllData() {
		try {
			for (Map.Entry<UUID, Integer> entry : playerProgress.entrySet()) {
				String uuid = entry.getKey().toString();
				dataConfig.set("players." + uuid + ".current-quest", entry.getValue());

				// Save individual quest progress
				Map<Integer, Double> questProgress = playerQuestProgress.get(entry.getKey());
				if (questProgress != null) {
					for (Map.Entry<Integer, Double> progressEntry : questProgress.entrySet()) {
						dataConfig.set("players." + uuid + ".quest-progress." + progressEntry.getKey(),
								progressEntry.getValue());
					}
				}
			}
			dataConfig.save(dataFile);
		} catch (IOException e) {
			plugin.getLogger().severe("Could not save player data: " + e.getMessage());
		}
	}

	public Quest getQuest(int id) {
		return quests.get(id);
	}

	public int getCurrentQuest(Player player) {
		return playerProgress.getOrDefault(player.getUniqueId(), 1);
	}

	public void completeQuest(Player player, int questId) {
		playerProgress.put(player.getUniqueId(), questId + 1);
		saveAllData();
	}

	public boolean hasCompletedQuest(Player player, int questId) {
		return getCurrentQuest(player) > questId;
	}

	public Collection<Quest> getAllQuests() {
		return quests.values();
	}

	public void reloadQuests() {
		quests.clear();
		File questFile = new File(plugin.getDataFolder(), "quests.yml");
		questConfig = YamlConfiguration.loadConfiguration(questFile);

		// Load defaults from JAR if file is missing or empty
		try (InputStreamReader reader = new InputStreamReader(plugin.getResource("quests.yml"),
				StandardCharsets.UTF_8)) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
			questConfig.setDefaults(defaultConfig);
			if (!questFile.exists() || questConfig.getConfigurationSection("quests") == null) {
				questConfig.options().copyDefaults(true);
			}
		} catch (Exception e) {
			plugin.getLogger().warning("Could not load default quests configuration: " + e.getMessage());
		}

		questConfig.options().copyDefaults(true); // Ensure defaults are copied
		loadQuests();
	}

	public double getQuestProgress(Player player, int questId) {
		UUID uuid = player.getUniqueId();
		Map<Integer, Double> progress = playerQuestProgress.get(uuid);
		if (progress != null) {
			return progress.getOrDefault(questId, 0.0);
		}
		return 0.0;
	}

	public void setQuestProgress(Player player, int questId, double progress) {
		UUID uuid = player.getUniqueId();
		Map<Integer, Double> questProgress = playerQuestProgress.computeIfAbsent(uuid, k -> new HashMap<>());
		questProgress.put(questId, progress);

		// Auto-save progress periodically
		saveAllData();
	}

}
