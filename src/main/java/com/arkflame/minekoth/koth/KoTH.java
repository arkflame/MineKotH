package com.arkflame.minekoth.koth;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class Koth {
    private int id;
    private String name;
    private String worldName;
    private Position firstPosition;
    private Position secondPosition;
    private int timeLimit;
    private int timeToCapture;
    private Rewards rewards;
    private String times;
    private String days;

    public Koth(int id, String name, String worldName, Position firstPosition, Position secondPosition, int timeLimit, int timeToCapture, Rewards rewards, String times, String days) {
        this.id = id;
        this.name = name;
        this.worldName = worldName;
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        this.timeLimit = timeLimit;
        this.timeToCapture = timeToCapture;
        this.rewards = rewards;
        this.times = times;
        this.days = days;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
    
    public Location getFirstPosition() {
        return new Location(getWorld(), firstPosition.getX(), firstPosition.getY(), firstPosition.getZ());
    }

    public Location getSecondPosition() {
        return new Location(getWorld(), secondPosition.getX(), secondPosition.getY(), secondPosition.getZ());
    }

    public boolean isInside(Location location) {
        double xMin = Math.min(firstPosition.getX(), secondPosition.getX());
        double xMax = Math.max(firstPosition.getX(), secondPosition.getX());
        double yMin = Math.min(firstPosition.getY(), secondPosition.getY());
        double yMax = Math.max(firstPosition.getY(), secondPosition.getY());
        double zMin = Math.min(firstPosition.getZ(), secondPosition.getZ());
        double zMax = Math.max(firstPosition.getZ(), secondPosition.getZ());

        return location.getWorld().getName().equals(worldName) &&
               location.getX() >= xMin && location.getX() <= xMax &&
               location.getY() >= yMin && location.getY() <= yMax &&
               location.getZ() >= zMin && location.getZ() <= zMax;
    }

    public boolean isInside(Player player) {
        return isInside(player.getLocation());
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public int getTimeToCapture() { return timeToCapture; }
    public void setTimeToCapture(int timeToCapture) { this.timeToCapture = timeToCapture; }

    public Rewards getRewards() { return rewards; }
    public void setRewards(Rewards rewards) { this.rewards = rewards; }

    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public Location getCenter() {
        if (firstPosition == null || secondPosition == null) return null;
        double x = (firstPosition.getX() + secondPosition.getX()) / 2;
        double y = (firstPosition.getY() + secondPosition.getY()) / 2;
        double z = (firstPosition.getZ() + secondPosition.getZ()) / 2;

        return new Location(getWorld(), x, y, z);
    }
}