package com.tutorial.managers;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
	private final Tutorial plugin;
	private final Map<Integer, Quest> quests;
	private final Map<UUID, Integer> playerProgress;
	private final File dataFile;
	private FileConfiguration questConfig;
	private FileConfiguration dataConfig;

	public QuestManager(Tutorial plugin) {
		this.plugin = plugin;
		this.quests = new HashMap<>();
		this.playerProgress = new HashMap<>();
		this.dataFile = new File(plugin.getDataFolder(), "data.yml");
		
		loadQuests();
		loadPlayerData();
	}

	public void loadQuests() {
		File questFile = new File(plugin.getDataFolder(), "quests.yml");
		questConfig = YamlConfiguration.loadConfiguration(questFile);

		for (String key : questConfig.getConfigurationSection("quests").getKeys(false)) {
			int questId = Integer.parseInt(key);
			String path = "quests." + key + ".";
			
			Quest quest = new Quest(
				questId,
				questConfig.getString(path + "name"),
				questConfig.getString(path + "type"),
				questConfig.getString(path + "message"),
				questConfig.getString(path + "gui-icon"),
				questConfig.getStringList(path + "reward")
			);
			
			// Set specific quest properties based on type
			switch (quest.getType().toLowerCase()) {
				case "command":
					quest.setCommand(questConfig.getString(path + "command"));
					break;
				case "region":
					quest.setRegion(questConfig.getString(path + "region"));
					quest.setWorld(questConfig.getString(path + "world"));
					break;
				case "kill":
					quest.setMobType(questConfig.getString(path + "mob"));
					quest.setAmount(questConfig.getInt(path + "amount"));
					break;
				case "placeholder":
					quest.setPlaceholder(questConfig.getString(path + "placeholder"));
					quest.setTargetValue(questConfig.getString(path + "target-value"));
					break;
			}
			
			quests.put(questId, quest);
		}
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
			}
		}
	}

	public void saveAllData() {
		try {
			for (Map.Entry<UUID, Integer> entry : playerProgress.entrySet()) {
				dataConfig.set("players." + entry.getKey().toString() + ".current-quest", entry.getValue());
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
}
