package com.trevor.ultimatehue;

/**
 * Created by nemo on 9/28/15.
 */
public class HueColor {

    private long _id;
    private String key;
    private String name;
    private int hue;
    private int saturation;
    private int brightness;
    private String imageId;
    private int favorite;
    private int timesClicked;
    private boolean isOn;

    public HueColor(String key, String name, int hue, int saturation, int brightness, String imageId, int favorite, int timesClicked) {
        this.key = key;
        this.name = name;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.imageId = imageId;
        this.favorite = favorite;
        this.timesClicked = timesClicked;
        this.isOn = true;
    }

    public HueColor() {
        this.isOn = true;
    };

    public long get_id() {
        return _id;
    }



    public void set_id(long _id) {
        this._id = _id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getTimesClicked() {
        return timesClicked;
    }

    public void setTimesClicked(int timesClicked) {
        this.timesClicked = timesClicked;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
    }

}
