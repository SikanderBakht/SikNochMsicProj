package com.hellodemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Mahnoor on 11/04/2018.
 */

public class Change_Password extends AppCompatActivity {

    private String TAG = "HelloDemoChangePasswordActivity";
    AppCompatEditText old_password,new_password, confirm_password;
    TextView save;
    String old_pwd,new_pwd,confirm_pwd;
    private UserModel mUserModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        old_password = findViewById(R.id.oldpassword);
        new_password = findViewById(R.id.newpassword);
        confirm_password = findViewById(R.id.confirmnewpwd);
        save=findViewById(R.id.save);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Change_Password.super.finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                old_pwd = old_password.getText().toString();
                new_pwd = new_password.getText().toString();
                confirm_pwd = confirm_password.getText().toString();


                final ProgressDialog pd = new ProgressDialog(Change_Password.this);
                pd.setMessage("Changing Password. Please Wait!");
                pd.show();


                String url = "";
                url = BuildConfig.BASE_URL + "/password/change";


                VolleyRequest volleyRequest = new VolleyRequest(getApplicationContext(), url, "Change_Password", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(mUserModel.getId()));
                params.put("old_password",old_pwd );
                params.put("new_password",new_pwd );
                params.put("new_password_confirmation",confirm_pwd );

                Log.v(TAG, "Change Password API Called:\n"
                        + "URL: " + url + "\n"
                        + "Params: " + params.toString());

                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("ChangePwd", "Change Password API Called:\n"
                                + response.toString());


                        if (!response.optBoolean("error", true)) {
                            CustomToast.makeText(Change_Password.this, "Successfully saved", Toast.LENGTH_LONG).show();
                            Change_Password.super.onBackPressed();
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        CustomToast.makeText(Change_Password.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                        Change_Password.super.onBackPressed();
                        pd.dismiss();
                    }
                });
            }
        });




    }
}
