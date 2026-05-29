package com.itmeansbigmountain.bosskcrivallookup;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.Skill;

final class BossKcRivalLookupFormatter
{
	private BossKcRivalLookupFormatter()
	{
	}

	static Optional<HiscoreSkill> findBossSkill(String bossName)
	{
		String requested = normalize(bossName);
		if (requested.isEmpty())
		{
			return Optional.empty();
		}

		return bossSkills()
			.filter(skill -> normalize(skill.getName()).equals(requested)
				|| normalize(skill.name()).equals(requested))
			.findFirst()
			.or(() -> bossSkills()
				.filter(skill -> normalize(skill.getName()).contains(requested))
				.findFirst());
	}

	static Stream<HiscoreSkill> bossSkills()
	{
		return Arrays.stream(HiscoreSkill.values())
			.filter(skill -> "BOSS".equals(skill.getType().name()));
	}

	static int killCount(Skill skill)
	{
		if (skill == null || skill.getRank() <= 0)
		{
			return 0;
		}

		return Math.max(0, skill.getLevel());
	}

	static String formatComparison(String localPlayer, String rivalPlayer, String bossName, int localKills, int rivalKills)
	{
		int diff = localKills - rivalKills;
		String leader;
		if (diff > 0)
		{
			leader = localPlayer + " leads by " + diff;
		}
		else if (diff < 0)
		{
			leader = rivalPlayer + " leads by " + Math.abs(diff);
		}
		else
		{
			leader = "tied";
		}

		return String.format(Locale.ROOT, "%s KC: %s=%d, %s=%d (%s)", bossName, localPlayer, localKills, rivalPlayer, rivalKills, leader);
	}

	static LookupRequest parseCommand(String message, String defaultRival, String defaultBoss)
	{
		String body = message == null ? "" : message.trim();
		if (body.toLowerCase(Locale.ROOT).startsWith("!bosskc"))
		{
			body = body.substring("!bosskc".length()).trim();
		}

		if (body.isEmpty())
		{
			return new LookupRequest(defaultRival, defaultBoss);
		}

		String[] pieces = body.split("\\|", 2);
		if (pieces.length == 2)
		{
			String rival = pieces[0].trim().isEmpty() ? defaultRival : pieces[0].trim();
			String boss = pieces[1].trim().isEmpty() ? defaultBoss : pieces[1].trim();
			return new LookupRequest(rival, boss);
		}

		return new LookupRequest(defaultRival, body);
	}

	private static String normalize(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	static final class LookupRequest
	{
		private final String rival;
		private final String boss;

		LookupRequest(String rival, String boss)
		{
			this.rival = rival;
			this.boss = boss;
		}

		String getRival()
		{
			return rival;
		}

		String getBoss()
		{
			return boss;
		}
	}
}
