package com.tutorial.listeners;

import com.tutorial.Tutorial;
import com.tutorial.models.Quest;
import com.tutorial.utils.QuestUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Material;

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
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		BossBar bossBar = playerBossBars.remove(uuid);
		if (bossBar != null) {
			bossBar.removeAll();
			if (debug) {
				plugin.getLogger().info("Cleaned up boss bar for player: " + player.getName());
			}
		}
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("command")) {
			String command = event.getMessage().toLowerCase();
			if (command.startsWith(currentQuest.getCommand().toLowerCase())) {
				// Get current progress and increment by 1
				double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
				int newProgress = (int) currentProgress + 1;
				plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

				if (debug) {
					plugin.getLogger().info(String.format("Player %s executed command %s. Progress: %d/%d",
							player.getName(), currentQuest.getCommand(), newProgress, currentQuest.getAmount()));
				}

				if (newProgress >= currentQuest.getAmount()) {
					completeQuest(player, currentQuest);
				}
				updateBossBar(player);
			}
		}
	}

	@EventHandler
	public void onEntityKill(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null)
			return;

		Player player = event.getEntity().getKiller();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("kill")) {
			try {
				EntityType targetType = EntityType.valueOf(currentQuest.getMobType().toUpperCase());
				if (event.getEntityType() == targetType) {
					// Get current progress and increment by 1
					double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
					int newProgress = (int) currentProgress + 1;
					plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

					if (debug) {
						plugin.getLogger().info(String.format("Player %s killed %s. Progress: %d/%d",
								player.getName(), targetType.name(), newProgress, currentQuest.getAmount()));
					}

					if (newProgress >= currentQuest.getAmount()) {
						completeQuest(player, currentQuest);
					}
					updateBossBar(player);
				}
			} catch (IllegalArgumentException e) {
				if (debug) {
					plugin.getLogger().warning(
							"Invalid entity type for quest " + currentQuest.getId() + ": " + currentQuest.getMobType());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock()))
			return;
		// Optimize: Only check movement every few blocks for performance
		if (event.getFrom().distance(event.getTo()) < QuestUtils.MOVEMENT_THRESHOLD)
			return;

		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);

		if (currentQuest != null) {
			if (currentQuest.getType().equalsIgnoreCase("placeholder")) {
				// Placeholder quests are updated dynamically in the boss bar, no need to store
				// progress
				updateBossBar(player);
			} else if (currentQuest.getType().equalsIgnoreCase("region")) {
				try {
					RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
					RegionQuery query = container.createQuery();
					com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(event.getTo());

					// Check if player is in the specified region and world
					if (event.getTo().getWorld().getName().equals(currentQuest.getWorld()) &&
							query.getApplicableRegions(loc).getRegions().stream()
									.anyMatch(region -> region.getId().equalsIgnoreCase(currentQuest.getRegion()))) {
						completeQuest(player, currentQuest);
					}
				} catch (Exception e) {
					if (debug) {
						plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
					}
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

	private void updateBossBar(Player player) {
		BossBar bossBar = playerBossBars.get(player.getUniqueId());
		if (bossBar == null) {
			bossBar = Bukkit.createBossBar(
					"Current Quest",
					BarColor.valueOf(plugin.getConfig().getString("settings.bossbar.color", "BLUE")),
					BarStyle.valueOf(plugin.getConfig().getString("settings.bossbar.style", "SOLID")));
			playerBossBars.put(player.getUniqueId(), bossBar);
		}
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null) {
			String bossbarMessage = ChatColor.translateAlternateColorCodes('&', currentQuest.getBossbar());

			// Replace progress placeholders in boss bar
			if (bossbarMessage.contains("%progress%")) {
				// Get the stored progress for this quest
				double storedProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
				int currentProgress = (int) storedProgress;

				String progressText = "";
				if (currentQuest.getType().equalsIgnoreCase("kill") ||
						currentQuest.getType().equalsIgnoreCase("break") ||
						currentQuest.getType().equalsIgnoreCase("place") ||
						currentQuest.getType().equalsIgnoreCase("craft") ||
						currentQuest.getType().equalsIgnoreCase("eat") ||
						currentQuest.getType().equalsIgnoreCase("command")) {
					progressText = currentProgress + "/" + currentQuest.getAmount();
				} else if (currentQuest.getType().equalsIgnoreCase("chat")) {
					progressText = currentProgress + "/" + currentQuest.getAmount();
				} else if (currentQuest.getType().equalsIgnoreCase("placeholder")) {
					if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
						String current = PlaceholderAPI.setPlaceholders(player, currentQuest.getPlaceholder());
						progressText = current + "/" + currentQuest.getTargetValue();
					}
				}
				bossbarMessage = bossbarMessage.replace("%progress%", progressText);
			}

			bossBar.setTitle(bossbarMessage);

			// Calculate progress percentage for the boss bar
			double progressPercentage = 0.0;
			if (currentQuest.getType().equalsIgnoreCase("placeholder")) {
				if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
					try {
						String current = PlaceholderAPI.setPlaceholders(player, currentQuest.getPlaceholder());
						String target = currentQuest.getTargetValue();
						double currentValue = Double.parseDouble(current);
						double targetValue = Double.parseDouble(target);
						progressPercentage = Math.max(0.0, Math.min(1.0, currentValue / targetValue));
					} catch (NumberFormatException e) {
						progressPercentage = 0.0;
					}
				}
			} else {
				double storedProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
				progressPercentage = Math.max(0.0, Math.min(1.0, storedProgress / currentQuest.getAmount()));
			}

			if (plugin.getConfig().getBoolean("settings.bossbar.show-progress", true)) {
				bossBar.setProgress(progressPercentage);
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

	public void refreshAllBossBars() {
		// Clean up existing boss bars
		for (BossBar bar : playerBossBars.values()) {
			bar.removeAll();
		}
		playerBossBars.clear();

		// Refresh for all online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			updateBossBar(player);
		}

		if (debug) {
			plugin.getLogger().info("Refreshed all boss bars");
		}
	}

	public void cleanupBossBars() {
		for (BossBar bar : playerBossBars.values()) {
			bar.removeAll();
		}
		playerBossBars.clear();
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("break")) {
			try {
				Material targetMaterial = Material.valueOf(currentQuest.getBlockType().toUpperCase());
				if (event.getBlock().getType() == targetMaterial) { // Get current progress and increment by 1
					double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
					int newProgress = (int) currentProgress + 1;
					plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

					if (debug) {
						plugin.getLogger().info(String.format("Player %s broke %s. Progress: %d/%d",
								player.getName(), targetMaterial.name(), newProgress, currentQuest.getAmount()));
					}

					if (newProgress >= currentQuest.getAmount()) {
						completeQuest(player, currentQuest);
					}
					updateBossBar(player);
				}
			} catch (IllegalArgumentException e) {
				if (debug) {
					plugin.getLogger().warning("Invalid material type for break quest " + currentQuest.getId() + ": "
							+ currentQuest.getBlockType());
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("place")) {
			try {
				Material targetMaterial = Material.valueOf(currentQuest.getBlockType().toUpperCase());
				if (event.getBlock().getType() == targetMaterial) {
					// Get current progress and increment by 1
					double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
					int newProgress = (int) currentProgress + 1;
					plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

					if (debug) {
						plugin.getLogger().info(String.format("Player %s placed %s. Progress: %d/%d",
								player.getName(), targetMaterial.name(), newProgress, currentQuest.getAmount()));
					}

					if (newProgress >= currentQuest.getAmount()) {
						completeQuest(player, currentQuest);
					}
					updateBossBar(player);
				}
			} catch (IllegalArgumentException e) {
				if (debug) {
					plugin.getLogger().warning("Invalid material type for place quest " + currentQuest.getId() + ": "
							+ currentQuest.getBlockType());
				}
			}
		}
	}

	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("craft")) {
			try {
				Material targetMaterial = Material.valueOf(currentQuest.getBlockType().toUpperCase());
				if (event.getRecipe().getResult().getType() == targetMaterial) {
					// Calculate how many items are being crafted
					int craftedAmount = event.getRecipe().getResult().getAmount();

					// For shift-click, we need to calculate the max possible crafts
					if (event.isShiftClick()) {
						// Get the maximum number of items that can be crafted
						// This is a simplified calculation - in a real implementation you'd check
						// inventory space
						craftedAmount = event.getRecipe().getResult().getAmount();
					}

					// Get current progress and add the crafted amount
					double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
					int newProgress = (int) currentProgress + craftedAmount;
					plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

					if (debug) {
						plugin.getLogger().info(String.format("Player %s crafted %d %s. Progress: %d/%d",
								player.getName(), craftedAmount, targetMaterial.name(), newProgress,
								currentQuest.getAmount()));
					}

					if (newProgress >= currentQuest.getAmount()) {
						completeQuest(player, currentQuest);
					}
					updateBossBar(player);
				}
			} catch (IllegalArgumentException e) {
				if (debug) {
					plugin.getLogger().warning("Invalid material type for craft quest " + currentQuest.getId() + ": "
							+ currentQuest.getBlockType());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("eat")) {
			try {
				Material targetMaterial = Material.valueOf(currentQuest.getBlockType().toUpperCase());
				if (event.getItem().getType() == targetMaterial) {
					// Get current progress and increment by 1
					double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
					int newProgress = (int) currentProgress + 1;
					plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

					if (debug) {
						plugin.getLogger().info(String.format("Player %s consumed %s. Progress: %d/%d",
								player.getName(), targetMaterial.name(), newProgress, currentQuest.getAmount()));
					}

					if (newProgress >= currentQuest.getAmount()) {
						completeQuest(player, currentQuest);
					}
					updateBossBar(player);
				}
			} catch (IllegalArgumentException e) {
				if (debug) {
					plugin.getLogger().warning("Invalid material type for eat quest " + currentQuest.getId() + ": "
							+ currentQuest.getBlockType());
				}
			}
		}
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		int currentQuestId = plugin.getQuestManager().getCurrentQuest(player);
		Quest currentQuest = plugin.getQuestManager().getQuest(currentQuestId);
		if (currentQuest != null && currentQuest.getType().equalsIgnoreCase("chat")) {
			String message = event.getMessage();
			if (message.equalsIgnoreCase(currentQuest.getChatMessage()) ||
					message.toLowerCase().contains(currentQuest.getChatMessage().toLowerCase())) {
				// Get current progress and increment by 1
				double currentProgress = plugin.getQuestManager().getQuestProgress(player, currentQuest.getId());
				int newProgress = (int) currentProgress + 1;
				plugin.getQuestManager().setQuestProgress(player, currentQuest.getId(), newProgress);

				if (debug) {
					plugin.getLogger().info(String.format("Player %s sent message %s. Progress: %d/%d",
							player.getName(), currentQuest.getChatMessage(), newProgress, currentQuest.getAmount()));
				}

				if (newProgress >= currentQuest.getAmount()) {
					completeQuest(player, currentQuest);
				}
				updateBossBar(player);
			}
		}
	}

	public void onDisable() {
		cleanupBossBars();
	}
}