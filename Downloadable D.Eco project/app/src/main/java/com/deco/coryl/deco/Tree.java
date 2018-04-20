package com.deco.coryl.deco;

import android.media.Image;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by coryl on 9/22/2017.
 */

public class Tree implements ClusterItem{

    //private int id = 0;
    private String mDescription;
    private String imgUrl;
    private final LatLng position;
    private String mTitle;
    private String mSnippet;


    public Tree(double lat, double lng) {
        position = new LatLng(lat, lng);
    }

    public Tree(double lat, double lng, String title, String snippet, String img) {
        position = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        imgUrl = img;
    }

    public Tree(double lat, double lng, String title, String snippet, String img, String description) {
        position = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        imgUrl = img;
        mDescription = description;

    }

    public String getDescription() {return mDescription;}

    public void setDescription(String paramDescription) {
        mDescription = paramDescription;
    }

    public String getImage() { return imgUrl; }

    public void setImage(String img) {
        imgUrl = img;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) { mTitle = title; }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public void setSnippet(String snippet) {mSnippet = snippet; }
}
