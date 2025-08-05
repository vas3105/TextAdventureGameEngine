package com.textadventure.model;

import java.util.List;
import java.util.ArrayList;
import com.textadventure.model.Item
;
public class Player {
    private String currentRoomName;
    private List<Item> inventory;

    public Player(String startingRoomName) {
        if (startingRoomName == null || startingRoomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player starting room name cannot be null or empty.");
        }

        this.currentRoomName = startingRoomName.trim();
        this.inventory = new ArrayList<>();
    }
    public void takeItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null item to player inventory.");
        }
        this.inventory.add(item);
    }

    public boolean dropItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot drop a null item from player inventory.");
        }
        boolean removed = this.inventory.remove(item);
        return removed;
    }
    public List<Item> getInventory() {
        return this.inventory;
}
    public String getCurrentRoomName() {
        return this.currentRoomName;
    }

    public void setCurrentRoomName(String newroomName) {
        if (newroomName == null || newroomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot set current room name to null or empty.");
        }
        this.currentRoomName = newroomName.trim();
    }
}