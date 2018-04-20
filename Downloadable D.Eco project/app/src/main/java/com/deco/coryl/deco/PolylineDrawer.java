package com.deco.coryl.deco;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by coryl on 10/27/2017.
 */

public class PolylineDrawer {

    PolylineDrawer(){

    }

    // Parameters: Array list of Trees on tour ONLY. userLocation is a Location object. If null, should be caught before being passed to this function.
    // Output: Array list of hashmaps for each tree, in the order that the polyline should be drawn. Does not include userLocation in the array.
    public ArrayList<LatLng> sortArray(ArrayList<LatLng> treeArray, Location userLocation)
    {
        ArrayList<LatLng> result = new ArrayList<>();

        while(treeArray.size() != 0) {
            // Get distance to the first point in the list
            LatLng initial = treeArray.get(0);

            int minDistanceIndex = 0;

            double initLat = initial.latitude;
            double initLong = initial.longitude;

            Location initLocation = new Location("initial location");
            initLocation.setLatitude(initLat);
            initLocation.setLongitude(initLong);

            // Find distance from user location to first point in original array, set our minDistance equal to that distance
            float distance = userLocation.distanceTo(initLocation);
            float minDistance = distance;
            //

            // Iterate through original array and find the closest point to the user's current location
            // while treeArray is not empty
            for (int i = 1; i < treeArray.size(); i++) {
                LatLng temp = treeArray.get(i);

                double tempLat = temp.latitude;
                double tempLong = temp.longitude;

                Location tempLocation = new Location("temp Location");
                tempLocation.setLatitude(tempLat);
                tempLocation.setLongitude(tempLong);

                distance = userLocation.distanceTo(tempLocation);

                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
                // Reinitialize minDistance and minDistance point
                // Remove the minDistancePoint from treeArray treeArray.remove(index)
            }
            // add tree at the minDistanceIndex to result and remove it from treeArray
            LatLng minDistancePoint = treeArray.get(minDistanceIndex);
            double latAdd = minDistancePoint.latitude;
            double longAdd = minDistancePoint.longitude;
            result.add(new LatLng(latAdd,longAdd));

            treeArray.remove(minDistanceIndex);

            // change current location for polyline
            userLocation = new Location("");
            userLocation.setLatitude(latAdd);
            userLocation.setLongitude(longAdd);

        }

        return result;

    }

    public ArrayList<LatLng> sortArray(ArrayList<HashMap<String,String>> treeArray)
    {
        ArrayList<LatLng> result = new ArrayList<>();

        // Get distance to the first point in the list
        HashMap firstNode = treeArray.get(0);
        Location previousLocation = new Location("Hello there");
        double firstLat = Double.parseDouble((String) firstNode.get("latitude"));
        double firstLong = Double.parseDouble((String) firstNode.get("longitude"));
        previousLocation.setLatitude(firstLat);
        previousLocation.setLongitude(firstLong);

        result.add(new LatLng(firstLat,firstLong));
        treeArray.remove(0);

        while(treeArray.size() != 0) {
            // Get distance to the first point in the list
            HashMap initial = treeArray.get(0);

            int minDistanceIndex = 0;

            double initLat = Double.parseDouble((String) initial.get("latitude"));
            double initLong = Double.parseDouble((String) initial.get("longitude"));

            Location initLocation = new Location("initial location");
            initLocation.setLatitude(initLat);
            initLocation.setLongitude(initLong);

            // Find distance from previous location to first point in the array, set our minDistance equal to that distance
            float distance = previousLocation.distanceTo(initLocation);
            float minDistance = distance;
            //

            // Iterate through original array and find the closest point to the user's current location
            // while treeArray is not empty
            for (int i = 1; i < treeArray.size(); i++) {
                HashMap temp = treeArray.get(i);

                double tempLat = Double.parseDouble((String) temp.get("latitude"));
                double tempLong = Double.parseDouble((String) temp.get("longitude"));

                Location tempLocation = new Location("temp Location");
                tempLocation.setLatitude(tempLat);
                tempLocation.setLongitude(tempLong);

                distance = previousLocation.distanceTo(tempLocation);

                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
                // Reinitialize minDistance and minDistance point
                // Remove the minDistancePoint from treeArray treeArray.remove(index)
            }
            // add tree at the minDistanceIndex to result and remove it from treeArray
            HashMap minDistancePoint = treeArray.get(minDistanceIndex);
            double latAdd = Double.parseDouble((String) minDistancePoint.get("latitude"));
            double longAdd = Double.parseDouble((String) minDistancePoint.get("longitude"));
            result.add(new LatLng(latAdd,longAdd));

            treeArray.remove(minDistanceIndex);

            // change current location for polyline
            previousLocation = new Location("");
            previousLocation.setLatitude(latAdd);
            previousLocation.setLongitude(longAdd);

        }

        return result;
    }

}
