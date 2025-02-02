package com.tutorial.utils;

import com.tutorial.Tutorial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestUtils {
	private static final Tutorial plugin = Tutorial.getInstance();

	public static void executeRewards(Player player, List<String> rewards) {
		String prefix = plugin.getConfig().getString("settings.messages.prefix", "&8[&bTutorial&8] &7");
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
			prefix + plugin.getConfig().getString("settings.messages.quest-complete", "&aQuest completed!")));
		
		for (String reward : rewards) {
			String command = reward.replace("%player%", player.getName());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
				prefix + "&7- &f" + formatReward(reward)));
		}
	}

	private static String formatReward(String reward) {
		if (reward.startsWith("money")) {
			return reward.replace("money ", "") + " coins";
		} else if (reward.startsWith("give")) {
			String[] parts = reward.split(" ");
			return parts[3] + "x " + formatItemName(parts[2]);
		}
		return reward;
	}

	private static String formatItemName(String item) {
		return item.toLowerCase()
			.replace("_", " ");
	}

	public static String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}