package com.deco.coryl.deco;

/**
 * Created by coryl on 2/26/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class CustomInfoWindowGoogleMaps implements GoogleMap.InfoWindowAdapter {

    private Context context;
    Bitmap mBitmap;

    public CustomInfoWindowGoogleMaps(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_info_window, null);

        TextView name_tv = (TextView) view.findViewById(R.id.name);
        TextView scientific_name_tv = (TextView) view.findViewById(R.id.scientific_name);
        ImageView img = (ImageView) view.findViewById(R.id.pic);

        TextView description_tv = (TextView) view.findViewById(R.id.description);

        name_tv.setText(marker.getTitle());
        scientific_name_tv.setText(marker.getSnippet());

        Tree infoWindowData = (Tree) marker.getTag();

        //TODO: fix bug that makes the previous marker's image show up in the current info window
        try {
            ImageDownloader imgDownloader = new ImageDownloader(img);
            imgDownloader.execute(infoWindowData.getImage());
            img.setImageBitmap(mBitmap);
        } catch(NullPointerException e) {
            System.out.print("Null pointer exception: image.");
        }

        try{
            description_tv.setText(infoWindowData.getDescription());
        } catch(NullPointerException e) {
            description_tv.setText(R.string.no_description);
            System.out.print("Null pointer exception: description.");
        }

        return view;
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... url) {
            String mUrl = url[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(mUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                Log.e(".TreeTour", "MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(".TreeTour", "IOException: " + e.getMessage());
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result){
            mBitmap = result;
        }
    }

}
