package com.tutorial.models;

import java.util.List;

public class Quest {
	private final int id;
	private final String name;
	private final String type;
	private final String message;
	private final String bossbar; // New field
	private final List<String> description; // New field
	private final String guiIcon;
	private final List<String> rewards;

	// Quest type specific fields
	private String command;
	private String region;
	private String world;
	private String mobType;
	private int amount;
	private String placeholder;
	private String targetValue;
	private String blockType; // For break, place, craft, eat quests
	private String chatMessage; // For chat quests
	private double progress; // Field for progress tracking

	public Quest(int id, String name, String type, String message, String bossbar, List<String> description,
			String guiIcon, List<String> rewards) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.message = message;
		this.bossbar = bossbar;
		this.description = description;
		this.guiIcon = guiIcon;
		this.rewards = rewards;
		this.progress = 0.0;
	}

	// Add getters/setters for progress
	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = Math.min(1.0, Math.max(0.0, progress));
	}

	// Calculate progress based on current and target values
	public void updateProgress(double current, double target) {
		setProgress(current / target);
	}

	// Update progress with player context for persistence
	public void updateProgress(double current, double target, org.bukkit.entity.Player player,
			com.tutorial.Tutorial plugin) {
		double newProgress = current / target;
		setProgress(newProgress);
		// Save progress to persistent storage
		plugin.getQuestManager().setQuestProgress(player, this.id, newProgress);
	}

	// Existing getters/setters
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String getGuiIcon() {
		return guiIcon;
	}

	public List<String> getRewards() {
		return rewards;
	}

	public String getBossbar() {
		return bossbar;
	}

	public List<String> getDescription() {
		return description;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public String getMobType() {
		return mobType;
	}

	public void setMobType(String mobType) {
		this.mobType = mobType;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public String getBlockType() {
		return blockType;
	}

	public void setBlockType(String blockType) {
		this.blockType = blockType;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}
}