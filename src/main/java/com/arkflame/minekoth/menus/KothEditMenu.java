package com.arkflame.minekoth.menus;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.MenuUtil.Menu;
import com.arkflame.minekoth.utils.MenuUtil.MenuItem;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.Materials;

public class KothEditMenu extends Menu {
        private final Koth koth;
    
        public KothEditMenu(Player player, Koth koth) {
            super(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.koth-edit-title").replace("<id>", String.valueOf(koth.getId())), 6);
            this.koth = koth;
            setBackground(Materials.get("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:7"));
            setupItems(player);
        }
    
        private void setupItems(Player player) {
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
    
            MenuItem nameItem = new MenuItem.Builder(Materials.get("NAME_TAG"))
                    .name(lang.getMessage("messages.koth-name"))
                    .lore(
                            lang.getMessage("messages.current-value").replace("<value>", koth.getName()),
                            "",
                            lang.getMessage("messages.modify-name"),
                            lang.getMessage("messages.name-announcement"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startNameSession)
                    .build();
    
            MenuItem regionItem = new MenuItem.Builder(Materials.get("GOLDEN_AXE", "GOLD_AXE"))
                    .name(lang.getMessage("messages.region-selection"))
                    .lore(
                            lang.getMessage("messages.first-position").replace("<value>", formatLocation(koth.getFirstLocation())),
                            lang.getMessage("messages.second-position").replace("<value>", formatLocation(koth.getSecondLocation())),
                            "",
                            lang.getMessage("messages.redefine-region"),
                            lang.getMessage("messages.select-two-corners"),
                            lang.getMessage("messages.use-golden-axe"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startRegionSession)
                    .build();
    
            MenuItem timeLimitItem = new MenuItem.Builder(Materials.get("CLOCK"))
                    .name(lang.getMessage("messages.time-limit"))
                    .lore(
                            lang.getMessage("messages.current-limit").replace("<value>", formatTime(koth.getTimeLimit())),
                            "",
                            lang.getMessage("messages.set-max-duration"),
                            lang.getMessage("messages.format-mm-ss"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startTimeLimitSession)
                    .build();
    
            MenuItem captureTimeItem = new MenuItem.Builder(Materials.get("HOPPER"))
                    .name(lang.getMessage("messages.capture-time"))
                    .lore(
                            lang.getMessage("messages.current-time").replace("<value>", formatTime(koth.getTimeToCapture())),
                            "",
                            lang.getMessage("messages.time-required-to-capture"),
                            lang.getMessage("messages.format-mm-ss"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startCaptureTimeSession)
                    .build();
    
            MenuItem rewardsItem = new MenuItem.Builder(Materials.get("CHEST"))
                    .name(lang.getMessage("messages.rewards-configuration"))
                    .lore(
                            lang.getMessage("messages.commands").replace("<value>", String.valueOf(koth.getRewards().getRewardsCommands().size())),
                            lang.getMessage("messages.items").replace("<value>", String.valueOf(koth.getRewards().getRewardsItems().size())),
                            lang.getMessage("messages.loot-type").replace("<value>", String.valueOf(koth.getRewards().getLootType())),
                            lang.getMessage("messages.loot-amount").replace("<value>", String.valueOf(koth.getRewards().getLootAmount())),
                            "",
                            lang.getMessage("messages.configure-rewards"),
                            lang.getMessage("messages.set-commands-and-items"),
                            lang.getMessage("messages.adjust-loot-settings"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startRewardsSession)
                    .build();
    
            MenuItem scheduleItem = new MenuItem.Builder(Materials.get("BOOK"))
                    .name(lang.getMessage("messages.schedule-settings"))
                    .lore(
                            lang.getMessage("messages.event-times").replace("<value>", koth.getTimes()),
                            lang.getMessage("messages.active-days").replace("<value>", koth.getDays()),
                            "",
                            lang.getMessage("messages.set-event-schedule"),
                            lang.getMessage("messages.configure-active-days"),
                            lang.getMessage("messages.manage-time-slots"),
                            "",
                            lang.getMessage("messages.click-to-edit"))
                    .onClick(this::startScheduleSession)
                    .build();
    
            grid(nameItem, regionItem, timeLimitItem,
                    captureTimeItem, rewardsItem, scheduleItem);
        }
    
        private void startNameSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setName(null);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.enter-name"));
        }
    
        private void startWorldSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setWorldName(null);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.enter-world-name"));
        }
    
        private void startRegionSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setFirstPosition(null);
            session.setSecondPosition(null);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.click-positions-to-select"));
        }
    
        private void startTimeLimitSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setTimeLimit(0);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.enter-time-limit"));
        }
    
        private void startCaptureTimeSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setCaptureTime(0);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.enter-capture-time"));
        }
    
        private void startRewardsSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setRewards(null);
            session.setRewardsCommands(new ArrayList<>());
            session.setLootType(null);
            session.unsetLootAmount();
            session.setEditingRewards(true);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.put-rewards-in-chest"));
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            SetupChatListener.openRewardsInventory(player);
        }
    
        private void startScheduleSession(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            SetupSession session = new SetupSession(koth);
            session.setTimes(null);
            session.setDays(null);
            MineKoth.getInstance().getSessionManager().addSession(player, session);
            player.closeInventory();
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.enter-event-times"));
        }
    
        private String formatLocation(Location loc) {
            return String.format("§f%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    
        private String formatTime(int seconds) {
            return String.format("§f%02d:%02d", seconds / 60, seconds % 60);
        }
    }
    