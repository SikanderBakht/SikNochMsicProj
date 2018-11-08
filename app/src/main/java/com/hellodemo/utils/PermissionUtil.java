package com.hellodemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Muzammil on 10/17/2016.
 * com.algotrick.quranforeveryone.utils
 */

public class PermissionUtil {

    public static final int REQUEST_CODE = (5121472 & 0xffff);

    public static boolean checkPermission(Context context, String permission){
        return  (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }
    public  static void requestPermission(final Activity context, final String permission, final String msg){
        if (!PermissionUtil.checkPermission(context, permission)){
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission Required");
                alertBuilder.setMessage(msg);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(context,
                                new String[]{permission},
                                REQUEST_CODE);
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(context,
                        new String[]{permission},
                        REQUEST_CODE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
