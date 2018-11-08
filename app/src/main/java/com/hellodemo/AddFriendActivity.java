package com.hellodemo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.qrcode.barcode.BarcodeCaptureActivity;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.PermissionUtil;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AddFriendActivity extends AppCompatActivity {


    private String TAG = "HelloDemoAddFriendActivity";
    private static final int BARCODE_READER_REQUEST_CODE = 10001;
    private ImageView back_icon, qr_code_image;
    private TextView scan_qr_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        back_icon = findViewById(R.id.back_icon);
        qr_code_image = findViewById(R.id.qr_code_image);
        scan_qr_code = findViewById(R.id.scan_qr_code);

        onLoadWebService();

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        scan_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionAndStartScanning();
            }
        });
    }

    private void onLoadWebService() {
        UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        String url = BuildConfig.BASE_URL + "/qrcodes/get_user_qrcode?user_id=" + mUserModel.getId();

        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                    String qr_code = response.optString("message");
                    Picasso.with(AddFriendActivity.this).load(qr_code).into(qr_code_image);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void checkPermissionAndStartScanning() {
        if (PermissionUtil.checkPermission(this, Manifest.permission.CAMERA)) {
            startScannerActivity();
        } else {
            PermissionUtil.requestPermission(this, Manifest.permission.CAMERA, "Camera Permission is Required to Scan QR Code");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    startScannerActivity();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void startScannerActivity() {
//        Intent intent = new Intent(AddFriendActivity.this, QrScannerActivity.class);
//        startActivity(intent);
        Intent intent = new Intent(AddFriendActivity.this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = (Barcode) data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
//                    Toast.makeText(this, barcode.displayValue, Toast.LENGTH_SHORT).show();
                    processQrCodeData(barcode.displayValue);
                } else {
                    CustomToast.makeText(this, "No Barcode found", Toast.LENGTH_SHORT).show();
                }
            } else
                Log.v(TAG, "Barcode Error: " + CommonStatusCodes.getStatusCodeString(resultCode));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQrCodeData(String data) {
        Log.v(TAG, "Code Data: " + data); // Prints scan results
        String qrCodeUserID = null;
        try {
            JSONObject jObject = new JSONObject(data);
            if (jObject.has("user_id")) {
                qrCodeUserID = jObject.getString("user_id");
            }
        } catch (JSONException e) {
            CustomToast.makeText(this, "QR Code is invalid!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qrCodeUserID == null) {
            CustomToast.makeText(this, "QR Code is invalid!", Toast.LENGTH_SHORT).show();
            return;
        }


        UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);

        if (qrCodeUserID.equalsIgnoreCase(mUserModel.getId()+"")) {
            CustomToast.makeText(this, "You have scanned your own QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BuildConfig.BASE_URL + "/friends/add_friend_for_mobile?user1_id="
                + mUserModel.getId() + "&user2_id=" + qrCodeUserID;

        Log.v(TAG, "4753 Add Friends API Called:\n"
                + "URL: " + url);
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "add_by_qr", true);
        final String finalQrCodeUserID = qrCodeUserID;
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "4753 Add Friends API Response:\n"
                        + response.toString());

                if (!response.optBoolean("error", true)) {
                    //CustomToast.makeText(AddFriendActivity.this, "Friend Added", Toast.LENGTH_LONG).show();
                    LayoutInflater li = LayoutInflater.from(AddFriendActivity.this);
                    View promptsView = li.inflate(R.layout.custom_music_popup, null);

                    View ok = promptsView.findViewById(R.id.ok);
                    MemphisTextView text = promptsView.findViewById(R.id.main_heading);
                    ImageView alertImage = promptsView.findViewById(R.id.alert_image);
                    MemphisTextView subText = promptsView.findViewById(R.id.sub_text);

                    subText.setVisibility(View.GONE);
                    alertImage.setVisibility(View.VISIBLE);
                    text.setText("You are now Friends with \n Username");
                    alertImage.setImageResource(R.drawable.no_shares);

                    final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                            AddFriendActivity.this);

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = AddFriendActivity.this.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.5f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.1f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final android.app.AlertDialog alertDialog = alertDialogBuilder.create();

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                    // show it
                    alertDialog.show();
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            finish();
                        }
                    });
                    //finish();
                }
                Log.d("", "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");

                // if friend can not be added because of network issue...
                // we will add the qr code to a list to be added as friend later...
                UserSharedPreferences.addUserIDToBeAddedAsFriend(AddFriendActivity.this, finalQrCodeUserID);
            }
        });
    }

}
