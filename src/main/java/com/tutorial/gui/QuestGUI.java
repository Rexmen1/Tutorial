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
		
		meta.setDisplayName(ChatColor.GOLD + quest.getName());
		
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "Type: " + ChatColor.YELLOW + quest.getType());
		lore.add(ChatColor.GRAY + "Description: " + ChatColor.WHITE + quest.getMessage());
		
		switch (quest.getType().toLowerCase()) {
			case "command":
				lore.add(ChatColor.GRAY + "Command: " + ChatColor.YELLOW + quest.getCommand());
				break;
			case "region":
				lore.add(ChatColor.GRAY + "Region: " + ChatColor.YELLOW + quest.getRegion());
				break;
			case "kill":
				lore.add(ChatColor.GRAY + "Target: " + ChatColor.YELLOW + quest.getAmount() + "x " + quest.getMobType());
				break;
			case "placeholder":
				lore.add(ChatColor.GRAY + "Goal: " + ChatColor.YELLOW + quest.getPlaceholder() + " = " + quest.getTargetValue());
				break;
		}
		
		lore.add("");
		lore.add(ChatColor.GRAY + "Rewards:");
		for (String reward : quest.getRewards()) {
			lore.add(ChatColor.YELLOW + "• " + reward);
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public void openInventory(Player player) {
		player.openInventory(inventory);
	}
}