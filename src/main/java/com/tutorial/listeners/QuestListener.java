package com.tutorial.listeners;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestListener implements Listener {
	private final Tutorial plugin;
	private final Map<UUID, BossBar> playerBossBars;

	public QuestListener(Tutorial plugin) {
		this.plugin = plugin;
		this.playerBossBars = new HashMap<>();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		updateBossBar(player);
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("command")) {
			String command = event.getMessage().toLowerCase();
			if (command.startsWith(currentQuest.getCommand().toLowerCase())) {
				completeQuest(player, currentQuest);
			}
		}
	}

	@EventHandler
	public void onEntityKill(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null) return;
		
		Player player = event.getEntity().getKiller();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("kill")) {
			if (event.getEntityType() == EntityType.valueOf(currentQuest.getMobType().toUpperCase())) {
				// Implement kill tracking logic here
				completeQuest(player, currentQuest);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("region")) {
			// Implement WorldGuard region check here
			// This is a placeholder for WorldGuard integration
			// You would need to check if the player is in the specified region
			// completeQuest(player, currentQuest);
		}
	}

	private void completeQuest(Player player, Quest quest) {
		// Give rewards
		for (String reward : quest.getRewards()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
				reward.replace("%player%", player.getName()));
		}

		plugin.getQuestManager().completeQuest(player, quest.getId());
		updateBossBar(player);
	}

	private void updateBossBar(Player player) {
		BossBar bossBar = playerBossBars.get(player.getUniqueId());
		if (bossBar == null) {
			bossBar = Bukkit.createBossBar(
				"Current Quest",
				BarColor.valueOf(plugin.getConfig().getString("settings.bossbar.color", "BLUE")),
				BarStyle.SOLID
			);
			playerBossBars.put(player.getUniqueId(), bossBar);
		}

		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null) {
			bossBar.setTitle(currentQuest.getMessage());
			if (!bossBar.getPlayers().contains(player)) {
				bossBar.addPlayer(player);
			}
		} else {
			bossBar.removePlayer(player);
			playerBossBars.remove(player.getUniqueId());
		}
	}
}