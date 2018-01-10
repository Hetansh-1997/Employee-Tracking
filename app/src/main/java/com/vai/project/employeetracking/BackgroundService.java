package com.vai.project.employeetracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hetansh Shah on 23-06-2017.
 */

public class BackgroundService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private DatabaseReference root;
    private long UPDATE_INTERVAL = 60 * 50;  /* 10 secs */
    private long FASTEST_INTERVAL = 60 * 20; /* 2 sec */
    private GoogleApiClient mGoogleClient;
    private Location mlocation;
    private LocationManager mlocationmanager;
    private LocationRequest mlocationrequest;
    private TextView tv_latitude;
    public static final String ACTION_LOCATION_BROADCAST = BackgroundService.class.getName() + "LocationBroadcast";

    /**
     * Creates an IntentService.   Invoked by your subclass's constructor.
     *66666666666666
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundService(String name) {
        super(name);
    }

    public BackgroundService() {
        super(null);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onLocationChanged(mlocation);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleClient.connect();
        mlocationmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent restartServiceIntent = new Intent(this, this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
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
            Toast.makeText(this, "No Location Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        sendBroadcastMessage(mlocationmanager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        root= FirebaseDatabase.getInstance().getReference().getRoot();
        Map<String,Object> map=new HashMap<String, Object>();
        TelephonyManager tel= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String DateToStr = tel.getDeviceId().toString();
        Toast.makeText(this, "Imei "+ DateToStr, Toast.LENGTH_SHORT).show();//This line is to delete
        root.updateChildren(map);
        DatabaseReference message_root=root.child(DateToStr);
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        String newlatitude=String.valueOf(mlocation.getLatitude());
        String newlongitude=String.valueOf(mlocation.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        Map<String,Object> result=new HashMap<String, Object>();
        result.put("Latitude",newlatitude);
        result.put("Longitude",newlongitude);
        FirebaseDatabase.getInstance().getReference().child("Root").child(DateToStr).updateChildren(result);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("Problem " ,"latitude passed "+DateToStr);
    }
    protected void startLocationUpdates() {
        mlocationrequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requeFstCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "add permissions for location", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mlocationrequest, this);
    }

    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra("extra_latitude", location.getLatitude());
            intent.putExtra("extra_longitude", location.getLongitude());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
