package com.tutorial.listeners;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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
import org.bukkit.Statistic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestListener implements Listener {
	private final Tutorial plugin;
	private final Map<UUID, BossBar> playerBossBars;
	private final boolean debug;

	public QuestListener(Tutorial plugin) {
		this.plugin = plugin;
		this.playerBossBars = new HashMap<>();
		this.debug = plugin.getConfig().getBoolean("settings.debug", false);
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
				updateQuestProgress(player, currentQuest);
				if (currentQuest.getProgress() >= 1.0) {
					completeQuest(player, currentQuest);
				}
				updateBossBar(player);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null) {
			if (currentQuest.getType().equalsIgnoreCase("placeholder")) {
				updateQuestProgress(player, currentQuest);
				if (currentQuest.getProgress() >= 1.0) {
					completeQuest(player, currentQuest);
				}
				updateBossBar(player);
			} else if (currentQuest.getType().equalsIgnoreCase("region")) {
				RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
				RegionQuery query = container.createQuery();
				com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(event.getTo());
				
				// Check if player is in the specified region and world
				if (event.getTo().getWorld().getName().equals(currentQuest.getWorld()) &&
					query.getApplicableRegions(loc).getRegions().stream()
						.anyMatch(region -> region.getId().equalsIgnoreCase(currentQuest.getRegion()))) {
					completeQuest(player, currentQuest);
				}
			}
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

	private void updateQuestProgress(Player player, Quest quest) {
		switch (quest.getType().toLowerCase()) {
			case "kill":
				int killed = player.getStatistic(Statistic.KILL_ENTITY, EntityType.valueOf(quest.getMobType().toUpperCase()));
				quest.updateProgress(killed, quest.getAmount());
				if (debug) {
					plugin.getLogger().info(String.format("Progress for %s: %d/%d kills", 
						player.getName(), killed, quest.getAmount()));
				}
				break;
			case "placeholder":
				if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
					String value = PlaceholderAPI.setPlaceholders(player, quest.getPlaceholder());
					try {
						double current = Double.parseDouble(value);
						double target = Double.parseDouble(quest.getTargetValue());
						quest.updateProgress(current, target);
						if (debug) {
							plugin.getLogger().info(String.format("Progress for %s: %s/%s (%s)", 
								player.getName(), value, quest.getTargetValue(), quest.getPlaceholder()));
						}
					} catch (NumberFormatException e) {
						if (debug) {
							plugin.getLogger().warning("Failed to parse progress values: " + e.getMessage());
						}
					}
				}
				break;
		}
	}

	private void updateBossBar(Player player) {
		BossBar bossBar = playerBossBars.get(player.getUniqueId());
		if (bossBar == null) {
			bossBar = Bukkit.createBossBar(
				"Current Quest",
				BarColor.valueOf(plugin.getConfig().getString("settings.bossbar.color", "BLUE")),
				BarStyle.valueOf(plugin.getConfig().getString("settings.bossbar.style", "SOLID"))
			);
			playerBossBars.put(player.getUniqueId(), bossBar);
		}

		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null) {
			updateQuestProgress(player, currentQuest);
			bossBar.setTitle(currentQuest.getMessage());
			if (plugin.getConfig().getBoolean("settings.bossbar.show-progress", true)) {
				bossBar.setProgress(currentQuest.getProgress());
			}
			if (!bossBar.getPlayers().contains(player)) {
				bossBar.addPlayer(player);
			}
			if (debug) {
				plugin.getLogger().info(String.format("Updated bossbar for %s: Progress %.2f", 
					player.getName(), currentQuest.getProgress()));
			}
		} else {
			bossBar.removePlayer(player);
			playerBossBars.remove(player.getUniqueId());
		}
	}
}