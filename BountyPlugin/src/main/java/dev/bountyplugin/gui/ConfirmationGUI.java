package dev.bountyplugin.gui;

import dev.bountyplugin.BountyPlugin;
import dev.bountyplugin.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class ConfirmationGUI {

    private final BountyPlugin plugin;

    public ConfirmationGUI(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory build(PendingBounty pendingBounty, OfflinePlayer targetPlayer) {
        ConfigManager config = plugin.getConfigManager();
        ConfirmationHolder holder = new ConfirmationHolder(pendingBounty);

        Inventory inventory = plugin.getServer().createInventory(
                holder, config.getGuiSize(), config.getGuiTitle());
        holder.setInventory(inventory);

        fillBackground(inventory);
        inventory.setItem(config.getInfoSlot(), buildInfoItem(pendingBounty, targetPlayer));
        inventory.setItem(config.getConfirmSlot(), buildButton(
                Material.LIME_WOOL, "&a&lBestaetigen",
                List.of("&7Klicke, um den Bounty", "&7endgueltig zu setzen.")));
        inventory.setItem(config.getCancelSlot(), buildButton(
                Material.RED_WOOL, "&c&lAbbrechen",
                List.of("&7Klicke, um abzubrechen.", "&7Es wird nichts abgebucht.")));

        return inventory;
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack buildInfoItem(PendingBounty pendingBounty, OfflinePlayer targetPlayer) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(targetPlayer);
            meta.setDisplayName(color("&6&l" + pendingBounty.getTargetName()));

            List<String> lore = new ArrayList<>();
            lore.add(color("&7Bounty-Betrag: &f" +
                    plugin.getConfigManager().getMoneyFormatter().formatFull(pendingBounty.getAmount())));
            lore.add(color("&7(" +
                    plugin.getConfigManager().getMoneyFormatter().formatAbbreviated(pendingBounty.getAmount()) + ")"));
            meta.setLore(lore);

            head.setItemMeta(meta);
        }

        return head;
    }

    private ItemStack buildButton(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(color(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
