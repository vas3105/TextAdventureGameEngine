package com.textadventure.model;

public class Item {
    private String name;
    private String description;

    public Item(String name, String description) {
       
        if(name==null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if(description==null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        
        this.name = name;
        this.description = description;
    }
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
}
