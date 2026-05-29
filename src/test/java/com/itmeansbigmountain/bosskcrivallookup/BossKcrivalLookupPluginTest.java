package com.itmeansbigmountain.bosskcrivallookup;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BossKcrivalLookupPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BossKcrivalLookupPlugin.class);
		RuneLite.main(args);
	}
}