package com.hellodemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers reset via email/password.
 */
public class RegisterActivity extends AppCompatActivity {
    // manages loading icons...
    LoadingManager mainLoadingManger;
    AppCompatTextView register, facebook_register, already_have_account;
    AppCompatEditText username, email, password, confirm_password, redeem_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        redeem_code = findViewById(R.id.redeem_code);
        register = findViewById(R.id.login);
        facebook_register = findViewById(R.id.facebook_login);
        already_have_account = findViewById(R.id.already_have_account);


        View topview = findViewById(R.id.topview);
        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = findViewById(R.id.fragment_content);

        //loading icon...
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = findViewById(R.id.loading_container);

        // setup loading icon...
        View view = null;// = Activity.getCurrentFocus();
        mainLoadingManger = new LoadingManager(this, fragmentContent, loadingIcon, loadingContainer);
        mainLoadingManger.hideLoadingIcon();//
        setLoginSpan();
        clickListeners();
    }

    private void setLoginSpan() {
        SpannableString ss = new SpannableString(getString(R.string.already_have_an_account_login));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 25, getString(R.string.already_have_an_account_login).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        already_have_account.setText(ss);
        already_have_account.setMovementMethod(LinkMovementMethod.getInstance());
        already_have_account.setHighlightColor(Utils.getColor(this, R.color.colorPrimary));
    }

    private void clickListeners() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {

        if (username.getText().toString().isEmpty() || email.getText().toString().isEmpty() || password.getText().toString().isEmpty() || confirm_password.getText().toString().isEmpty()) {

            CustomToast.makeText(RegisterActivity.this, "Please fill the required fields.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.getText().toString().equals(confirm_password.getText().toString())) {

            CustomToast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.getText().toString().length()<6) {

            CustomToast.makeText(RegisterActivity.this, "Password should be 6 characters or more", Toast.LENGTH_LONG).show();
            return;
        }

        mainLoadingManger.showLoadingIcon();
        String url = BuildConfig.BASE_URL + "/register";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "register", false);

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username.getText().toString());
        params.put("email", email.getText().toString());
        params.put("password", password.getText().toString());
        params.put("password_confirmation", confirm_password.getText().toString());
        params.put("redeem_code", redeem_code.getText().toString());
        params.put("android_token", FirebaseInstanceId.getInstance().getToken());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                mainLoadingManger.hideLoadingIcon();
                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(RegisterActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                    UserModel userModel = Utils.parseJson(response.optJSONObject("data").toString(), UserModel.class);
                    UserSharedPreferences.saveString(RegisterActivity.this, UserSharedPreferences.USER_MODEL,
                            response.optJSONObject("data").toString());
                    UserSharedPreferences.saveBoolean(RegisterActivity.this, UserSharedPreferences.USER_LOGGED_IN, true);
                    if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                        Registration registration = Registration.create().withEmail(email.getText().toString());
                        Intercom.client().registerIdentifiedUser(registration);
                    }
                    RegisterActivity.this.startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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

