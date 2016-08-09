package com.trevor.ultimatehue;

/**
 * Created by nemo on 9/28/15.
 */
/*public class CycleHueColor {

    private long _id;
    private String key;
    private String name;
    private int [] hue;
    private int [] saturation;
    private int [] brightness;
    private String imageId;
    private int favorite;
    private int timesClicked;

    public CycleHueColor(String key, String name, int [] hue, int [] saturation, int [] brightness, String imageId, int favorite, int timesClicked) {
        this.key = key;
        this.name = name;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.imageId = imageId;
        this.favorite = favorite;
        this.timesClicked = timesClicked;
    }

    public CycleHueColor() {};

    public String getHueString() {
        StringBuilder hueString = new StringBuilder();

        for(int x : this.hue) {
            hueString.append(x);
            hueString.append(",");
        }

        return hueString.length() > 0 ? hueString.substring(0, hueString.length() - 1) : "";
    }

    public String getSaturationString() {
        StringBuilder hueString = new StringBuilder();

        for(int x : this.saturation) {
            hueString.append(x);
            hueString.append(",");
        }

        return hueString.length() > 0 ? hueString.substring(0, hueString.length() - 1) : "";
    }

    public String getBrightnessString() {
        StringBuilder brightnessString = new StringBuilder();

        for(int x : this.brightness) {
            brightnessString.append(x);
            brightnessString.append(",");
        }

        return brightnessString.length() > 0 ? brightnessString.substring(0, brightnessString.length() - 1) : "";
    }

    public void setHue(String hue) {
        this.hue = new int [hue.split(",").length];
        int i = 0;
        for(String x : hue.split(",")) {
            this.hue[i++] = Integer.parseInt(x);
        }
    }

    public void setSaturation(String saturation) {
        this.saturation = new int [saturation.split(",").length];
        int i = 0;
        for(String x : saturation.split(",")) {
            this.saturation[i++] = Integer.parseInt(x);
        }
    }

    public void setBrightness(String brightness) {
        this.brightness = new int [brightness.split(",").length];
        int i = 0;
        for(String x : brightness.split(",")) {
            this.brightness[i++] = Integer.parseInt(x);
        }
    }


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

    public int [] getHue() {
        return hue;
    }

    public void setHue(int [] hue) {
        this.hue = hue;
    }

    public int [] getSaturation() {
        return saturation;
    }

    public void setSaturation(int [] saturation) {
        this.saturation = saturation;
    }

    public int [] getBrightness() {
        return brightness;
    }

    public void setBrightness(int [] brightness) {
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
}*/
