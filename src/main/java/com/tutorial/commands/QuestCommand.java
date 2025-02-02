package com.tutorial.commands;

import com.tutorial.Tutorial;
import com.tutorial.gui.QuestGUI;
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

			plugin.reloadConfig();
			plugin.getQuestManager().loadQuests();
			player.sendMessage(ChatColor.GREEN + "Tutorial configuration reloaded!");
			return true;
		}

		return false;
	}
}