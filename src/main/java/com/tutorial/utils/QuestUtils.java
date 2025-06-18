package com.tutorial.utils;

import com.tutorial.Tutorial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestUtils {
	// Constants to replace magic numbers/strings
	public static final int DEFAULT_GUI_SIZE = 27;
	public static final double PROGRESS_COMPLETE = 1.0;
	public static final double PROGRESS_EMPTY = 0.0;
	public static final double MOVEMENT_THRESHOLD = 2.0;
	public static final String DEFAULT_QUEST_TITLE = "Tutorial Quests";
	public static final String DEFAULT_ITEM_FORMAT = "&6%quest_name%";
	public static final String DEFAULT_PREFIX = "&8[&bTutorial&8] &7";

	private static final Tutorial plugin = Tutorial.getInstance();

	public static void executeRewards(Player player, List<String> rewards) {
		String prefix = plugin.getConfig().getString("settings.messages.prefix", DEFAULT_PREFIX);
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
			if (parts.length >= 4) {
				return parts[3] + "x " + formatItemName(parts[2]);
			}
		}
		return reward;
	}

	private static String formatItemName(String item) {
		return item.toLowerCase().replace("_", " ");
	}

	public static String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static boolean isValidProgress(double progress) {
		return progress >= PROGRESS_EMPTY && progress <= PROGRESS_COMPLETE;
	}

	public static String formatProgress(double current, double total) {
		return String.format("%.1f/%.1f", current, total);
	}
}