package com.potatomasterextreme.personnel.infrastructure;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsManager {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_PHONE_STATE
    };
    public static final int MY_PERMISSIONS_REQUEST = 1;


    public static void checkForContactPermissions(Activity activity) {
        List<String> askForPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                askForPermissions.add(permission);
            }
        }
        askForPermissions.toArray();
        if (askForPermissions.size() > 0) {
            // request permission..
            String[] array = new String[askForPermissions.size()];
            askForPermissions.toArray(array);
            ActivityCompat.requestPermissions(activity, array, MY_PERMISSIONS_REQUEST);
        }
    }
}
