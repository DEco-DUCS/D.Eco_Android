package com.deco.coryl.deco;

import android.os.AsyncTask;

/**
 * Created by coryl on 9/22/2017.
 */

public class Tree {

    private String common_name = "";
    private String scientific_name = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int id = 0;


    public Tree(String common_name,String scientific_name,double latitude,double longitude,int id)
    {
        this.setCommon_name(common_name);
        this.setScientific_name(scientific_name);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setID(id);
    }

    public String getCommon_name() {
        return common_name;
    }

    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }
}
