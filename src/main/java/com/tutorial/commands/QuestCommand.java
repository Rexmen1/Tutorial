package com.tutorial.commands;

import com.tutorial.Tutorial;
import com.tutorial.gui.QuestGUI;
import com.tutorial.listeners.QuestListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestCommand implements CommandExecutor, TabCompleter {
	private final Tutorial plugin;

	public QuestCommand(Tutorial plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("settings.messages.player-only",
							"&cThis command can only be used by players!")));
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
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("settings.messages.no-permission",
								"&cYou don't have permission to use this command!")));
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

			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("settings.messages.reload-success",
							"&aConfiguration and quests have been reloaded!")));
			if (plugin.getConfig().getBoolean("settings.debug", false)) {
				String debugStatus = plugin.getConfig().getBoolean("settings.debug", false) ? "enabled" : "disabled";
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("settings.messages.debug-status", "&7Debug mode is %status%")
								.replace("%status%", debugStatus)));
			}
			return true;
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			if (sender.hasPermission("tutorial.admin")) {
				completions.add("reload");
			}
		}

		return completions;
	}
}