package com.chatworldhop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ChatWorldHopConfig.GROUP)
public interface ChatWorldHopConfig extends Config
{
	String GROUP = "chatworldhop";

	@ConfigItem(
		keyName = "showHopMessage",
		name = "Show hop message",
		description = "Print a chat message when hopping to a world from chat"
	)
	default boolean showHopMessage()
	{
		return true;
	}
}
