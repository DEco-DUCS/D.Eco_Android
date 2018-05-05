package com.deco.coryl.deco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, PopupMenu.OnMenuItemClickListener {

    private GoogleMap mMap;
    // URL_STRING is the url where the restful api is located
    String URL_STRING = "http://mcs.drury.edu/deco/treeservice/index.php";
    // Tag name for debugging
    private String TAG = MainActivity.class.getSimpleName();

    // Tree object array
    ArrayList<Tree> trees;

    // Declare a variable for the cluster manager.
    private ClusterManager<Tree> mClusterManager;

    // LatLng object for springfield Hall (Center of the map)
    private static final LatLng spHall = new LatLng(37.218588, -93.285534);

    // Variables for Current location functionality
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private LocationManager lm;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private boolean locationEnabled = false;
    public static final int ZOOM_LEVEL = 16;
    public static final int UPDATE_ZOOM_LEVEL = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);

        // (re)Initialize the list of Trees on App Create
        trees = new ArrayList<>();

        // this adds a listener to switch activities on click
        //treeTourButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {

                //launchTreeTourActivity();
            //}
        //});

        // Execute the AsyncTask to fetch tree data
        new GetTrees().execute();

        // Check user has at least minimum SDK for current location
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    // This function switches the activity from main to tree tour
    /*public void launchTreeTourActivity() {

        Intent intent = new Intent(this, TreeTour.class);
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

        //Declare latlng as the users latitude and longitude
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        //edit view to focus in on current location marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(UPDATE_ZOOM_LEVEL));

        // ???
        if(client != null) {

            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

        // Set info winder adapter to our custom info window
        CustomInfoWindowGoogleMaps customInfoWindow = new CustomInfoWindowGoogleMaps(this);
        mMap.setInfoWindowAdapter(customInfoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Log.e("MARKER_DATA:",marker.getTitle().toString()+marker.getSnippet().toString()+marker.getTag());
                Intent intent = new Intent(MainActivity.this,MainTreeInfoPage.class);
                String title = marker.getTitle().toString();
                String subTitle = marker.getSnippet().toString();
                Tree treeData = (Tree) marker.getTag();
                //Log.e("TREE_DATA", treeData.getDescription().toString()+treeData.getImage().toString());
                try {
                    String filepath = treeData.getImage();
                    intent.putExtra("filepath",filepath);
                } catch (NullPointerException e){
                    System.out.print("Null pointer exceptiion: image.");
                }
                try {
                    String description = treeData.getDescription();
                    intent.putExtra("description",description);
                } catch (NullPointerException e) {
                    System.out.print("Null pointer exception: description.");
                }
                intent.putExtra("title",title);
                intent.putExtra("subtitle",subTitle);
                startActivity(intent);
            }
        });


    }

    //
    //   Code for Marker Clustering
    //

    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spHall, ZOOM_LEVEL));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Tree>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        //mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setRenderer(new OwnIconRendered(this, mMap, mClusterManager));

        // Add cluster items (markers) to the cluster manager.
        addItems();

        mClusterManager.cluster();

    }

    private void addItems() {

        for (int i=0; i<trees.size();i++)
        {
            Tree temp = trees.get(i);
            mClusterManager.addItem(temp);
        }

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
            case R.id.tree_tour_option:
                Intent intent = new Intent(this,TreeTour.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
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

                        // temp hash map for single tree
                        HashMap<String, String> tree = new HashMap<>();

                        // add each child node to HashMap key => value
                        tree.put("longitude",longitude);
                        tree.put("latitude",latitude);
                        tree.put("scientific_name",sname);
                        tree.put("common_name",cname);
                        tree.put("id",id);

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

            for(int i=0; i < result.size(); i++) {

                HashMap treeData = result.get(i);

                // Convert to correct types
                String sName = (String) treeData.get("scientific_name");
                String cName = (String) treeData.get("common_name");
                double latitude = Double.parseDouble((String) treeData.get("latitude"));
                double longitude = Double.parseDouble((String) treeData.get("longitude"));
                int id = Integer.parseInt((String) treeData.get("id"));

                // create tree object from database attributes, add it to array of trees
                Tree tempTree = new Tree(latitude,longitude);
                tempTree.setTitle(cName);
                tempTree.setSnippet(sName);
                tempTree.setImage("tree_img_placeholder");
                tempTree.setDescription("This is a tree description.");
                trees.add(tempTree);


            }

            setUpClusterer();

        }

    }
}
