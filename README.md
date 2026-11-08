# Minecraft Plugin: ShortsCraft Plugin

ShortsCraft Plugin is a Minecraft SMP opening-sequence plugin that creates a cinematic player introduction with title messages, sounds, blindness, soul particles, enchantment effects, magic circles, and randomized safe teleportation. It is designed as a Paper Minecraft plugin for servers that want a dramatic world-start or SMP awakening experience.

## Install

1. Build the plugin with Java 21 and Maven:
   `mvn package`
2. Copy the generated `target/plugin.jar` file into your server's `plugins` folder.
3. Run the server once to generate the plugin configuration.
4. Stop and restart the server.
5. Confirm that the server is running Paper with Minecraft 1.21 and Java 21 or newer.
6. Edit `plugins/SoulXSkullSMP/config.yml` if you want to adjust the teleport radius or sequence timing.

## Use

1. Join the server with operator permissions or the `soulxskull.start` permission.
2. Run:
   `/smp start`
3. All currently online players receive the welcome title and sound.
4. After the configured welcome delay, players receive blindness while the particle animation runs.
5. At the end of the sequence, each online player is teleported to a randomly selected safe location around the main world's spawn.
6. Players receive a final awakening title and sound.
7. The sequence can only run once at a time.

Default configuration:

```yaml
teleport-radius: 2500

sequence-timing:
  welcome-delay-ticks: 50
  transition-duration-ticks: 100
  animation-period-ticks: 2
```

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/43f60857-275c-4d4d-b6d6-18c082184d40?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=43f60857-275c-4d4d-b6d6-18c082184d40
