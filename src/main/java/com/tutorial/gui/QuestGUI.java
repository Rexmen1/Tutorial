package com.tutorial.gui;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import com.tutorial.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QuestGUI {
	private final Tutorial plugin;
	private final Inventory inventory;

	public QuestGUI(Tutorial plugin) {
		this.plugin = plugin;
		this.inventory = Bukkit.createInventory(null,
				plugin.getConfig().getInt("settings.gui.size", QuestUtils.DEFAULT_GUI_SIZE),
				ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("settings.gui.title", QuestUtils.DEFAULT_QUEST_TITLE)));

		setupInventory();
	}

	private void setupInventory() {
		int currentSlot = 0;
		for (Quest quest : plugin.getQuestManager().getAllQuests()) {
			inventory.setItem(currentSlot++, createQuestItem(quest));
		}
	}

	private ItemStack createQuestItem(Quest quest) {
		Material material;
		try {
			material = Material.valueOf(quest.getGuiIcon().toUpperCase());
		} catch (IllegalArgumentException e) {
			plugin.getLogger().warning("Invalid material for quest " + quest.getId() + ": " + quest.getGuiIcon()
					+ ". Using PAPER as fallback.");
			material = Material.PAPER;
		}

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		// Set name using config format
		String nameFormat = plugin.getConfig().getString("settings.gui.item_name_format",
				QuestUtils.DEFAULT_ITEM_FORMAT);
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				nameFormat.replace("%quest_name%", quest.getName())));

		List<String> lore = new ArrayList<>();
		List<String> loreFormat = plugin.getConfig().getStringList("settings.gui.item_lore_format");

		for (String line : loreFormat) {
			String formattedLine = line
					.replace("%quest_type%", quest.getType())
					.replace("%quest_message%", String.join("\n", quest.getDescription()));

			// Handle rewards separately
			if (line.contains("%quest_rewards%")) {
				for (String reward : quest.getRewards()) {
					lore.add(ChatColor.translateAlternateColorCodes('&',
							line.replace("%quest_rewards%", "• " + reward)));
				}
				continue;
			} // Add quest-specific information
			if (quest.getType().equalsIgnoreCase("command")) {
				formattedLine = formattedLine.replace("%quest_command%",
						quest.getCommand() != null ? quest.getCommand() : "N/A");
			} else if (quest.getType().equalsIgnoreCase("region")) {
				formattedLine = formattedLine.replace("%quest_region%",
						quest.getRegion() != null ? quest.getRegion() : "N/A");
			} else if (quest.getType().equalsIgnoreCase("kill")) {
				formattedLine = formattedLine
						.replace("%quest_amount%", String.valueOf(quest.getAmount()))
						.replace("%quest_mob%", quest.getMobType() != null ? quest.getMobType() : "N/A");
			} else if (quest.getType().equalsIgnoreCase("placeholder")) {
				formattedLine = formattedLine
						.replace("%quest_placeholder%", quest.getPlaceholder() != null ? quest.getPlaceholder() : "N/A")
						.replace("%quest_target%", quest.getTargetValue() != null ? quest.getTargetValue() : "N/A");
			} else if (quest.getType().equalsIgnoreCase("break") ||
					quest.getType().equalsIgnoreCase("place") ||
					quest.getType().equalsIgnoreCase("craft") ||
					quest.getType().equalsIgnoreCase("eat")) {
				formattedLine = formattedLine
						.replace("%quest_amount%", String.valueOf(quest.getAmount()))
						.replace("%quest_block%", quest.getBlockType() != null ? quest.getBlockType() : "N/A");
			} else if (quest.getType().equalsIgnoreCase("chat")) {
				formattedLine = formattedLine
						.replace("%quest_message%", quest.getChatMessage() != null ? quest.getChatMessage() : "N/A");
			}

			lore.add(ChatColor.translateAlternateColorCodes('&', formattedLine));
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public void openInventory(Player player) {
		player.openInventory(inventory);
	}
}