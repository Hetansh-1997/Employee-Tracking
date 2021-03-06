package com.vai.project.employeetracking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleClient;
    private Location mlocation;
    private LocationManager mlocationmanager;
    private LocationRequest mlocationrequest;
    TextView latitude, longitude;
    private long UPDATE_INTERVAL = 60*1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 60*1000; /* 2 sec */
    private DatabaseReference root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        startService(new Intent(this,BackgroundService.class ));
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mlocationmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "add permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        mlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        if (mlocation == null) {
            startLocationUpdates();
        }
        if (mlocation != null) {
//            latitude.setText(String.format("%.2f",String.valueOf(mlocation.getLatitude())));
  //          longitude.setText(String.format("%.2f",String.valueOf(mlocation.getLatitude())));
        } else {
            Toast.makeText(this, "No Loaction Found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        root= FirebaseDatabase.getInstance().getReference().getRoot();
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        String newlatitude=String.valueOf(mlocation.getLatitude());
        String newlongitude=String.valueOf(mlocation.getLongitude());
        int dot_latitude=newlatitude.indexOf(".");
        int dot_longitude=newlatitude.indexOf(".");
        latitude.setText(newlatitude.substring(0,dot_latitude+4));
        longitude.setText(newlongitude.substring(0,dot_longitude+4));
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        Map<String,Object> map=new HashMap<String, Object>();
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd  hh:mm:ss ");
        String DateToStr = format.format(curDate);
        root.updateChildren(map);
        DatabaseReference message_root=root.child(DateToStr);
        Map<String,Object> map_new=new HashMap<String, Object>();
        map_new.put("Latitude",newlatitude);
        map_new.put("Longitude",newlongitude);
        message_root.updateChildren(map_new);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }
    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }
    private boolean isLocationEnabled() {
        mlocationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mlocationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               mlocationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleClient != null) {
            mGoogleClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    protected void startLocationUpdates() {
        mlocationrequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "add permissions for location", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mlocationrequest, this);
    }

}
