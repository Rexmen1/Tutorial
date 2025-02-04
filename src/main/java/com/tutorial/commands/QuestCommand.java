package com.tutorial.commands;

import com.tutorial.Tutorial;
import com.tutorial.gui.QuestGUI;
import com.tutorial.listeners.QuestListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {
	private final Tutorial plugin;

	public QuestCommand(Tutorial plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			// Open quest GUI
			new QuestGUI(plugin).openInventory(player);
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (!player.hasPermission("tutorial.admin")) {
				player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
				return true;
			}

			// Clean up boss bars before reload
			QuestListener questListener = plugin.getQuestListener();
			if (questListener != null) {
				questListener.cleanupBossBars();
			}

			// Reload all configurations
			plugin.reloadAllConfigs();

			// Refresh boss bars with new config
			if (questListener != null) {
				questListener.refreshAllBossBars();
			}

			player.sendMessage(ChatColor.GREEN + "Configuration and quests have been reloaded!");
			if (plugin.getConfig().getBoolean("settings.debug", false)) {
				player.sendMessage(ChatColor.GRAY + "Debug mode is " + 
					(plugin.getConfig().getBoolean("settings.debug", false) ? "enabled" : "disabled"));
			}
			return true;
		}

		return false;
	}
}