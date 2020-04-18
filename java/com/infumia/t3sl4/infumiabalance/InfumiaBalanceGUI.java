package com.infumia.t3sl4.infumiabalance;

import com.infumia.t3sl4.infumiabalance.gui.BalanceGuiCustom;
import com.infumia.t3sl4.infumiabalance.inv.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class InfumiaBalanceGUI extends JavaPlugin {

    private BalanceManager balanceManager;
    private static InventoryManager invManager;
    private Economy econ;

    public static InventoryManager getInvManager() {
        return invManager;
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getConsoleSender().sendMessage("   ");
        Bukkit.getConsoleSender().sendMessage("  ___            __                       _         ");
        Bukkit.getConsoleSender().sendMessage(" |_ _|  _ __    / _|  _   _   _ __ ___   (_)   __ _ ");
        Bukkit.getConsoleSender().sendMessage("  | |  | '_ \\  | |_  | | | | | '_ ` _ \\  | |  / _` |");
        Bukkit.getConsoleSender().sendMessage("  | |  | | | | |  _| | |_| | | | | | | | | | | (_| |");
        Bukkit.getConsoleSender().sendMessage(" |___| |_| |_| |_|    \\__,_| |_| |_| |_| |_|  \\__,_|");
        Bukkit.getConsoleSender().sendMessage("    ");

        if (!setupEconomy()) {
            getLogger().severe("Sunucunuzda Yapilandirilmamis Vault Oldugundan Dolayi Plugin Devre Disi Kalacaktir !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        invManager = new InventoryManager(this);
        invManager.init();

        balanceManager = new BalanceManager(this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEcon() {
        return econ;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("baltop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Bu Komutu da Konsolda Denemezsin be Adam :P");
                return true;
            }
            if (getBalanceManager().wantReload()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("No-Ranking-Yet")));
                long ms = getBalanceManager().reloadBalances();
                sender.sendMessage(getBalanceManager().topMap.size() + " veri "+(ms/1000) + " saniyede yüklendi!");
                return true;
            }
            if (getBalanceManager().isInReload()) {
                sender.sendMessage("şuan reload yapıyor aqü");
                return true;
            }
            new BalanceGuiCustom(0, this).open((Player) sender);
            return true;
        }
        return false;
    }


}
