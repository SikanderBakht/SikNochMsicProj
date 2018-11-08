package com.hellodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisCheckBox;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String ACTIVITY_NAME = "login";

    private String remembered_email;
    AppCompatEditText email, password;
    AppCompatTextView forgot_password, login, facebook_login, do_not_have_account;
    MemphisCheckBox remember_me;
    // manages loading icons...
    LoadingManager mainLoadingManger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgot_password = findViewById(R.id.forgot_password);
        login = findViewById(R.id.login);
        facebook_login = findViewById(R.id.facebook_login);
        do_not_have_account = findViewById(R.id.do_not_have_account);
        remember_me = findViewById(R.id.remember_me);

        remembered_email = UserSharedPreferences.getString(this, UserSharedPreferences.USER_EMAIL);
        if(remembered_email != null) {
            email.setText(remembered_email);
        }

        View topview = findViewById(R.id.topview);
        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = findViewById(R.id.fragment_content);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = findViewById(R.id.loading_container);
        // setup loading icon...
        View view=null;// = Activity.getCurrentFocus();
        mainLoadingManger = new LoadingManager(this,  fragmentContent, loadingIcon, loadingContainer);
        mainLoadingManger.hideLoadingIcon();

        setSignupSpan();
        clickListener();
    }

    private void clickListener() {
        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
        forgot_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void setSignupSpan() {
        SpannableString ss = new SpannableString(getString(R.string.don_t_have_an_account_sing_up));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 22, getString(R.string.don_t_have_an_account_sing_up).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        do_not_have_account.setText(ss);
        do_not_have_account.setMovementMethod(LinkMovementMethod.getInstance());
        do_not_have_account.setHighlightColor(Utils.getColor(this, R.color.colorPrimary));
    }

    private void loginUser() {

        if(email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){

            CustomToast.makeText(this, "All Field Are Required", Toast.LENGTH_LONG).show();
            return;
        }

        mainLoadingManger.showLoadingIcon();
        String url = BuildConfig.BASE_URL + "/login";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "register", false);

        HashMap<String, String> params = new HashMap<>();
        params.put("email",email.getText().toString());
        params.put("password",password.getText().toString());
        params.put("password",password.getText().toString());
        params.put("android_token",FirebaseInstanceId.getInstance().getToken());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                mainLoadingManger.hideLoadingIcon();
                if (!response.optBoolean("error", true)){
                    UserModel userModel = Utils.parseJson(response.optJSONObject("data").toString(), UserModel.class);
                    UserSharedPreferences.saveString(LoginActivity.this, UserSharedPreferences.USER_MODEL,
                            response.optJSONObject("data").toString());
                    if(remember_me.isChecked()) {
                        UserSharedPreferences.saveString(LoginActivity.this, UserSharedPreferences.USER_EMAIL,
                                userModel.getEmail());
                    }
                    UserSharedPreferences.saveBoolean(LoginActivity.this, UserSharedPreferences.USER_LOGGED_IN, true);
                    if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                        Registration registration = Registration.create().withEmail(email.getText().toString());
                        Intercom.client().registerIdentifiedUser(registration);
                    }
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("activity_name", ACTIVITY_NAME);
                    LoginActivity.this.startActivity(i);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainLoadingManger.hideLoadingIcon();
                Log.d("","");
            }
        });
    }
}

