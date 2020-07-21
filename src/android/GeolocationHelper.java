package com.megster.cordova.ble.central;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.cordova.CordovaPlugin;

import static android.content.ContentValues.TAG;

/**
 * Created by algoritma on 02/07/2019.
 */

public class GeolocationHelper {

    public static void enableGeolocation(final CordovaPlugin command, final int requestCode) {

        if (Build.VERSION.SDK_INT >= 28) {
            // geolocalizzazione necessaria da Android 9 in poi

            final Context context = command.cordova.getActivity();
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                command.onActivityResult(requestCode, Activity.RESULT_OK, null);
            } else {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10 * 1000);
                locationRequest.setFastestInterval(2 * 1000);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
                LocationSettingsRequest locationSettingsRequest = builder.build();
                //**************************
                builder.setAlwaysShow(true); //this is the key ingredient
                //**************************
                SettingsClient settingsClient = LocationServices.getSettingsClient(context);
                settingsClient
                        .checkLocationSettings(locationSettingsRequest)
                        .addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                //  GPS is already enable, callback GPS status through listener
                                command.onActivityResult(requestCode, Activity.RESULT_OK, null);
                            }
                        })
                        .addOnFailureListener((Activity) context, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                int statusCode = ((ApiException) e).getStatusCode();
                                switch (statusCode) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        try {
                                            // Show the dialog by calling startResolutionForResult(), and check the
                                            // result in onActivityResult().
                                            ResolvableApiException rae = (ResolvableApiException) e;
                                            //rae.startResolutionForResult((Activity) context, requestCode);

                                            command.cordova.setActivityResultCallback(command);
                                            rae.startResolutionForResult(command.cordova.getActivity(), requestCode);

                                        } catch (IntentSender.SendIntentException sie) {
                                            Log.i(TAG, "PendingIntent unable to execute request.");
                                            command.onActivityResult(requestCode, Activity.RESULT_CANCELED, null);
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                                "fixed here. Fix in Settings.";
                                        Log.e(TAG, errorMessage);
                                        command.onActivityResult(requestCode, Activity.RESULT_CANCELED, null);
                                }
                            }
                        });
            }
        } else {
            // prima di Android 9 non è necessaria la geolocalizzazione
            command.onActivityResult(requestCode, Activity.RESULT_OK, null);
        }
    }

}
