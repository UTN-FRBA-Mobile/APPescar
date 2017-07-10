package com.appescar.appescar;

/**
 * Created by dabiusi on 10/07/17.
 */

public class Ubicacion {

    public Double lat = new Double("0");
    public Double lng = new Double("0");
    public String uid = "";

    public Ubicacion() {
    }

    public Ubicacion(Double lat, Double lng, String uid)  {
        this.lat=lat;
        this.lng=lng;
        this.uid=uid;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getUid() {
        return uid;
    }

}
