package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements CommandExecutor, Listener {
    private boolean sequenceRunning;
    private BukkitTask animationTask;
    private int teleportRadius;
    private long welcomeDelayTicks;
    private long transitionDurationTicks;
    private long animationPeriodTicks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        if (getCommand("smp") != null) {
            getCommand("smp").setExecutor(this);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (animationTask != null) {
            animationTask.cancel();
        }
    }

    private void loadSettings() {
        teleportRadius = Math.max(1, getConfig().getInt("teleport-radius", 2500));
        welcomeDelayTicks = Math.max(0L, getConfig().getLong("sequence-timing.welcome-delay-ticks", 50L));
        transitionDurationTicks = Math.max(1L, getConfig().getLong("sequence-timing.transition-duration-ticks", 100L));
        animationPeriodTicks = Math.max(1L, getConfig().getLong("sequence-timing.animation-period-ticks", 2L));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("start")) {
            sender.sendMessage(Component.text("Usage: /smp start", NamedTextColor.RED));
            return true;
        }
        if (sequenceRunning) {
            sender.sendMessage(Component.text("The SoulXSkull SMP opening is already running.", NamedTextColor.RED));
            return true;
        }
        startSequence(sender);
        return true;
    }

    private void startSequence(CommandSender sender) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            sender.sendMessage(Component.text("There are no online players.", NamedTextColor.RED));
            return;
        }
        sequenceRunning = true;
        Component title = Component.text("WELCOME TO SOULXSKULL SMP", NamedTextColor.DARK_PURPLE);
        Component subtitle = Component.text("Your soulbound adventure begins now", NamedTextColor.GRAY);
        for (Player player : players) {
            player.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, title);
            player.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, subtitle);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 0.8f);
        }
        Bukkit.getScheduler().runTaskLater(this, () -> beginTransition(players), welcomeDelayTicks);
    }

    private void beginTransition(List<Player> players) {
        for (Player player : players) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) Math.min(Integer.MAX_VALUE, transitionDurationTicks + 20L), 0, false, false, false));
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);
        }
        animationTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : players) {
                if (!player.isOnline()) {
                    continue;
                }
                Location center = player.getLocation().clone().add(0, 1.2, 0);
                drawSoulOrbit(center);
                drawMagicCircle(player.getLocation());
            }
        }, 0L, animationPeriodTicks);
        Bukkit.getScheduler().runTaskLater(this, () -> finishSequence(players), transitionDurationTicks);
    }

    private void drawSoulOrbit(Location center) {
        for (int i = 0; i < 3; i++) {
            double angle = (System.currentTimeMillis() / 120.0) + (i * Math.PI * 2.0 / 3.0);
            double x = Math.cos(angle) * 1.35;
            double z = Math.sin(angle) * 1.35;
            center.getWorld().spawnParticle(Particle.SOUL, center.clone().add(x, Math.sin(angle * 1.5) * 0.25, z), 2, 0.04, 0.04, 0.04, 0.01);
            center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(x, Math.sin(angle * 1.5) * 0.25, z), 1, 0, 0, 0, 0);
        }
    }

    private void drawMagicCircle(Location location) {
        Location center = location.clone().subtract(0, 1.0, 0);
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI * 2.0 / 16.0;
            double x = Math.cos(angle) * 2.8;
            double z = Math.sin(angle) * 2.8;
            center.getWorld().spawnParticle(Particle.ENCHANT, center.clone().add(x, 0.12, z), 3, 0.05, 0.02, 0.05, 0.01);
        }
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2.0 / 8.0;
            double x = Math.cos(angle) * 1.7;
            double z = Math.sin(angle) * 1.7;
            center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(x, 0.14, z), 1, 0, 0, 0, 0);
        }
    }

    private void finishSequence(List<Player> players) {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        World world = Bukkit.getWorlds().get(0);
        for (Player player : players) {
            if (!player.isOnline()) {
                continue;
            }
            Location destination = findSafeLocation(world);
            player.teleport(destination);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.playSound(destination, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.2f);
            player.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text("SOUL AWAKENED", NamedTextColor.AQUA));
            player.sendTitlePart(net.kyori.adventure.title.TitlePart.SUBTITLE, Component.text("Find your destiny", NamedTextColor.WHITE));
        }
        sequenceRunning = false;
    }

    private Location findSafeLocation(World world) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location origin = world.getSpawnLocation();
        for (int attempt = 0; attempt < 40; attempt++) {
            int x = origin.getBlockX() + random.nextInt(-teleportRadius, teleportRadius + 1);
            int z = origin.getBlockZ() + random.nextInt(-teleportRadius, teleportRadius + 1);
            int y = world.getHighestBlockYAt(x, z) + 1;
            Location location = new Location(world, x + 0.5, y, z + 0.5);
            Material floor = world.getBlockAt(x, y - 1, z).getType();
            Material feet = world.getBlockAt(x, y, z).getType();
            Material head = world.getBlockAt(x, y + 1, z).getType();
            if (floor.isSolid() && feet.isAir() && head.isAir()) {
                return location;
            }
        }
        return world.getSpawnLocation().clone().add(0.5, 1, 0.5);
    }
}
