package com.chatworldhop;

import com.google.inject.Provides;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

@Slf4j
@PluginDescriptor(
	name = "Chat World Hop",
	description = "Right-click a chat message mentioning a world (e.g. w302) to hop to it",
	tags = {"world", "hop", "hopper", "chat", "clan", "menu"}
)
public class ChatWorldHopPlugin extends Plugin
{
	private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;

	// Each chat line has 4 dynamic children in the scroll area; the message text is the second
	private static final int CHAT_LINE_CHILDREN = 4;
	private static final int CHAT_LINE_MESSAGE_OFFSET = 1;

	@Inject
	private Client client;

	@Inject
	private WorldService worldService;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ChatWorldHopConfig config;

	private net.runelite.api.World hopTargetWorld;
	private int displaySwitcherAttempts;

	@Provides
	ChatWorldHopConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChatWorldHopConfig.class);
	}

	@Override
	protected void shutDown()
	{
		resetHop();
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		final int lineChildId = findChatLineChildId(event.getMenuEntries());
		if (lineChildId < 0)
		{
			return;
		}

		final Widget scrollArea = client.getWidget(InterfaceID.Chatbox.SCROLLAREA);
		if (scrollArea == null)
		{
			return;
		}

		final int lineIndex = lineChildId - WidgetUtil.componentToId(InterfaceID.Chatbox.LINE0);
		final Widget messageWidget = scrollArea.getChild(lineIndex * CHAT_LINE_CHILDREN + CHAT_LINE_MESSAGE_OFFSET);
		if (messageWidget == null || messageWidget.getText() == null)
		{
			return;
		}

		final List<Integer> worlds = WorldMentionParser.parse(Text.removeTags(messageWidget.getText()));
		if (worlds.isEmpty())
		{
			return;
		}

		final WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			log.debug("World list not available yet");
			return;
		}

		final int currentWorld = client.getWorld();
		for (final int worldId : worlds)
		{
			if (worldId == currentWorld || worldResult.findWorld(worldId) == null)
			{
				continue;
			}

			// Index 1 is just above Cancel; inserting in mention order keeps that order top-down
			client.getMenu().createMenuEntry(1)
				.setOption("Hop to world " + worldId)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> hop(worldId));
		}
	}

	// Returns the chatbox child id of the chat line this menu was opened on, or -1
	private int findChatLineChildId(MenuEntry[] entries)
	{
		for (MenuEntry entry : entries)
		{
			final MenuAction type = entry.getType();
			if (type != MenuAction.CC_OP && type != MenuAction.CC_OP_LOW_PRIORITY)
			{
				continue;
			}

			final int componentId = entry.getParam1();
			if (WidgetUtil.componentToInterface(componentId) != InterfaceID.CHATBOX)
			{
				continue;
			}

			final Widget line = client.getWidget(componentId);
			if (line == null)
			{
				continue;
			}

			final Widget parent = line.getParent();
			if (parent == null || parent.getId() != InterfaceID.Chatbox.SCROLLAREA)
			{
				continue;
			}

			return WidgetUtil.componentToId(componentId);
		}
		return -1;
	}

	private void hop(int worldId)
	{
		final WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			return;
		}

		final World world = worldResult.findWorld(worldId);
		if (world == null)
		{
			return;
		}

		final net.runelite.api.World rsWorld = client.createWorld();
		rsWorld.setActivity(world.getActivity());
		rsWorld.setAddress(world.getAddress());
		rsWorld.setId(world.getId());
		rsWorld.setPlayerCount(world.getPlayers());
		rsWorld.setLocation(world.getLocation());
		rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			client.changeWorld(rsWorld);
			return;
		}

		if (config.showHopMessage())
		{
			sendConsoleMessage(new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("Hopping to World ")
				.append(ChatColorType.HIGHLIGHT)
				.append(Integer.toString(world.getId()))
				.append(ChatColorType.NORMAL)
				.append("..")
				.build());
		}

		hopTargetWorld = rsWorld;
		displaySwitcherAttempts = 0;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (hopTargetWorld == null)
		{
			return;
		}

		// The world switcher must be loaded before hopToWorld works
		if (client.getWidget(InterfaceID.Worldswitcher.BUTTONS) == null)
		{
			client.openWorldHopper();

			if (++displaySwitcherAttempts >= DISPLAY_SWITCHER_MAX_ATTEMPTS)
			{
				sendConsoleMessage(new ChatMessageBuilder()
					.append(ChatColorType.NORMAL)
					.append("Failed to hop after ")
					.append(ChatColorType.HIGHLIGHT)
					.append(Integer.toString(displaySwitcherAttempts))
					.append(ChatColorType.NORMAL)
					.append(" attempts.")
					.build());
				resetHop();
			}
		}
		else
		{
			client.hopToWorld(hopTargetWorld);
			resetHop();
		}
	}

	private void resetHop()
	{
		hopTargetWorld = null;
		displaySwitcherAttempts = 0;
	}

	private void sendConsoleMessage(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
