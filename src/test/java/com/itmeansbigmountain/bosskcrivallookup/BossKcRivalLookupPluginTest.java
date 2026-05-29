package com.itmeansbigmountain.bosskcrivallookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

public class BossKcRivalLookupPluginTest
{
	@Test
	public void resolvesBossNamesLoosely()
	{
		assertTrue(BossKcRivalLookupFormatter.findBossSkill("vorkath").isPresent());
		assertTrue(BossKcRivalLookupFormatter.findBossSkill("challenge mode").isPresent());
		assertTrue(BossKcRivalLookupFormatter.findBossSkill("K'ril").isPresent());
	}

	@Test
	public void parsesDefaultCommand()
	{
		BossKcRivalLookupFormatter.LookupRequest request = BossKcRivalLookupFormatter.parseCommand("!bosskc", "Oyama", "Vorkath");

		assertEquals("Oyama", request.getRival());
		assertEquals("Vorkath", request.getBoss());
	}

	@Test
	public void parsesRivalAndBossCommand()
	{
		BossKcRivalLookupFormatter.LookupRequest request = BossKcRivalLookupFormatter.parseCommand(
			"!bosskc zezima | Theatre of Blood: Hard Mode",
			"Oyama",
			"Vorkath"
		);

		assertEquals("zezima", request.getRival());
		assertEquals("Theatre of Blood: Hard Mode", request.getBoss());
	}

	@Test
	public void formatsComparisonLeader()
	{
		String message = BossKcRivalLookupFormatter.formatComparison("Oyama", "Rival", "Vorkath", 125, 100);

		assertEquals("Vorkath KC: Oyama=125, Rival=100 (Oyama leads by 25)", message);
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BossKcRivalLookupPlugin.class);
		RuneLite.main(args);
	}
}
