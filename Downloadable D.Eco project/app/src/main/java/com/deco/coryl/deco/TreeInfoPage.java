package com.deco.coryl.deco;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class TreeInfoPage extends AppCompatActivity {

    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tree_info_layout);

        Button backButton = (Button) findViewById(R.id.tree_info_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TreeInfoPage.this,TreeTour.class);
                startActivity(intent);
            }
        });

        ImageView imgView = (ImageView) findViewById(R.id.info_pic);
        TextView titleView = (TextView) findViewById(R.id.info_common_name);
        TextView snippetView = (TextView) findViewById(R.id.info_scientific_name);
        TextView descriptionView = (TextView) findViewById(R.id.info_description);

        Bundle dataBundle = getIntent().getExtras();

        String title = dataBundle.getString("title");
        String snippet = dataBundle.getString("subtitle");
        String filename = dataBundle.getString("filename");
        String description = dataBundle.getString("description");

        ImageDownloader imgDownloader = new ImageDownloader(imgView);
        imgDownloader.execute(filename);
        imgView.setImageBitmap(mBitmap);

        titleView.setText("Common Name: "+title);
        snippetView.setText("Scientific Name: "+snippet);
        descriptionView.setText("Description: \n"+description);
        descriptionView.setMovementMethod(new ScrollingMovementMethod());

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
