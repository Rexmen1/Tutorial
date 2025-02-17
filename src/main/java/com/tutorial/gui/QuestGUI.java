package com.tutorial.gui;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
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
			plugin.getConfig().getInt("settings.gui.size", 27),
			ChatColor.translateAlternateColorCodes('&', 
				plugin.getConfig().getString("settings.gui.title", "Tutorial Quests")));
		
		setupInventory();
	}

	private void setupInventory() {
		int currentSlot = 0;
		for (Quest quest : plugin.getQuestManager().getAllQuests()) {
			inventory.setItem(currentSlot++, createQuestItem(quest));
		}
	}

	private ItemStack createQuestItem(Quest quest) {
		Material material = Material.valueOf(quest.getGuiIcon().toUpperCase());
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		
		// Set name using config format
		String nameFormat = plugin.getConfig().getString("settings.gui.item_name_format", "&6%quest_name%");
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
			nameFormat.replace("%quest_name%", quest.getName())));
		
		List<String> lore = new ArrayList<>();
		List<String> loreFormat = plugin.getConfig().getStringList("settings.gui.item_lore_format");
		
		for (String line : loreFormat) {
			String formattedLine = line
				.replace("%quest_type%", quest.getType())
				.replace("%quest_message%", quest.getMessage());
				
			// Handle rewards separately
			if (line.contains("%quest_rewards%")) {
				for (String reward : quest.getRewards()) {
					lore.add(ChatColor.translateAlternateColorCodes('&', 
						line.replace("%quest_rewards%", "• " + reward)));
				}
				continue;
			}
			
			// Add quest-specific information
			if (quest.getType().equalsIgnoreCase("command")) {
				formattedLine = formattedLine.replace("%quest_command%", quest.getCommand());
			} else if (quest.getType().equalsIgnoreCase("region")) {
				formattedLine = formattedLine.replace("%quest_region%", quest.getRegion());
			} else if (quest.getType().equalsIgnoreCase("kill")) {
				formattedLine = formattedLine
					.replace("%quest_amount%", String.valueOf(quest.getAmount()))
					.replace("%quest_mob%", quest.getMobType());
			} else if (quest.getType().equalsIgnoreCase("placeholder")) {
				formattedLine = formattedLine
					.replace("%quest_placeholder%", quest.getPlaceholder())
					.replace("%quest_target%", quest.getTargetValue());
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