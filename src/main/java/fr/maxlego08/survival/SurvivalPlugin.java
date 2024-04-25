package fr.maxlego08.survival;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.maxlego08.survival.storage.Persist;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SurvivalPlugin extends JavaPlugin implements Listener {

    private final Set<Material> survivableBlocks = EnumSet.noneOf(Material.class);
    private Users users = new Users();
    private Gson gson;
    private Persist persist;

    @Override
    public void onEnable() {

        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
        this.persist = new Persist(this);
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);
        initSurvivableBlocks();

        this.users = this.persist.loadOrSaveDefault(this.users, Users.class);

        Expansion expansion = new Expansion(this);
        expansion.register();
    }

    public Gson getGson() {
        return gson;
    }

    private void initSurvivableBlocks() {
        for (Material material : Material.values()) {
            if (material.isBlock() && material.isItem()) {
                if (material.getHardness() >= 0) {
                    this.survivableBlocks.add(material);
                }
            }
        }
        getLogger().info("Load " + this.survivableBlocks.size() + " blocks");
    }

    @Override
    public void onDisable() {
        this.persist.save(this.users);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            Material pickedUpMaterial = event.getItem().getItemStack().getType();

            if (survivableBlocks.contains(pickedUpMaterial)) {
                User user = this.users.computeIfAbsent(player.getUniqueId(), k -> new User());
                Set<Material> collectedMaterials = user.getMaterials();

                if (!collectedMaterials.contains(pickedUpMaterial)) {
                    collectedMaterials.add(pickedUpMaterial);
                    message(player, "new-block-message", "%block%", capitalize(pickedUpMaterial.name()));


                    if (collectedMaterials.containsAll(survivableBlocks)) {
                        message(player, "all-block-message");
                    }

                    this.persist.save(this.users);
                }
            }
        }
    }

    private String capitalize(String input) {
        return Arrays.stream(input.split("_")).map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }

    private void message(CommandSender sender, String key, Object... args) {
        sender.sendMessage(color(getMessage(getConfig().getString(key, key + " was not found !"), args)));
    }

    private String getMessage(String message, Object... args) {
        if (args.length % 2 != 0) {
            System.err.println("Impossible to apply the method for messages.");
        } else {
            for (int a = 0; a < args.length; a += 2) {
                String replace = args[a].toString();
                String to = args[a + 1].toString();
                message = message.replace(replace, to);
            }
        }
        return message;
    }

    private String color(String message) {
        if (message == null) return null;
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, String.valueOf(net.md_5.bungee.api.ChatColor.of(color)));
            matcher = pattern.matcher(message);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }

    public int getTotal(UUID uniqueId) {
        return this.users.getTotal(uniqueId);
    }
}