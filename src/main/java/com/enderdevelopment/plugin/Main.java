package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    private final NamespacedKey boundPlayersKey = new NamespacedKey(this, "bound_players");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("orbitalpearl").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Usage: /orbitalpearl <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return true;
        }

        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            player.sendMessage("You must be holding a fishing rod.");
            return true;
        }

        ItemMeta meta = rod.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(Material.FISHING_ROD);
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> boundPlayers = new ArrayList<>();
        if (container.has(boundPlayersKey, PersistentDataType.STRING)) {
            String playersString = container.get(boundPlayersKey, PersistentDataType.STRING);
            if (playersString != null) {
                String[] playersArray = playersString.split(",");
                for (String playerName : playersArray) {
                    boundPlayers.add(playerName);
                }
            }
        }

        if (!boundPlayers.contains(target.getName())) {
            boundPlayers.add(target.getName());
            String playersString = String.join(",", boundPlayers);
            container.set(boundPlayersKey, PersistentDataType.STRING, playersString);
            rod.setItemMeta(meta);
            player.sendMessage("You have bound " + target.getName() + " to your fishing rod.");
        } else {
            player.sendMessage(target.getName() + " is already bound to your fishing rod.");
        }

        return true;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (rod.getType() != Material.FISHING_ROD) {
            return;
        }

        ItemMeta meta = rod.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(boundPlayersKey, PersistentDataType.STRING)) {
            return;
        }

        String playersString = container.get(boundPlayersKey, PersistentDataType.STRING);
        if (playersString == null) {
            return;
        }

        String[] playersArray = playersString.split(",");
        for (String playerName : playersArray) {
            Player target = Bukkit.getPlayer(playerName);
            if (target != null) {
                target.teleport(player.getLocation());
                target.playSound(target.getLocation(), "entity.ender_pearl.throw", 1.0f, 1.0f);
                target.playSound(target.getLocation(), "entity.item.break", 1.0f, 1.0f);

                // Apply effects to the target
                target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 60 * 8, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 8, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 8, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 8, 0));

                // Schedule Weaving effect
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60 * 3, 0));
                }, 20 * 60 * 8);

                // Schedule Regeneration II effect
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 22, 1));
                }, 20 * 60 * 11);
            }
        }

        // Apply effects to the player
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 8, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 8, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 8, 0));

        // Schedule Weaving effect
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60 * 3, 0));
        }, 20 * 60 * 8);

        // Schedule Regeneration II effect
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 22, 1));
        }, 20 * 60 * 11);

        // Reduce durability of the fishing rod
        if (rod.getDurability() < rod.getType().getMaxDurability()) {
            rod.setDurability((short) (rod.getDurability() + 1));
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}