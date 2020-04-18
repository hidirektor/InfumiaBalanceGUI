package com.infumia.t3sl4.infumiabalance;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class BalanceManager {
    public LinkedHashMap<String, Double> topMap = new LinkedHashMap<>();
    public List<Map.Entry<String, Double>> indexedList = new ArrayList<>();
    private long lastLoad = 0;
    private final InfumiaBalanceGUI infumiaBalance;
    private boolean inReload;

    public BalanceManager(InfumiaBalanceGUI infumiaBalance) {
        this.infumiaBalance = infumiaBalance;
    }

    public boolean wantReload() {
        return !isInReload() && (lastLoad==0 || (System.currentTimeMillis()-lastLoad)>=infumiaBalance.getConfig().getInt("Sort-Task-Seconds") * 1000);
    }

    public boolean isInReload() {
        return inReload;
    }

    public long reloadBalances() {
        long startLoad = System.currentTimeMillis();
        inReload = true;
        lastLoad = System.currentTimeMillis();
        HashMap<String, Double> balanceMap = new HashMap<>();
        List<OfflinePlayer> players = Arrays.asList(Bukkit.getOfflinePlayers());
        topMap = new LinkedHashMap<>();
        players.forEach(p -> {
            if (infumiaBalance.getEcon().getBalance(p) > 0) {
                balanceMap.put(p.getName(), infumiaBalance.getEcon().getBalance(p));
            }
        });
        topMap = balanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        indexedList = new ArrayList<Map.Entry<String, Double>>(topMap.entrySet());
        inReload = false;
        return System.currentTimeMillis()-startLoad;
    }
}
