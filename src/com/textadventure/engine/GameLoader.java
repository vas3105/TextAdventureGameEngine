package com.textadventure.engine;
import com.textadventure.model.Room;
import com.textadventure.model.Item;

// Import necessary utility classes
import java.util.Map;
import java.util.HashMap; 
import java.util.List;   
import java.util.ArrayList; 

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.textadventure.engine.GameLoader.GameDataException;
public class GameLoader {

    // --- Intermediate Data Classes (mirroring JSON structure) ---
    private static class ItemData { String name; String description; }
    private static class RoomData { String name; String description; Map<String, String> exits; List<String> items; }
    private static class GameData { String playerStart; List<ItemData> items; List<RoomData> rooms; }

    // --- Attributes to Store FINAL Loaded Data ---
    private Map<String, Room> loadedRooms;
    private Map<String, Item> loadedItems;
    private String playerStartRoomName;

    // --- Gson Instance ---
    private final Gson gson;

    // --- Constructor ---
    public GameLoader() {
        this.loadedRooms = new HashMap<>();
        this.loadedItems = new HashMap<>();
        this.gson = new Gson();
        System.out.println("GameLoader initialized. Gson parser ready.");
    }
    public void loadGameData(String filePath) throws IOException, JsonSyntaxException, IllegalArgumentException, GameDataException {
        // 1. Validate filePath
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        System.out.println("Attempting to load game data from: " + filePath);

        // 2. Read the JSON file content into a String
        Path path = Paths.get(filePath);
        String jsonContent;
        try {
            jsonContent = Files.readString(path);
            System.out.println("Successfully read content from file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading game data file at path: " + filePath);
            System.err.println("Reason: "+e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new IOException("Failed to read game data file: " + filePath, e);
        }

        // 3. Use Gson to parse the JSON string into intermediate GameData object
        GameData gameData;
        try {
            System.out.println("Parsing JSON content...");
            gameData = gson.fromJson(jsonContent, GameData.class);
            if (gameData == null) {
                // Handle cases where JSON is valid but represents 'null' or empty object incorrectly
                throw new GameDataException("Parsed game data is null. JSON might be empty or fundamentally incorrect.");
            }
            System.out.println("JSON content successfully parsed into intermediate GameData object.");
        } catch (JsonSyntaxException e) {
            System.err.println("Error invalid JSON syntax in file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            throw new JsonSyntaxException("Invalid JSON syntax encountered in: " + filePath + ". Please check the file format.",e);
        } catch (Exception e) {
            // Catch other potential runtime exceptions during parsing
            System.err.println("An unexpected error occurred during JSON parsing: " + filePath);
            System.err.println("Details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new GameDataException("An unexpected error occurred while parsing game data from '" + filePath + "'.", e);
        }

        // 4. Process the intermediate data to create actual game objects
        try {
            processIntermediateData(gameData);
            System.out.println("Intermediate GameData successfully processed into Room and Item objects.");
        } catch (GameDataException e) {
            // Catch logical errors from the processing step
            System.err.println("ERROR: Logical error found while processing game data from '" + filePath + "':");
            System.err.println("Details: " + e.getMessage());
            throw e; // Re-throw the specific game data error
        }
    }
        private void processIntermediateData(GameData gameData) throws GameDataException {
        // --- Basic Validation of GameData ---
        if (gameData.items == null) {
            System.out.println("Warning: 'items' array not found or null in JSON. No items will be loaded.");
            gameData.items = new ArrayList<>(); // Avoid NullPointerException later
        }
        if (gameData.rooms == null) {
            throw new GameDataException("'rooms' array not found or null in JSON. Cannot load game world.");
        }
        if (gameData.playerStart == null || gameData.playerStart.trim().isEmpty()) {
            throw new GameDataException("'playerStart' field not found, null, or empty in JSON. Cannot determine starting room.");
        }

        // --- Step 1: Process Items ---
        // Create all Item objects first and store them in loadedItems.
        for (ItemData itemData : gameData.items) {
            if (itemData == null || itemData.name == null || itemData.name.trim().isEmpty()) {
                System.err.println("Warning: Skipping invalid item data (null or missing name).");
                continue; // Skip this invalid item
            }
            String itemName = itemData.name.trim();
            if (loadedItems.containsKey(itemName)) {
                throw new GameDataException("Duplicate item name found in JSON: '" + itemName + "'");
            }
            // Description can be null/empty, provide default if necessary
            String itemDesc = (itemData.description != null) ? itemData.description : "An item.";
            Item newItem = new Item(itemName, itemDesc);
            loadedItems.put(itemName, newItem);
            System.out.println("Created Item: " + itemName);
        }

        // --- Step 2: Process Rooms (Creation Only) ---
        // Create all Room objects with name/description and store in loadedRooms.
        for (RoomData roomData : gameData.rooms) {
            if (roomData == null || roomData.name == null || roomData.name.trim().isEmpty()) {
                System.err.println("Warning: Skipping invalid room data (null or missing name).");
                continue; // Skip invalid room
            }
            String roomName = roomData.name.trim();
            if (loadedRooms.containsKey(roomName)) {
                throw new GameDataException("Duplicate room name found in JSON: '" + roomName + "'");
            }
             // Description can be null/empty, provide default if necessary
            String roomDesc = (roomData.description != null) ? roomData.description : "A non-descript location.";
            Room newRoom = new Room(roomName, roomDesc);
            loadedRooms.put(roomName, newRoom);
            System.out.println("Created Room: " + roomName);
        }

        // --- Step 3: Link Exits and Add Items to Rooms ---
        // Iterate through rooms again now that all rooms and items exist in maps.
        for (RoomData roomData : gameData.rooms) {
            if (roomData == null || roomData.name == null || roomData.name.trim().isEmpty()) {
                continue; // Skip invalid room data encountered before
            }
            String currentRoomName = roomData.name.trim();
            Room currentRoom = loadedRooms.get(currentRoomName); // We know it exists from Step 2

            // Link Exits
            if (roomData.exits != null) {
                for (Map.Entry<String, String> exitEntry : roomData.exits.entrySet()) {
                    String direction = exitEntry.getKey();
                    String destinationRoomName = exitEntry.getValue();

                    if (direction == null || direction.trim().isEmpty() ||
                        destinationRoomName == null || destinationRoomName.trim().isEmpty()) {
                        System.err.println("Warning: Skipping invalid exit data in room '" + currentRoomName + "' (null/empty direction or destination).");
                        continue;
                    }
                    String trimmedDirection = direction.toLowerCase().trim();
                    String trimmedDestinationName = destinationRoomName.trim();
                    // Validate destination room exists
                    if (!loadedRooms.containsKey(trimmedDestinationName)) {
                        throw new GameDataException("Broken exit link in room '" + currentRoomName + "': Destination room '" + trimmedDestinationName + "' (for direction '" + trimmedDirection +"' ) not found.");
                    }
                    currentRoom.addExit(trimmedDirection, trimmedDestinationName); // Use lowercase direction
                    System.out.println("  Added exit from '" + currentRoomName + "' [" + trimmedDirection + "] to '" + trimmedDestinationName + "'");
                }
            } else {
                System.out.println("  Room '" + currentRoomName + "' has no exits defined.");
            }

            // Add Items
            if (roomData.items != null) {
                for (String itemNameFromJson : roomData.items) {
                    if (itemNameFromJson == null || itemNameFromJson.trim().isEmpty()) {
                        System.err.println("Warning: Skipping invalid item name (null/empty) listed in room '" + currentRoomName + "'.");
                        continue;
                    }
                    String itemName = itemNameFromJson.trim();
                    // Look up the actual Item object
                    Item itemToAdd = loadedItems.get(itemName);
                    if (itemToAdd == null) {
                        throw new GameDataException("Item '" + itemName + "' listed in room '" + currentRoomName + "' but not defined in the top-level 'items' array.");
                    }
                    currentRoom.addItem(itemToAdd);
                    System.out.println("  Added item '" + itemName + "' to room '" + currentRoomName + "'");
                }
            } else {
                System.out.println("  Room '" + currentRoomName + "' has no items defined.");
            }
        }

        // --- Step 4: Set and Validate Player Start ---
        String startRoomName = gameData.playerStart.trim();
        if (!loadedRooms.containsKey(startRoomName)) {
            throw new GameDataException("Player starting room '" + startRoomName + "' specified in 'playerStart' does not exist in the loaded rooms.");
        }
        this.playerStartRoomName = startRoomName; // Store the validated starting room name
        System.out.println("Player starting room set to: " + this.playerStartRoomName);
    }

    // --- Getters for Loaded Data ---
    public Map<String, Room> getLoadedRooms() { return this.loadedRooms; }
    public Map<String, Item> getLoadedItems() { return this.loadedItems; }
    public String getPlayerStartRoomName() { return this.playerStartRoomName; }
    public static class GameDataException extends Exception {
        public GameDataException(String message) {
            super(message);
        }
        public GameDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

} 