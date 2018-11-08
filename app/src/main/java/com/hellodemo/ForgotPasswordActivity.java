package com.hellodemo;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ForgotPasswordActivity extends AppCompatActivity {

    AppCompatEditText email;
    AppCompatTextView reset;
    // manages loading icons...
    LoadingManager mainLoadingManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        email = findViewById(R.id.email);
        reset = findViewById(R.id.reset);


        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = findViewById(R.id.fragment_content);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = findViewById(R.id.loading_container);
        // setup loading icon...
        mainLoadingManger = new LoadingManager(this, fragmentContent, loadingIcon, loadingContainer);
        mainLoadingManger.hideLoadingIcon();

        setUpClickListeners();
    }

    private void setUpClickListeners() {
        reset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                callResetPasswordAPI();
            }
        });
    }


    private void callResetPasswordAPI() {

        if (email.getText().toString().isEmpty()) {

            CustomToast.makeText(this, "Please enter your email address", Toast.LENGTH_LONG).show();
            return;
        }

        mainLoadingManger.showLoadingIcon();
        String url = BuildConfig.BASE_URL + "/get_password_reset_mail";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "reset", false);

        HashMap<String, String> params = new HashMap<>();
        params.put("email", email.getText().toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                mainLoadingManger.hideLoadingIcon();

                if (!response.optBoolean("error", true)) {

                    try {
                        CustomToast.makeText(ForgotPasswordActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainLoadingManger.hideLoadingIcon();
                Log.d("", "");
            }
        });
    }
}

