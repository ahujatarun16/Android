package com.tarun.mycameraapp;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.safetynet.SafetyNetApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    Button btnTakePhoto,btnSavePhoto,btnSharePhoto,btnSetWallpaper;
    ImageView ivPhoto;
    Bitmap photo;
    TextView tvLocationLabel;
    GoogleApiClient mLocationClient;
    Location mLastLocation;
    String label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind
        btnSavePhoto=(Button)findViewById(R.id.btnSavePhoto);
        btnSetWallpaper=(Button)findViewById(R.id.btnSetWallpaper);
        btnSharePhoto=(Button)findViewById(R.id.btnSharePhoto);
        btnTakePhoto=(Button)findViewById(R.id.btnTakePhoto);
        ivPhoto=(ImageView)findViewById(R.id.ivPhoto);
        tvLocationLabel=(TextView)findViewById(R.id.tvLocationLabel);

        //google api client
        GoogleApiClient.Builder builder=new GoogleApiClient.Builder(this);
        builder.addApi(LocationServices.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        mLocationClient=builder.build();

        tvLocationLabel.setText("Finding the Current Location...");
        //button disable
        btnSetWallpaper.setEnabled(false);
        btnSharePhoto.setEnabled(false);
        btnSavePhoto.setEnabled(false);

        //onclick take photo
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,100);
            }
        });
        //onclick set wallaper
        btnSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperManager wm=WallpaperManager.getInstance(getApplicationContext());
                try {
                    wm.setBitmap(photo);
                    Snackbar.make(view,"Wallpaper Set Successful",Snackbar.LENGTH_SHORT).show();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        btnSharePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("image/bitmap");
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "", null);
                i.putExtra(Intent.EXTRA_STREAM,Uri.parse(path));
                i.putExtra(Intent.EXTRA_TEXT,tvLocationLabel.getText().toString());
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });
        btnSavePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state= Environment.getExternalStorageState();
                if(Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)){

                    File root=Environment.getExternalStorageDirectory();
                    File dir=new File(root.getAbsolutePath()+"/My Camera App");

                    if(!dir.exists()){
                        dir.mkdir();
                    }
                    try {
                        File outputFile = File.createTempFile("MCA", ".png", dir);
                        FileOutputStream out = new FileOutputStream(outputFile);
                        photo.compress(Bitmap.CompressFormat.PNG, 90, out);
                        Snackbar.make(view,"Image Saved Successful",Snackbar.LENGTH_LONG).show();
                        out.close();
                    } catch (IOException e) {
                        Snackbar.make(view,"Image cannot be saved",Snackbar.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    //connect google api
    @Override
    protected void onStart() {
        super.onStart();
        if(mLocationClient!=null){
            mLocationClient.connect();
        }
    }

    //activity camera result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==100 && data!=null){

            photo=(Bitmap)data.getExtras().get("data");
            ivPhoto.setImageBitmap(photo);
            tvLocationLabel.setText(label);
            btnSetWallpaper.setEnabled(true);
            btnSharePhoto.setEnabled(true);
            btnSavePhoto.setEnabled(true);
        }
    }

    //on connected get lat & lng and find address
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if(mLastLocation!=null){
            double latitude=mLastLocation.getLatitude();
            double longitude=mLastLocation.getLongitude();

            Geocoder geocoder=new Geocoder(this, Locale.ENGLISH);
            try{
                List<Address> addresses=geocoder.getFromLocation(latitude,longitude,1);
                if(addresses!=null){
                    Address fetchedAddresses=addresses.get(0);
                    label="Location: "+fetchedAddresses.getFeatureName()+", "+fetchedAddresses.getSubLocality()+", "+fetchedAddresses.getLocality()+", "+fetchedAddresses.getPostalCode()+", "+fetchedAddresses.getAdminArea()+", "+fetchedAddresses.getCountryName();
                }
                else{
                    tvLocationLabel.setText("Location Not Found");
                    Snackbar.make(findViewById(android.R.id.content),"Location Not Found",Snackbar.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Snackbar.make(findViewById(android.R.id.content),"Unable to Get Location",Snackbar.LENGTH_SHORT).show();
                tvLocationLabel.setText("Location Not Found");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(findViewById(android.R.id.content),"Connection Suspended",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(findViewById(android.R.id.content),"Connection Failed",Snackbar.LENGTH_SHORT).show();
    }
}
