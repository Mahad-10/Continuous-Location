package com.example.hamzalocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;

public class BroadcastService extends BroadcastReceiver {
    public static final String TAG = "BroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.d(TAG,"OnReceive: Error receiving geofence evenet..");
            Toast.makeText(context, "OnReceive: Error receiving geofence evenet..", Toast.LENGTH_SHORT).show();
        }
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList){
            Log.d(TAG,"onReceive" + geofence.getRequestId());
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType){
            case GEOFENCE_TRANSITION_ENTER:
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER","",MainActivity.class);
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT","",MainActivity.class);
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
