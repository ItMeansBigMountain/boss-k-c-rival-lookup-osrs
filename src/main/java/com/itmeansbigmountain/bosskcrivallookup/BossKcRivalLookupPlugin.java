package com.itmeansbigmountain.bosskcrivallookup;

import com.google.inject.Provides;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Boss KC Rival Lookup",
	description = "Compare your boss kill count against a configured rival from OSRS hiscores.",
	tags = {"boss", "kc", "hiscores", "rival"}
)
public class BossKcRivalLookupPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private HiscoreClient hiscoreClient;

	@Inject
	private BossKcRivalLookupConfig config;

	private ExecutorService lookupExecutor;

	@Override
	protected void startUp()
	{
		lookupExecutor = Executors.newSingleThreadExecutor(r ->
		{
			Thread thread = new Thread(r, "boss-kc-rival-lookup");
			thread.setDaemon(true);
			return thread;
		});
		log.debug("Boss KC Rival Lookup started");
	}

	@Override
	protected void shutDown()
	{
		if (lookupExecutor != null)
		{
			lookupExecutor.shutdownNow();
			lookupExecutor = null;
		}
		log.debug("Boss KC Rival Lookup stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && config.showLoginHint())
		{
			addGameMessage("Boss KC Rival Lookup ready. Type !bosskc or !bosskc rival | boss.");
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.PUBLICCHAT && event.getType() != ChatMessageType.PRIVATECHAT)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage()).trim();
		if (!message.toLowerCase().startsWith("!bosskc"))
		{
			return;
		}

		BossKcRivalLookupFormatter.LookupRequest request = BossKcRivalLookupFormatter.parseCommand(
			message,
			config.rivalName(),
			config.bossName()
		);
		lookup(request);
	}

	private void lookup(BossKcRivalLookupFormatter.LookupRequest request)
	{
		Player localPlayer = client.getLocalPlayer();
		String localName = localPlayer == null ? null : localPlayer.getName();
		if (localName == null || localName.trim().isEmpty())
		{
			addGameMessage("Boss KC lookup needs a logged-in local player.");
			return;
		}

		Optional<HiscoreSkill> bossSkill = BossKcRivalLookupFormatter.findBossSkill(request.getBoss());
		if (!bossSkill.isPresent())
		{
			addGameMessage("Unknown boss hiscore entry: " + request.getBoss());
			return;
		}

		lookupExecutor.submit(() -> lookupOnBackgroundThread(localName, request.getRival(), bossSkill.get()));
	}

	private void lookupOnBackgroundThread(String localName, String rivalName, HiscoreSkill bossSkill)
	{
		try
		{
			HiscoreResult localResult = hiscoreClient.lookup(localName);
			HiscoreResult rivalResult = hiscoreClient.lookup(rivalName);
			if (localResult == null || rivalResult == null)
			{
				addGameMessage("Boss KC lookup failed: one of the players is not on the hiscores.");
				return;
			}

			int localKills = BossKcRivalLookupFormatter.killCount(localResult.getSkill(bossSkill));
			int rivalKills = BossKcRivalLookupFormatter.killCount(rivalResult.getSkill(bossSkill));
			addGameMessage(BossKcRivalLookupFormatter.formatComparison(
				localName,
				rivalName,
				bossSkill.getName(),
				localKills,
				rivalKills
			));
		}
		catch (IOException ex)
		{
			log.warn("Unable to look up boss KC", ex);
			addGameMessage("Boss KC lookup failed: " + ex.getMessage());
		}
	}

	private void addGameMessage(String message)
	{
		clientThread.invokeLater(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null));
	}

	@Provides
	BossKcRivalLookupConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossKcRivalLookupConfig.class);
	}
}
