package fr.oneblock.bedwarsrandomdrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BedwarsRandomDropAddon extends JavaPlugin implements Listener {

    private final Random random = new Random();

    private final List<Material> lootPool = Collections.unmodifiableList(Arrays.asList(
            Material.IRON_INGOT,
            Material.GOLD_INGOT,
            Material.DIAMOND,
            Material.EMERALD,
            Material.CHAINMAIL_BOOTS,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_HELMET,
            Material.WOOD_SWORD,
            Material.STONE_SWORD,
            Material.ENDER_PEARL,
            Material.GOLDEN_APPLE,
            Material.TNT,
            Material.WOOL,
            Material.GLASS,
            Material.BOW,
            Material.ARROW,
            Material.WATER_BUCKET
    ));

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        long periodTicks = 20L * 5L;
        Bukkit.getScheduler().runTaskTimer(this, this::distributeRandomItem, periodTicks, periodTicks);

        getLogger().info("BedwarsRandomDropAddon activé.");
    }

    private void distributeRandomItem() {
        List<Player> eligiblePlayers = getEligiblePlayers();
        if (eligiblePlayers.isEmpty()) {
            return;
        }

        Player target = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));
        Material randomMaterial = lootPool.get(random.nextInt(lootPool.size()));

        target.getInventory().addItem(new org.bukkit.inventory.ItemStack(randomMaterial, 1));
        target.sendMessage(ChatColor.GREEN + "[BedWars] " + ChatColor.YELLOW + "Tu as reçu: "
                + ChatColor.AQUA + prettifyMaterial(randomMaterial));
    }

    /**
     * Si BedWars1058 est présent, on ne retient que les joueurs réellement en partie BedWars.
     * Sinon fallback sur tous les joueurs en ligne.
     */
    private List<Player> getEligiblePlayers() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            return online;
        }

        List<Player> inBedwars = new ArrayList<>();
        for (Player player : online) {
            if (isPlayerInBedwars(player)) {
                inBedwars.add(player);
            }
        }
        return inBedwars;
    }

    /**
     * Hook tolérant via réflexion pour éviter de casser si l'API change.
     */
    private boolean isPlayerInBedwars(Player player) {
        try {
            Class<?> bedWarsClass = Class.forName("com.andrei1058.bedwars.BedWars");
            Object api = bedWarsClass.getMethod("getAPI").invoke(null);

            for (String methodName : Arrays.asList("isPlayerPlaying", "isPlaying")) {
                try {
                    Object result = api.getClass().getMethod(methodName, Player.class).invoke(api, player);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                } catch (NoSuchMethodException ignored) {
                    // On teste le prochain nom de méthode.
                }
            }
        } catch (Exception ex) {
            // On évite le spam console : en cas d'erreur API, fallback = false.
        }
        return false;
    }

    private String prettifyMaterial(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }
}
