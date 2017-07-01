package com.appescar.appescar;

/**
 * Created by sperez on 7/1/17.
 */

public class Pesca {
    public String img = "";
    public String fish = "";
    public String line = "";
    public String bait = "";
    public String description = "";
    public Double lat = new Double("0");
    public Double lng = new Double("0");

    public Pesca(String img, String fish, String line, String bait, String description, Double lat, Double lng) {
        this.img = img;
        this.fish = fish;
        this.line = line;
        this.bait = bait;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }
}
