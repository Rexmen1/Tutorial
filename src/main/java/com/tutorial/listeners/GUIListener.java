package com.tutorial.listeners;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
	private final Tutorial plugin;

	public GUIListener(Tutorial plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		String title = plugin.getConfig().getString("settings.gui.title", "Tutorial Quests");
		if (!ChatColor.translateAlternateColorCodes('&', title).equals(event.getView().getTitle())) {
			return;
		}

		event.setCancelled(true);
		
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
			return;
		}

		String questName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
		for (Quest quest : plugin.getQuestManager().getAllQuests()) {
			if (quest.getName().equals(questName)) {
				int currentQuest = plugin.getQuestManager().getCurrentQuest(player);
				if (quest.getId() > currentQuest) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("settings.messages.quest-locked", 
							"&cComplete previous quests first!")));
				} else if (quest.getId() < currentQuest) {
					player.sendMessage(ChatColor.GREEN + "You have already completed this quest!");
				} else {
					player.sendMessage(ChatColor.YELLOW + quest.getMessage());
				}
				break;
			}
		}
	}
}