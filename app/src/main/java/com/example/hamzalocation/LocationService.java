package com.example.hamzalocation;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pk.codebase.requests.HttpError;
import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;

public class LocationService extends Service {

    private static final String TAG = "LocationSErvice";

    public LocationService() {
    }


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("Location_Update", latitude + "," + longitude);
                sendLocation(latitude,longitude);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationUpdates() {
        String channel_Id = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resuultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resuultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), channel_Id
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null &&
                    notificationManager.getNotificationChannel(channel_Id) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channel_Id, "Location Service ",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.Location_Service_ID,builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null){
            String action = intent.getAction();
            if (action != null){
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationUpdates();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void sendLocation(final double latitude,final double longitude) {
        List<Object> args = new ArrayList<>();
        args.add(latitude);
        args.add(longitude);

        HttpRequest request = new HttpRequest();
        request.setOnResponseListener(new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(HttpResponse response) {
                Toast.makeText(LocationService.this, "Success", Toast.LENGTH_SHORT).show();
                Toast.makeText(LocationService.this, String.valueOf(latitude) + ", " + String.valueOf(longitude),
                      Toast.LENGTH_SHORT).show();
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpError error) {
                Toast.makeText(LocationService.this, "Fail", Toast.LENGTH_SHORT).show();
            }
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", "pk.codebase.hamza.location");
        payload.put("args", args);

        request.post("http://codebase.pk:8080/publish", payload);
    }
}
