package com.chatworldhop;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatWorldHopPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChatWorldHopPlugin.class);
		RuneLite.main(args);
	}
}
