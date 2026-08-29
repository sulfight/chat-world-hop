package com.chatworldhop;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import net.runelite.client.util.Text;
import org.junit.Test;

public class WorldMentionParserTest
{
	@Test
	public void matchesBasicForms()
	{
		assertEquals(singletonList(302), WorldMentionParser.parse("w302"));
		assertEquals(singletonList(302), WorldMentionParser.parse("W302"));
		assertEquals(singletonList(302), WorldMentionParser.parse("w 302"));
		assertEquals(singletonList(302), WorldMentionParser.parse("world 302"));
		assertEquals(singletonList(302), WorldMentionParser.parse("World302"));
		assertEquals(singletonList(302), WorldMentionParser.parse("WORLD 302"));
	}

	@Test
	public void matchesInsideSentences()
	{
		assertEquals(singletonList(330), WorldMentionParser.parse("anyone want to mass w330?"));
		assertEquals(singletonList(330), WorldMentionParser.parse("come to (w330)!"));
		assertEquals(singletonList(330), WorldMentionParser.parse("hopping to w330, brb"));
		assertEquals(singletonList(330), WorldMentionParser.parse("W330 is dead"));
	}

	@Test
	public void multipleWorldsKeepOrderAndDedupe()
	{
		assertEquals(asList(302, 330), WorldMentionParser.parse("w302 or w330"));
		assertEquals(asList(302, 330), WorldMentionParser.parse("w302 w330 w302 world 330"));
	}

	@Test
	public void rejectsNonMentions()
	{
		assertEquals(emptyList(), WorldMentionParser.parse("bow302"));
		assertEquals(emptyList(), WorldMentionParser.parse("w30"));
		assertEquals(emptyList(), WorldMentionParser.parse("w3021"));
		assertEquals(emptyList(), WorldMentionParser.parse("w"));
		assertEquals(emptyList(), WorldMentionParser.parse("wow 302"));
		assertEquals(emptyList(), WorldMentionParser.parse("1w302"));
		assertEquals(emptyList(), WorldMentionParser.parse("w  302"));
		assertEquals(emptyList(), WorldMentionParser.parse(""));
		assertEquals(emptyList(), WorldMentionParser.parse(null));
	}

	@Test
	public void worksWithTagStrippedChatText()
	{
		String raw = "<col=ff0000><img=2>w302</col> is packed";
		assertEquals(singletonList(302), WorldMentionParser.parse(Text.removeTags(raw)));
	}
}
