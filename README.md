# Boss KC Rival Lookup

Boss KC Rival Lookup is an external RuneLite plugin that compares your boss kill count against a configured rival using the official Old School RuneScape hiscores exposed through RuneLite's `HiscoreClient`.

The plugin is intentionally lightweight: it does not store data locally, scrape webpages, or block the RuneLite game thread while network lookups are running.

## Features

- Adds a `!bosskc` chat trigger for quick kill-count comparisons.
- Uses configurable defaults for the rival player and boss/raid entry.
- Supports one-off lookups with `!bosskc <boss>` or `!bosskc <rival> | <boss>`.
- Resolves boss names loosely, so inputs such as `vorkath`, `K'ril`, or `cox challenge` can match RuneLite hiscore boss entries.
- Performs OSRS hiscore lookups on a background thread and posts the result back into the game chat.

## Configuration

Open RuneLite's plugin configuration panel after enabling the plugin.

| Setting | Default | Description |
| --- | --- | --- |
| Rival name | `Oyama` | OSRS account compared when `!bosskc` is used without a rival override. |
| Boss name | `Vorkath` | Boss/raid hiscore entry compared when `!bosskc` is used without a boss override. |
| Show login hint | enabled | Prints a short usage reminder after logging in. |

## Chat commands

```text
!bosskc
!bosskc Vorkath
!bosskc zezima | Theatre of Blood: Hard Mode
```

The command output is posted as a game message, for example:

```text
Vorkath KC: Oyama=125, Rival=100 (Oyama leads by 25)
```

## API usage

The plugin relies on RuneLite's bundled `net.runelite.client.hiscore.HiscoreClient`, which calls the official OSRS hiscore JSON endpoint. Lookups are performed off the game thread via a single-thread executor, then chat output is marshalled back through `ClientThread`.

If either player is missing from the hiscores or the lookup fails, the plugin reports a friendly in-game error message instead of throwing an exception into the client.

## Development

This repository follows the standard external RuneLite plugin layout:

```text
src/main/java/com/itmeansbigmountain/bosskcrivallookup/
  BossKcRivalLookupPlugin.java
  BossKcRivalLookupConfig.java
  BossKcRivalLookupFormatter.java
src/test/java/com/itmeansbigmountain/bosskcrivallookup/
  BossKcRivalLookupPluginTest.java
runelite-plugin.properties
build.gradle
settings.gradle
```

Use Java 11 for local builds:

```bash
export JAVA_HOME=/opt/data/jdks/current-java11
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test --no-daemon -q
./gradlew assemble --no-daemon -q
```

To launch RuneLite in developer mode with this plugin loaded:

```bash
./gradlew run --no-daemon
```

## Manual testing checklist

1. Run `./gradlew run --no-daemon` and log into RuneLite.
2. Confirm the plugin appears as `Boss KC Rival Lookup` in the plugin list.
3. Set a rival player and default boss in the plugin config.
4. Use `!bosskc`, `!bosskc Vorkath`, and `!bosskc <rival> | <boss>` in chat.
5. Confirm successful comparisons, unknown boss handling, and missing hiscore player handling all display friendly game messages.

## Plugin Hub prep notes

- `runelite-plugin.properties` points at `com.itmeansbigmountain.bosskcrivallookup.BossKcRivalLookupPlugin`.
- The plugin does not require extra runtime secrets or external services beyond OSRS hiscores.
- Before submission, capture a screenshot or short GIF of a successful lookup in RuneLite and verify the GitHub `support` URL points to the final public repository.
