package com.deco.coryl.deco;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    // URL_STRING is the url where the restful webpage is located
    String URL_STRING = "http://ec2-52-88-66-54.us-west-2.compute.amazonaws.com/index.php";
    // Tag name for debugging
    private String TAG = MainActivity.class.getSimpleName();

    // Array List of tree data
    ArrayList<HashMap<String,String>> treeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // (re)Initialize the list of Trees on App Create
        treeList = new ArrayList<>();

        // Execute the AsyncTask to fetch tree data
        new GetTrees().execute();
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

        // Add a marker in Drury Circle and move the camera
        LatLng spHall = new LatLng(37.218431, -93.285545);
        // Center the view on Springfield Hall
        mMap.moveCamera(CameraUpdateFactory.newLatLng(spHall));
        // Zoom in for relative view
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        // Specify the type of Google Map being displayed
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

                // create tree object from database attributes
                Tree tempTree = new Tree(cName,sName,latitude,longitude,id);

                // Create a lat long object for the tree since MarkerOptions takes an input of LatLong object for position
                LatLng treeLoc = new LatLng(latitude,longitude);

                // Add tree markers to the map
                mMap.addMarker(new MarkerOptions().position(treeLoc).title(cName).snippet(sName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            }

        }

    }
}
