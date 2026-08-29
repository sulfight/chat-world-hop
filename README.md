# Chat World Hop

RuneLite plugin: right-click any player's chat message that mentions a world and hop straight to it.

Works in public, clan, friends chat, group ironman and private chat. Recognised formats
(case-insensitive): `w302`, `w 302`, `world 302`, `world302`. A message mentioning several
worlds gets one **Hop to world XXX** entry per world. Only worlds that exist in the live world
list are offered; the world you're already on is skipped.

The hop itself uses the same mechanism as RuneLite's built-in World Hopper, so the game's own
warnings for PvP / high-risk / members worlds apply unchanged.

## Config

- **Show hop message**: print "Hopping to World XXX.." in the chatbox when a hop starts.

## Notes

- Menu entries are only added on lines that have player options (Add friend / Report etc.),
  i.e. messages from other players. Game messages and your own messages are not handled.

## Development

```
./gradlew test   # unit tests for the world-mention parser
./gradlew run    # launch RuneLite in developer mode with the plugin loaded
```
