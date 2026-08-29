package com.chatworldhop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds world mentions such as w302, w 302, world 302 or World302 in tag-stripped chat text.
 */
public final class WorldMentionParser
{
	// "w" or "world", optional space, exactly three digits, not embedded in another word or number
	private static final Pattern WORLD_MENTION = Pattern.compile(
		"(?i)(?<![a-z0-9])(?:world|w)\\s?(\\d{3})(?![0-9])");

	private WorldMentionParser()
	{
	}

	/**
	 * @return distinct world numbers in order of first appearance
	 */
	public static List<Integer> parse(String text)
	{
		if (text == null || text.isEmpty())
		{
			return new ArrayList<>();
		}

		Set<Integer> worlds = new LinkedHashSet<>();
		Matcher m = WORLD_MENTION.matcher(text);
		while (m.find())
		{
			worlds.add(Integer.parseInt(m.group(1)));
		}
		return new ArrayList<>(worlds);
	}
}
