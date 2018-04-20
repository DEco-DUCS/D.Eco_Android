package com.deco.coryl.deco;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeTour extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PopupMenu.OnMenuItemClickListener {

    private GoogleMap mMap;
    // URL_STRING is the url where the restful api is located
    String URL_STRING = "http://ec2-34-210-220-81.us-west-2.compute.amazonaws.com//index2.php";
    // Url string pointing to images on ec2 instance
    String IMAGE_URL_STRING = "http://ec2-34-210-217-19.us-west-2.compute.amazonaws.com/PHP/html/adminPHP/html/upload/";
    // Tag name for debugging
    private String TAG = TreeTour.class.getSimpleName();
    // count for how many times location has changed
    private static int locationChangeCount = 0;

    // variable to hold the static value for how close the trigger distance is to remove a point from the tour as "visited" (measurement in meters)
    private static float distanceTrigger = 1;

    // Polyline that can be overwritten on location change
    Polyline polyline;
    // Polyline that will be overwritten on successive location changes
    Polyline newPolyline;

    // List of trees (LatLng)
    ArrayList<LatLng> trees;

    // defined zoom level for google maps we will use
    public static final int ZOOM_LEVEL = 16;
    // zoom update level for location updates
    public static final int UPDATE_ZOOM_LEVEL = 18;
    // Variables for Current location functionality
    private GoogleApiClient client;
    protected LocationRequest locationRequest;
    protected Location lastLocation;
    protected LocationManager lm;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private boolean locationEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_tour);

        // Execute the AsyncTask to fetch tree data
        new GetTrees().execute();

        // Initialize count for polylines on create
        locationChangeCount = 0;

        // Initialize the list of trees
        trees = new ArrayList<>();


        // this adds a listener to switch activities on click
        /*homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivity();
            }
    });*/

        // Check user has at least minimum SDK for current location
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.tt_map);
        mapFragment.getMapAsync(this);
    }

    // This function switches the activity from main to tree tour
    /*private void launchActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }*/

    // This function returns true or false when checking for location permission
    public boolean checkLocationPermission() {

        // Checks if location permission is not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Checks if user has denied location permission previously or checked box to not ask again
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }

            // We still ask for location permission because we need it
            else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);

            }
            return false;
        }

        // Return true if we have permission
        else
            return true;

    }

    // This function handles our Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:

                //Permission is granted
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if(client == null) {

                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }

                //Permission is denied
                else {
                    Toast.makeText(this, "Permission Denied" , Toast.LENGTH_LONG).show();
                }
        }
    }

    // This function is called once a device is connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }


    }

    //This function is called when a location change is detected
    @Override
    public void onLocationChanged(Location location) {

        //lastLocation is set to the last known location which is passed to the function
        lastLocation = location;

        ArrayList<LatLng> newPoints = new ArrayList<>();
        LatLng myPoint = new LatLng(location.getLatitude(),location.getLongitude());
        newPoints.add(myPoint);

        if(locationChangeCount == 0)
        {
            polyline = mMap.addPolyline(new PolylineOptions().color(Color.BLUE));
            // Sort the array of points in order of distance to user location
            PolylineDrawer polylineDrawer = new PolylineDrawer();
            ArrayList<LatLng> sortedTrees = polylineDrawer.sortArray(trees,location);
            Log.e(TAG,sortedTrees.toString());
            // add each tree in sorted array to the points List
            for(int i=0; i<sortedTrees.size();i++)
            {
                newPoints.add(sortedTrees.get(i));
            }
            polyline.setPoints(newPoints);
            Toast.makeText(this,"Please follow route to begin the Tree Tour :)",Toast.LENGTH_LONG).show();
        } else {
            if(polyline != null)
            {
                PolylineDrawer polylineDrawer = new PolylineDrawer();
                ArrayList<LatLng> points = (ArrayList<LatLng>) polyline.getPoints();

                Location nextPoint = new Location("Next location on tour");
                nextPoint.setLatitude(points.get(1).latitude);
                nextPoint.setLongitude(points.get(1).longitude);
                Log.d(TAG,"Next point in tour:"+nextPoint.toString());

                if(location.distanceTo(nextPoint) <= distanceTrigger) {
                    points.remove(1);
                }

                points.remove(0);
                points = polylineDrawer.sortArray(points,location);
                polyline.remove();
                newPoints.set(0,myPoint);
                polyline = mMap.addPolyline(new PolylineOptions().color(Color.BLUE));
                for(int i=0; i<points.size();i++)
                {
                    newPoints.add(points.get(i));
                }
                polyline.setPoints(newPoints);

            }
        }

        locationChangeCount += 1;

        // ???
        if(client != null) {

            //LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    //This function is called to build the google API client
    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onConnectionSuspended(int i){

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.home_menu, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    public void goToDescriptionPage(){
        Intent intent = new Intent(this,TreeInfoPage.class);
        startActivity(intent);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Springfield Hall and move the camera
        LatLng spHall = new LatLng(37.218588, -93.285534);
        // Center the view on Springfield Hall
        mMap.moveCamera(CameraUpdateFactory.newLatLng(spHall));
        // Zoom in for relative view
        mMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
        // Specify the type of Google Map being displayed
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        locationChangeCount = 0;

        // Set info winder adapter to our custom info window
        CustomInfoWindowGoogleMaps customInfoWindow = new CustomInfoWindowGoogleMaps(this);
        mMap.setInfoWindowAdapter(customInfoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Log.e("MARKER_DATA:",marker.getTitle().toString()+marker.getSnippet().toString()+marker.getTag());
                String title = marker.getTitle().toString();
                String subTitle = marker.getSnippet().toString();
                Tree treeData = (Tree) marker.getTag();
                //Log.e("TREE_DATA", treeData.getDescription().toString()+treeData.getImage().toString());
                String filepath = treeData.getImage();
                String description = treeData.getDescription();
                Intent intent = new Intent(TreeTour.this,TreeInfoPage.class);
                intent.putExtra("title",title);
                intent.putExtra("subtitle",subTitle);
                intent.putExtra("filepath",filepath);
                intent.putExtra("description",description);
                startActivity(intent);
            }
        });

    }

    // Create class to handle the AsyncTask
    private class GetTrees extends AsyncTask<Void,Void,ArrayList<HashMap<String,String>>> {

        // execute
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(Void... args) {

            HttpHandler sh = new HttpHandler();

            ArrayList<HashMap<String,String>> treeList = new ArrayList<>();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(URL_STRING);


            if (jsonStr != null) {

                try {

                    // Getting JSON Array node
                    JSONArray jsonAry = new JSONArray(jsonStr);

                    // iterate through the JSON
                    for (int i = 0; i < jsonAry.length(); i++) {
                        JSONObject c = jsonAry.getJSONObject(i);
                        // Get the values for each attribute
                        String id = c.getString("id");
                        String cname = c.getString("common_name");
                        String sname = c.getString("scientific_name");
                        String latitude = c.getString("latitude");
                        String longitude = c.getString("longitude");
                        String description = c.getString("description");

                        // temp hash map for single tree
                        HashMap<String, String> tree = new HashMap<>();

                        // add each child node to HashMap key => value
                        tree.put("longitude",longitude);
                        tree.put("latitude",latitude);
                        tree.put("scientific_name",sname);
                        tree.put("common_name",cname);
                        tree.put("id",id);
                        tree.put("description",description);

                        //adding tree to list of trees
                        treeList.add(tree);

                    }

                    // Return the array of JSON Objects (Trees)
                    return treeList;

                }catch(final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Json parsing error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        // Catches the value returned, for manipulation on the main thread. 'result' is the result returned from doInBackground()
        @Override
        protected void onPostExecute(ArrayList<HashMap<String,String>> result) {
            super.onPostExecute(result);


            for(int i=0; i<result.size();i++)
            {
                HashMap tree  = result.get(i);

                // Convert to correct types
                String sName = (String) tree.get("scientific_name");
                String cName = (String) tree.get("common_name");
                double latitude = Double.parseDouble((String) tree.get("latitude"));
                double longitude = Double.parseDouble((String) tree.get("longitude"));
                int id = Integer.parseInt((String) tree.get("id"));
                String description = (String) tree.get("description");

                // convert cName to picture file name
                String[] nameAry = cName.split(" ");
                String filename = IMAGE_URL_STRING;
                if(nameAry.length >=1) {
                    for(int index=0; index<nameAry.length; index++){
                        //Log.e(TAG, "check for string"+nameAry[index]);
                        if(index == 0){
                            filename += nameAry[index];
                        }
                        else {
                            filename += "%20"+nameAry[index];
                        }
                    }
                    filename += ".jpg";
                }

                // Create tree object for data passing
                Tree tempTree = new Tree(latitude,longitude);
                tempTree.setTitle(cName);
                tempTree.setSnippet(sName);
                tempTree.setImage(filename);
                if(description != null) {
                    tempTree.setDescription(description);
                }


                // Create a lat long object for the tree since MarkerOptions takes an input of LatLong object for position
                LatLng treeLoc = new LatLng(latitude,longitude);

                // add each tree to a List<LatLng> for onLocationChanged function
                trees.add(treeLoc);

                // Add tree markers to the map
                MarkerOptions markerOptions = new MarkerOptions().position(treeLoc).title(cName).snippet(sName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                Marker m = mMap.addMarker(markerOptions);
                m.setTag(tempTree);
                //m.showInfoWindow();

            }



            //
            //  Create a polyline and add the trees in order
            //
            //
            //
            /*if(polyline != null) {
                polyline.remove();
            }
            polyline = mMap.addPolyline(new PolylineOptions().color(Color.BLUE));

            List<LatLng> points;

            // Sort array of trees
            PolylineDrawer polylineDrawer = new PolylineDrawer();
            ArrayList<LatLng> sortedTrees = polylineDrawer.sortArray(result);

            points = polyline.getPoints();

            for(int i=0;i<sortedTrees.size();i++)
            {
                points.add(sortedTrees.get(i));
            }

            polyline.setPoints(points);*/

        }

    }

}
