package com.itmeansbigmountain.bosskcrivallookup;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bosskcrivallookup")
public interface BossKcRivalLookupConfig extends Config
{
	@ConfigItem(
		keyName = "rivalName",
		name = "Rival name",
		description = "Default OSRS account to compare against when using !bosskc"
	)
	default String rivalName()
	{
		return "Oyama";
	}

	@ConfigItem(
		keyName = "bossName",
		name = "Boss name",
		description = "Default boss or raid hiscore entry to compare"
	)
	default String bossName()
	{
		return "Vorkath";
	}

	@ConfigItem(
		keyName = "showLoginHint",
		name = "Show login hint",
		description = "Show a short chat reminder after logging in"
	)
	default boolean showLoginHint()
	{
		return true;
	}
}
