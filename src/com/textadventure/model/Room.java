package com.textadventure.model;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
//import com.textadventure.model.Item;

public class Room {

    private String name; /* The unique identifier name for the room (e.g., "Cave Entrance"). Declared as 'private' to enforce encapsulation.*/

    private String description; //stores the exits from this room

    private Map<String, String> exits; //stores the items cuurrently in this room
        
    private List<Item> items; // Declaring the List to hold Item objects

    public Room(String name, String description) {
        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if(description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        
        // Trim the name to remove leading and trailing whitespace
        this.name = name.trim();
        this.description = description;
        this.exits = new HashMap<>(); //Initialize the exits map to an empty HashMap
        this.items = new ArrayList<>(); // Initialize the items list to an empty ArrayList
    }
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<String, String> getExits() {
        return this.exits;
    }

    public List<Item> getItems() {
        return this.items;
    }
    public void addItem(Item item) {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        this.items.add(item);
    }

    public void removeItem(Item item) {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        this.items.remove(item);
    }

    public void addExit(String direction, String destinationRoomName) {
        if(direction == null || direction.trim().isEmpty()) {
            throw new IllegalArgumentException("Direction cannot be null or empty");
        }
        if(destinationRoomName == null || destinationRoomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        String normalizedDirection = direction.trim().toLowerCase();
        String trimmedDestination = destinationRoomName.trim();
        this.exits.put(normalizedDirection, trimmedDestination);
    }
}

