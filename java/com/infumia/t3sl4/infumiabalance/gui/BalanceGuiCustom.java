package com.infumia.t3sl4.infumiabalance.gui;

import com.infumia.t3sl4.infumiabalance.BalanceManager;
import com.infumia.t3sl4.infumiabalance.InfumiaBalanceGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class BalanceGuiCustom implements Listener {
    private final int start;
    private final InfumiaBalanceGUI infumiaBalance;
    private Inventory inventory;

    public BalanceGuiCustom(int start, InfumiaBalanceGUI infumiaBalance) {
        this.start = start;
        this.infumiaBalance = infumiaBalance;
        infumiaBalance.getServer().getPluginManager().registerEvents(this, infumiaBalance);
    }

    public void open(Player player) {
        BalanceManager m = infumiaBalance.getBalanceManager();
        final boolean earlyEnd = start + 45 > m.topMap.size();
        int end = earlyEnd ? m.topMap.size() : 45;
        this.inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', infumiaBalance.getConfig().getString("GUI.Title")));
        int playerIndex = -1;
        for (int i = start; i < end; i++) {
            if (m.indexedList.get(i).getKey().equalsIgnoreCase(player.getName())) {
                playerIndex = i;
            }
            inventory.addItem(generateItem(i));
        }
        int x = 0;
        for (ItemStack content : inventory.getContents()) {
            if (content == null || content.getType() == Material.AIR) {
                inventory.setItem(x, getGlass());
            }
            x++;
        }
        if (start != 0)
            inventory.setItem(45, nextOrBefore(player, false, -1));

        if (!earlyEnd)
            inventory.setItem(53, nextOrBefore(player, true, -1));

        inventory.setItem(48,
                getItem(player, infumiaBalance.getConfig().getConfigurationSection("GUI.CurrentMoney"), playerIndex, false));

        inventory.setItem(49,
                getItem(player, infumiaBalance.getConfig().getConfigurationSection("GUI.Exit"), playerIndex, false));

        inventory.setItem(50,
                getItem(player, infumiaBalance.getConfig().getConfigurationSection("GUI.CurrentOrder"), playerIndex, false));

        player.openInventory(inventory);

    }

    private ItemStack generateItem(int index) {
        BalanceManager m = infumiaBalance.getBalanceManager();
        final String player = m.indexedList.get(index).getKey();
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setDisplayName(replace(ChatColor.translateAlternateColorCodes('&',
                infumiaBalance.getConfig().getString("GUI.Skull.Name")), index, player));
        skullMeta.setOwner(player);
        skullMeta.setLore(infumiaBalance.getConfig().getStringList("GUI.Skull.Lore").stream().map(s -> replace(ChatColor.translateAlternateColorCodes('&', s), index, player)).collect(Collectors.toList()));
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    private ItemStack getItem(Player player, ConfigurationSection configurationSection, int index, boolean nextItem) {
        ItemStack itemStack;
        if (!nextItem) {
            String[] item = configurationSection.getString("Item").split(":");
            if (item.length == 1)
                itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(item[0])));
            else
                itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(item[0])), 1, Short.parseShort(item[1]));
        } else {
            itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        }
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(replace(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("Name")), index, player.getName()));
        meta.setLore(configurationSection.getStringList("Lore").stream().map(s -> replace(ChatColor.translateAlternateColorCodes('&', s), index, player.getName())).collect(Collectors.toList()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack nextOrBefore(Player player, boolean next, int index) {
        ConfigurationSection configurationSection = infumiaBalance.getConfig().getConfigurationSection("GUI." + (next ? "Next" : "Before"));
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(replace(ChatColor.translateAlternateColorCodes('&', configurationSection.getString("Name")), index, player.getName()));
        meta.setLore(configurationSection.getStringList("Lore").stream().map(s -> replace(ChatColor.translateAlternateColorCodes('&', s), index, player.getName())).collect(Collectors.toList()));
        itemStack.setItemMeta(meta);
        String base64 = next ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19" : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(itemStack,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
    }

    private String replace(String text, int index, String name) {
        BalanceManager m = infumiaBalance.getBalanceManager();
        return ChatColor.translateAlternateColorCodes('&', text.replace("{Name}", name).replace("{Position}", (index + 1) + "")
                .replace("{Money}", infumiaBalance.getEcon().format(Double.parseDouble(m.topMap.getOrDefault(name, 0.0).toString()))));
    }

    private ItemStack getGlass() {
        final int code = infumiaBalance.getConfig().getInt("GUI.Empty-Glass-Color");
        if (code != -1) {
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("");
            itemStack.setDurability((short) code);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        } else
            return new ItemStack(Material.AIR);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            e.setCancelled(true);
            return;
        }

        if (e.getAction() == InventoryAction.NOTHING && e.getClick() != ClickType.MIDDLE) {
            e.setCancelled(true);
            return;
        }

        if (e.getClickedInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', infumiaBalance.getConfig().getString("GUI.Title")))) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                if (e.getSlot() == 49) {
                    p.closeInventory();
                } else if (e.getSlot() == 45 && e.getCurrentItem().getType() == Material.SKULL_ITEM) {
                    new BalanceGuiCustom(start - 45, infumiaBalance).open((Player) e.getWhoClicked());
                } else if (e.getSlot() == 53 && e.getCurrentItem().getType() == Material.SKULL_ITEM) {
                    new BalanceGuiCustom(start + 45, infumiaBalance).open((Player) e.getWhoClicked());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(InventoryCloseEvent e) {
        if (inventory == null)
            return;
        if (e.getInventory().equals(inventory)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }



}