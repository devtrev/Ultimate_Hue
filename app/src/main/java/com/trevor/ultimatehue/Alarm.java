package com.trevor.ultimatehue;

/**
 * Created by nemo on 12/25/15.
 */
public class Alarm {

    private String name;
    private String description;
    private String time;
    private String repeats;
    private String identifier;

    private boolean isEnabled;

    public Alarm(String identifier, String name, String description, String time, String repeats, boolean isEnabled) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.time = time;
        this.repeats = repeats;
        this.isEnabled = isEnabled;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getRepeats() {
        return repeats;
    }

    public void setRepeats(String repeats) {
        this.repeats = repeats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
