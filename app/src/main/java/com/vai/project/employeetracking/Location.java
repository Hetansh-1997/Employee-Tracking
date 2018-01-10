package com.vai.project.employeetracking;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Location extends AppCompatActivity {
    TextView tv_latitude,tv_longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            startService(new Intent(this,BackgroundService.class));
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

        }  else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE},99);
                startService(new Intent(this,BackgroundService.class));
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        }

         tv_latitude= (TextView) findViewById(R.id.latitude);
         tv_longitude= (TextView) findViewById(R.id.longitude);
        LocalBroadcastManager .getInstance(this).registerReceiver(new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra("extra_latitude", 0);
                double longitude = intent.getDoubleExtra("extra_longitude", 0);
                tv_latitude.setText(""+latitude);
                tv_longitude.setText(""+longitude);
            }
        }, new IntentFilter(BackgroundService.ACTION_LOCATION_BROADCAST)
        );



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}