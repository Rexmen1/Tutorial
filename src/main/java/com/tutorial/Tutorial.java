package com.tutorial;

import com.tutorial.commands.QuestCommand;
import com.tutorial.listeners.QuestListener;
import com.tutorial.managers.QuestManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Tutorial extends JavaPlugin {
	private static Tutorial instance;
	private QuestManager questManager;
	private QuestListener questListener;

	@Override
	public void onEnable() {
		instance = this;
		
		// Check for WorldGuard
		if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
			getLogger().warning("WorldGuard not found! Region quests will not work properly.");
		}
		
		// Only save default configs if they don't exist
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		
		File configFile = new File(getDataFolder(), "config.yml");
		File questsFile = new File(getDataFolder(), "quests.yml");
		
		if (!configFile.exists()) {
			saveResource("config.yml", false);
		}
		if (!questsFile.exists()) {
			saveResource("quests.yml", false);
		}
		
		// Load configs
		reloadConfig();
		
		// Initialize managers
		this.questManager = new QuestManager(this);
		
		// Register commands
		getCommand("quest").setExecutor(new QuestCommand(this));
		
		// Register listeners
		this.questListener = new QuestListener(this);
		getServer().getPluginManager().registerEvents(questListener, this);
		
		getLogger().info("Tutorial plugin has been enabled!");
	}

	@Override
	public void onDisable() {
		// Clean up boss bars
		if (questListener != null) {
			questListener.cleanupBossBars();
		}
		
		// Save any pending data
		if (questManager != null) {
			questManager.saveAllData();
		}
		
		getLogger().info("Tutorial plugin has been disabled!");
	}

	public static Tutorial getInstance() {
		return instance;
	}

	public QuestManager getQuestManager() {
		return questManager;
	}

	public QuestListener getQuestListener() {
		return questListener;
	}

	// Update reloadAllConfigs method to properly reload both configs
	public void reloadAllConfigs() {
		// Reload main config
		reloadConfig();
		
		// Reload quests.yml
		File questFile = new File(getDataFolder(), "quests.yml");
		if (questFile.exists()) {
			questManager.reloadQuests();
		} else {
			getLogger().warning("quests.yml not found! Using default configuration.");
			saveResource("quests.yml", false);
			questManager.reloadQuests();
		}
	}
}