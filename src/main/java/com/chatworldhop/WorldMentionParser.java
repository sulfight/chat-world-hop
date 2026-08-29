package com.chatworldhop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds world mentions such as {@code w302}, {@code W302}, {@code w 302},
 * {@code world 302} or {@code World302} in plain (tag-stripped) chat text.
 */
public final class WorldMentionParser
{
	// (?<![a-z0-9]) - don't match inside another word/number, e.g. "bow302"
	// (?:world|w)   - "w" or "world", case-insensitive
	// \s?           - optional single space: "w 302", "world 302"
	// (\d{3})       - OSRS world numbers are always three digits
	// (?![0-9])     - reject "w3021"
	private static final Pattern WORLD_MENTION = Pattern.compile(
		"(?i)(?<![a-z0-9])(?:world|w)\\s?(\\d{3})(?![0-9])");

	private WorldMentionParser()
	{
	}

	/**
	 * @param text chat text with formatting tags already removed
	 * @return distinct world numbers in order of first appearance; empty if none
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
