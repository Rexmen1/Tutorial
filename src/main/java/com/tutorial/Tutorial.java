package com.tutorial;

import com.tutorial.commands.QuestCommand;
import com.tutorial.listeners.QuestListener;
import com.tutorial.managers.QuestManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Tutorial extends JavaPlugin {
	private static Tutorial instance;
	private QuestManager questManager;

	@Override
	public void onEnable() {
		instance = this;
		
		// Check for WorldGuard
		if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
			getLogger().warning("WorldGuard not found! Region quests will not work properly.");
		}
		
		// Save default configs
		saveDefaultConfig();
		saveResource("quests.yml", false);
		
		// Initialize managers
		this.questManager = new QuestManager(this);
		
		// Register commands
		getCommand("quest").setExecutor(new QuestCommand(this));
		
		// Register listeners
		getServer().getPluginManager().registerEvents(new QuestListener(this), this);
		
		getLogger().info("Tutorial plugin has been enabled!");
	}

	@Override
	public void onDisable() {
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
}