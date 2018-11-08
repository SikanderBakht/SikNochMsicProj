package com.hellodemo.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hellodemo.AddFriendActivity;
import com.hellodemo.LeaveMessageActivity;
import com.hellodemo.LoginActivity;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.fragment.GroupsFragment;
import com.hellodemo.fragment.MessagesFragment;
import com.hellodemo.fragment.SettingsFragment;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by new user on 2/13/2018.
 */

public class VolleyRequest {
    private static final String TAG = "HelloDemoVolleyRequest";

    private static RequestQueue mRequestQueue;
    private final Context mContext;
    private boolean mSendAuthorizationHeader = false;
    private String mUrl;
    private String mTag;

    public VolleyRequest(Context context, String url, String tag, boolean sendAuthorizationHeader) {
        mContext = context;
        mUrl = url;
        mTag = tag;
        mSendAuthorizationHeader = sendAuthorizationHeader;
        mRequestQueue = getRequestQueue(mContext);
    }

    public static RequestQueue getRequestQueue(Context mContext) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    private Map<String, String> getRequestHeaders(Request request) {
        Map<String, String> headers = new HashMap<>();

        String accessToken = Utils.getAccessToken(mContext);
        if (mSendAuthorizationHeader && accessToken != null && !TextUtils.isEmpty(accessToken)) {
            String auth = "Bearer " + accessToken;
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", auth);
        }

        // this will make sure we only get json response...
        headers.put("Accept", "application/json");

        if (request.getMethod() == Request.Method.POST) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        return headers;
    }

    public void requestServer(int method, HashMap<String, String> params, Response.Listener<JSONObject> successCallback, Response.ErrorListener failureCallback) {
        HelloResponseListener mResponseListener = new HelloResponseListener(successCallback);
        ErrorResponseListener mErrorListener = new ErrorResponseListener(failureCallback);
        HelloStringRequest mStringRequest = new HelloStringRequest(method, mUrl, mResponseListener, mErrorListener);
        mStringRequest.setTag(mTag);
        if (params != null) mStringRequest.setmParams(params);
        mStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(mStringRequest);
    }


    private class HelloResponseListener implements Response.Listener<String> {

        private final Response.Listener<JSONObject> mCallback;

        HelloResponseListener(Response.Listener<JSONObject> callback) {
            mCallback = callback;
        }

        @Override
        public void onResponse(String response) {

            Log.v(TAG, "API URL: " + mUrl + "\nResponse: "+ response);

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (mCallback != null)
                    mCallback.onResponse(jsonObject);
                if (jsonObject.optBoolean("error", false)) {
                    CustomToast.makeText(mContext, jsonObject.optString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ErrorResponseListener implements Response.ErrorListener {
        private final Response.ErrorListener mCallback;

        ErrorResponseListener(Response.ErrorListener callback) {
            mCallback = callback;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "API URL: " + mUrl
                    + "\nError Response: " + error.toString()
                    + "\nError Response: " + error.getMessage()
                    + "\nError Response: " + error.getLocalizedMessage()
                    + "\nError Response: " + error.networkResponse
            );


            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                if(mContext instanceof MainActivity) {
                    android.support.v4.app.Fragment currentFragment = ((MainActivity) mContext).getSupportFragmentManager()
                            .findFragmentById(R.id.flContent);
                    if(currentFragment instanceof SettingsFragment || currentFragment instanceof MessagesFragment || currentFragment instanceof GroupsFragment) {
                        CustomToast.makeText(mContext, "Connection Wasn't Established", Toast.LENGTH_LONG).show();

                    } else {
                        LayoutInflater li = LayoutInflater.from(mContext);
                        View promptsView = li.inflate(R.layout.custom_music_loading_failed_popup, null);

                        View retry = promptsView.findViewById(R.id.Retry);
                        View cancel = promptsView.findViewById(R.id.Cancel);

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                mContext);

                        // retrieve display dimensions
                        Rect displayRectangle = new Rect();
                        Window window = ((Activity)mContext).getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((Activity)mContext).finish();
                                Intent i = new Intent(mContext, MainActivity.class);
                                ((Activity)mContext).startActivity(i);
                                alertDialog.hide();
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.hide();
                            }
                        });
                        // show it
                        alertDialog.show();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                alertDialog.hide();
                            }
                        });
                    }
                } else {
                    if(!(mContext instanceof AddFriendActivity)) {
                        CustomToast.makeText(mContext, "Connection Wasn't Established", Toast.LENGTH_LONG).show();
                    } else {
                        LayoutInflater li = LayoutInflater.from(mContext);
                        View promptsView = li.inflate(R.layout.custom_music_popup, null);

                        View ok = promptsView.findViewById(R.id.ok);
                        View alertImage = promptsView.findViewById(R.id.alert_image);
                        MemphisTextView mainHeading = promptsView.findViewById(R.id.main_heading);
                        MemphisTextView subText = promptsView.findViewById(R.id.sub_text);

                        alertImage.setVisibility(View.GONE);
                        mainHeading.setText("Friend Added");

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                mContext);

                        // retrieve display dimensions
                        Rect displayRectangle = new Rect();
                        Window window = ((Activity)mContext).getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.3f));
                        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.1f));

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((Activity)mContext).finish();
                                Intent i = new Intent(mContext, MainActivity.class);
                                ((Activity)mContext).startActivity(i);
                                alertDialog.hide();
                            }
                        });
                        // show it
                        alertDialog.show();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                alertDialog.hide();
                            }
                        });
                    }
                }

            } else if (error instanceof AuthFailureError) {
                Log.v(TAG, "Auth Error");
//                CustomToast.makeText(mContext, "Authentication Failed!", Toast.LENGTH_LONG).show();
                UserSharedPreferences.deleteSharedPreferences(mContext);
                mContext.startActivity(new Intent(mContext, LoginActivity.class));

            } else if (error instanceof ServerError) {

                Log.v(TAG, "Server Error");
                //server error
//                CustomToast.makeText(mContext, "Server Error!", Toast.LENGTH_LONG).show();

            }

            if (mCallback != null) {
                mCallback.onErrorResponse(error);
            }

            if (error != null && error.networkResponse != null) {

                Log.v(TAG, error.networkResponse.statusCode + " Error");
//                CustomToast.makeText(mContext, "Error : " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
            } else {
                Log.v(TAG, "Network Error");
//                CustomToast.makeText(mContext, "Network Error ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class HelloStringRequest extends StringRequest {
        private Map<String, String> mParams = new HashMap<>();

        HelloStringRequest(int method, String mUrl, HelloResponseListener mResponseListener, ErrorResponseListener mErrorListener) {
            super(method, mUrl, mResponseListener, mErrorListener);
        }

        public void setmParams(HashMap<String, String> mParams) {
            this.mParams = mParams;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return getRequestHeaders(this);
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return mParams;
        }
    }


}