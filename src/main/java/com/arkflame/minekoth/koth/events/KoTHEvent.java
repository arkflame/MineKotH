package com.arkflame.minekoth.koth.events;

import com.arkflame.mineclans.MineClans;
import com.arkflame.mineclans.api.MineClansAPI;
import com.arkflame.mineclans.models.Faction;
import com.arkflame.mineclans.models.FactionPlayer;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.utils.ChatColors;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.GlowingUtility;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.Firework;

import java.util.*;

public class KothEvent {
    private static final List<Integer> COUNTDOWN_INTERVALS = Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);

    public enum KothEventState {
        UNCAPTURED,
        CAPTURING,
        STALEMATE,
        CAPTURED;

        public String getFancyName() {
            return name().substring(0, 1) + name().substring(1).toLowerCase();
        }
    }

    private final Koth koth;
    private KothEventState state;
    private Collection<Player> playersInZone;
    private List<CapturingPlayers> playersCapturing;
    private int timetoCapture;
    private boolean stalemateEnabled;
    private long startTime;
    private long timeCaptureStarted;
    private long endTime;
    private KothEventStats stats;

    public KothEvent(Koth koth) {
        this.koth = koth;
        this.state = KothEventState.UNCAPTURED;
        this.playersInZone = new HashSet<>();
        this.playersCapturing = new ArrayList<>();
        this.timetoCapture = koth.getTimeToCapture();
        this.stalemateEnabled = false;
        this.startTime = System.currentTimeMillis();
        this.stats = new KothEventStats();
    }

    public KothEventStats getStats() {
        return stats;
    }

    public KothEventState getState() {
        return state;
    }

    public boolean isStalemateEnabled() {
        return stalemateEnabled;
    }

    public void setStalemateEnabled(boolean stalemateEnabled) {
        this.stalemateEnabled = stalemateEnabled;
    }

    public void updateEffects(Player player) {
        if (player == null)
            return;

        if (isTopPlayer(player)) {
            applyCapturingEffect(player);
        } else {
            clearEffects(player);
        }
    }

    public void updatePlayerState(Player player, boolean entering) {
        CapturingPlayers oldTopGroup = getTopGroup();
        if (entering) {
            if (!playersInZone.contains(player)) {
                Player oldTopPlayer = getTopPlayer();

                // Update capturing players
                addToCapturingPlayers(player);
                evaluateState(oldTopGroup);

                // Update effects
                updateEffects(player);
                updateEffects(oldTopPlayer);

                Player topPlayer = getTopPlayer();
                if (player == topPlayer) {
                    Titles.sendTitle(player, "&aCapturing", "&aYou started capturing the koth", 10, 20, 10);
                } else {
                    Titles.sendTitle(player, "&aEntering Zone",
                            (oldTopGroup.containsPlayer(player) ? "&a" : "&c") + topPlayer.getName() + " is capturing",
                            10, 20, 10);
                }
                Sounds.play(player, 1.0f, 1.0f, "NOTE_PLING");
            }
        } else {
            if (playersInZone.contains(player)) {
                Player oldTopPlayer = getTopPlayer();

                // Update capturing players
                playersInZone.remove(player);
                removeFromCapturingPlayers(player);
                evaluateState(oldTopGroup);

                // Update effects
                Player topPlayer = getTopPlayer();
                if (oldTopPlayer != topPlayer) {
                    updateEffects(oldTopPlayer);
                    updateEffects(topPlayer);
                }
                if (player == oldTopPlayer) {
                    Titles.sendTitle(player, "&cLeaving Koth", "&7You are no longer capturing", 10, 20, 10);
                } else {
                    Titles.sendTitle(player, "&cLeaving Koth", "&7You left the koth zone", 10, 20, 10);
                }
                Sounds.play(player, 1.0f, 1.0f, "NOTE_BASS");
            }
        }
    }

    private boolean isSameTeam(Player p1, Player p2) {
        // Placeholder logic for determining if two players are on the same team.
        if (true) { // MineClans plugin enabled && MineClans config enabled
            MineClansAPI mineClansAPI = MineClans.getInstance().getAPI();

            FactionPlayer fp1 = mineClansAPI.getFactionPlayer(p1);
            FactionPlayer fp2 = mineClansAPI.getFactionPlayer(p2);
            if (fp1 != null && fp2 != null) {
                Faction f1 = fp1.getFaction();
                Faction f2 = fp2.getFaction();
                if (f1 != null && f2 != null) {
                    return f1.equals(f2);
                }
            }
        }
        return false;
    }

    private void sortCapturingPlayers() {
        playersCapturing
                .sort((group1, group2) -> Integer.compare(group2.getPlayers().size(), group1.getPlayers().size()));
    }

    private void addToCapturingPlayers(Player player) {
        playersInZone.add(player);
        for (CapturingPlayers group : playersCapturing) {
            Player firstPlayer = group.getPlayers().get(0);
            if (firstPlayer != player && isSameTeam(group.getPlayers().get(0), player)) {
                group.addPlayer(player);
                return;
            }
        }
        playersCapturing.add(new CapturingPlayers(player));
        sortCapturingPlayers();
    }

    private void removeFromCapturingPlayers(Player player) {
        playersInZone.remove(player);
        playersCapturing.removeIf(group -> {
            group.removePlayer(player);
            return group.getPlayers().isEmpty();
        });
        sortCapturingPlayers();
    }

    private void evaluateState(CapturingPlayers oldTopGroup) {
        if (state == KothEventState.CAPTURED) {
            return;
        }

        CapturingPlayers topGroup = getTopGroup();
        if (topGroup == null) {
            state = KothEventState.UNCAPTURED;
        } else if (stalemateEnabled && playersCapturing.size() > 1
                && getTopGroup() == getGroup(0)) {
            state = KothEventState.STALEMATE;
        } else {
            if (oldTopGroup != topGroup) {
                updateTimeCaptured();
            }
            state = KothEventState.CAPTURING;
        }
    }

    public void setCaptured(CapturingPlayers winners) {
        Player topPlayer = getTopPlayer();
        distributeRewards(winners);

        // Notify Discord
        DiscordHook.sendKothCaptured(koth.getName(), topPlayer == null ? "No Winner" : topPlayer.getName());

        // Show win/lose effects titles and subtitles
        for (Player player : Bukkit.getOnlinePlayers()) {
            FoliaAPI.runTaskForEntity(player, () -> {
                displayWinLoseEffects(player, isWinner(player), topPlayer);
            }, () -> {
            }, 1L);
        }

        end();
    }

    public void tick() {
        if (state == KothEventState.CAPTURING) {
            CapturingPlayers topGroup = playersCapturing.get(0);
            long secondsLeft = getTimeLeftToCapture() / 1000;
            if (secondsLeft <= 0) {
                setCaptured(topGroup);
            } else {
                Player topPlayer = getTopPlayer();
                String topPlayerName = topPlayer == null ? "No Winner" : topPlayer.getName();
                boolean sendTimeLeftTitle = COUNTDOWN_INTERVALS.contains((int) secondsLeft);

                for (Player player : playersInZone) {
                    boolean isTopPlayer = player == topPlayer;
                    boolean isTopGroup = topGroup != null && topGroup.containsPlayer(player);
                    if (isTopPlayer) {
                        Titles.sendActionBar(
                                player,
                                ChatColors.color("&aYou are capturing! &e" + getTimeLeftToCaptureFormatted()));
                    } else if (isTopGroup) {
                        Titles.sendActionBar(
                                player,
                                ChatColors.color("&a" + topPlayerName + " is capturing! &e"
                                        + getTimeLeftToCaptureFormatted()));
                    } else {
                        Titles.sendActionBar(
                                player,
                                ChatColors.color("&c" + topPlayerName + " is capturing! &e"
                                        + getTimeLeftToCaptureFormatted()));
                    }

                    if (sendTimeLeftTitle) {
                        Titles.sendTitle(player,
                                "&e" + secondsLeft,
                                isTopPlayer
                                        ? "&aYou are capturing"
                                        : isTopGroup
                                                ? "&a" + topPlayerName + " is capturing"
                                                : "&c" + topPlayerName + " is capturing",
                                10, 20, 10);
                        Sounds.play(player, 1.0f, 1.0f, "CLICK");
                    }
                }
            }
        } else if (state == KothEventState.UNCAPTURED) {
            long timeLimit = koth.getTimeLimit() * 1000L;

            if (System.currentTimeMillis() - startTime >= timeLimit) {
                end();

                // Notify Discord
                DiscordHook.sendKothTimeLimit(koth.getName());

                Titles.sendTitle("&aTime Limit", "&eNo koth winners", 10, 60, 10);
            }
        }
    }

    public boolean isWinner(Player player) {
        CapturingPlayers winners = getTopGroup();
        return winners == null ? false : winners.containsPlayer(player);
    }

    private void distributeRewards(CapturingPlayers winners) {
        Player topPlayer = winners.getPlayers().get(0);

        if (topPlayer != null) {
            Rewards rewards = koth.getRewards();
            rewards.giveRewards(topPlayer);
        }
    }

    public Firework createFirework(Location location, FireworkEffect.Type type) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder().with(type).withColor(org.bukkit.Color.LIME).withFlicker().withTrail().build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
        return firework;
    }

    private void displayWinLoseEffects(Player player, boolean isWinner, Player winner) {
        String title = isWinner ? "&aYOU WON" : "&cYOU LOSE";
        String subtitle = "&eWINNER: &b" + (winner == null ? "N/A" : winner.getName());
        Titles.sendTitle(player, title, subtitle, 10, 70, 20);
        Sounds.play(1.0f, 1.0f, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        if (isWinner) {
            applyWinnerEffect(player);
        } else {
            clearEffects(player);
        }
    }

    public void end() {
        state = KothEventState.CAPTURED;
        endTime = System.currentTimeMillis();
    }

    public long getTimeSinceEnd() {
        return System.currentTimeMillis() - endTime;
    }

    public Koth getKoth() {
        return koth;
    }

    public void updatePlayerState(Player player, Location to, boolean dead) {
        if (koth.isInside(to) && !dead) {
            updatePlayerState(player, true);
        } else {
            updatePlayerState(player, false);
        }
    }

    public CapturingPlayers getGroup(int index) {
        return playersCapturing.size() > index ? playersCapturing.get(index) : null;
    }

    public CapturingPlayers getTopGroup() {
        return getGroup(0);
    }

    public Player getTopPlayer() {
        CapturingPlayers topGroup = getTopGroup();
        return topGroup != null ? topGroup.getPlayers().get(0) : null;
    }

    public boolean isTopPlayer(Player player) {
        return player == getTopPlayer();
    }

    public boolean isCapturing(Player player) {
        return playersInZone.contains(player);
    }

    public long getTimeCapturedStarted() {
        return timeCaptureStarted;
    }

    public void updateTimeCaptured() {
        this.timeCaptureStarted = System.currentTimeMillis();
    }

    public int getTimeToCapture() {
        return timetoCapture;
    }

    public long getTimeLeftToCapture() {
        return (timeCaptureStarted + timetoCapture * 1000) - System.currentTimeMillis();
    }

    public String getTimeLeftToCaptureFormatted() {
        long time = (timeCaptureStarted + timetoCapture * 1000) - System.currentTimeMillis();
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        if (seconds < 0) {
            return "0";
        }
        if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%d", seconds);
    }

    // Get koth time limit and check if it was exceded comparing it with start time
    // and current time
    public String getTimeLeftToFinishFormatted() {
        long time = (startTime + koth.getTimeLimit() * 1000) - System.currentTimeMillis();
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        if (seconds < 0) {
            return "0";
        }
        if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%d", seconds);
    }

    public void clearAllEffects() {
        for (Player player : playersInZone) {
            clearEffects(player);
        }
    }

    private void clearTopGroupEffects() {
        CapturingPlayers topGroup = getTopGroup();
        if (topGroup == null) {
            return;
        }
        for (Player player : topGroup.getPlayers()) {
            clearEffects(player);
        }
    }

    private void applyCapturingEffect(Player player) {
        if (player != null) {
            FoliaAPI.runTask(() -> {
                MineKoth.getInstance().getParticleScheduler().spiralTrail(player, "COLOURED_DUST", 0.5, 2, 3, 20,
                        5);
                GlowingUtility.setGlowing(player, ChatColor.RED);
            });
        }
    }

    private void applyWinnerEffect(Player player) {
        if (player != null) {
            FoliaAPI.runTask(() -> {
                MineKoth.getInstance().getParticleScheduler().spiralTrail(player, "HAPPY_VILLAGER", 0.5, 2, 3, 20, 20);
                GlowingUtility.setGlowing(player, ChatColor.GREEN);
            });
        }
    }

    public void clearEffects(Player player) {
        if (player != null) {
            FoliaAPI.runTask(() -> {
                MineKoth.getInstance().getParticleScheduler().removeTrail(player);
                GlowingUtility.unsetGlowing(player);
            });
        }
    }

    public void clearPlayers() {
        clearTopGroupEffects();

        playersCapturing.clear();
        playersInZone.clear();
    }

    public Collection<Player> getPlayersInZone() {
        return playersInZone;
    }
}
