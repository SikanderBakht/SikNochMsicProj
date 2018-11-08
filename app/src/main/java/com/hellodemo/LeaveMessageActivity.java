package com.hellodemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.hellodemo.fragment.MusicPlayerFragment;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

public class LeaveMessageActivity extends AppCompatActivity {
    public static final String KEY_SCREEN_UPLOADS = "uploads_screen";
    EditText et_leave_message;
    private String selectedArtistsIds = "";
    private String selectedGroupsIds = "";
    LoadingManager mainLoadingManger;
    // Create a new fragment and show upload screen on success or error of server request
    Fragment fragment = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_leave_message);
        et_leave_message = findViewById(R.id.et_leave_message);
        selectedArtistsIds = getIntent().getStringExtra(UIConstants.IntentExtras.SELECTED_ARTISTS_IDS_JSON_STR);
        selectedGroupsIds = getIntent().getStringExtra(UIConstants.IntentExtras.SELECTED_GROUPS_IDS_JSON_STR);

        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = findViewById(R.id.fragment_content1);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(this, fragmentContent, loadingIcon, loadingContainer);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }
    }

    public void onSendMusicButtonClicked(View view) {
       // if (et_leave_message.getText().toString().trim().isEmpty())return;
        MusicListItem musicListItem = getIntent().getParcelableExtra(UIConstants.IntentExtras.SELECTED_MUSIC);
        UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        String url = BuildConfig.BASE_URL + "/send_songs/send_to_groups_and_users";/*?upload_id=" + musicListItem.getId() +
                "&user_ids=" + selectedArtistsIds +
                "&group_ids=" + selectedGroupsIds +
                "&message_text="+et_leave_message.getText().toString();*/
        mainLoadingManger.showLoadingIcon();
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "send_contact", true);
        HashMap<String, String> params = new HashMap<>();
        params.put("upload_id",String.valueOf(musicListItem.getId()));
        params.put("user_ids",selectedArtistsIds);
        params.put("group_ids",selectedGroupsIds);
        params.put("message_text",et_leave_message.getText().toString());
        params.put("user_id",String.valueOf(mUserModel.getId()));
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mainLoadingManger.hideLoadingIcon();

                if (!response.optBoolean("error", true)) {
                    LayoutInflater li = LayoutInflater.from(LeaveMessageActivity.this);
                    View promptsView = li.inflate(R.layout.custom_music_popup, null);
                    ImageView alertImage = promptsView.findViewById(R.id.alert_image);
                    MemphisTextView subText = promptsView.findViewById(R.id.sub_text);

                    View ok = promptsView.findViewById(R.id.ok);

                    subText.setVisibility(View.GONE);
                    alertImage.setVisibility(View.VISIBLE);

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            LeaveMessageActivity.this);

                    // retrieve display dimensions
                    Rect displayRectangle = new Rect();
                    Window window = LeaveMessageActivity.this.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                    promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                    promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);
                    // create alert dialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();

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
//                    CustomToast.makeText(LeaveMessageActivity.this,response.optString("message"),Toast.LENGTH_LONG).show();
                    try {
                        fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_UPLOADS,
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if(response.optString("message").equals("Your shares are all being used. Free up shares by removing them from other users.")) {
                        LayoutInflater li = LayoutInflater.from(LeaveMessageActivity.this);
                        View promptsView = li.inflate(R.layout.custom_music_popup, null);

                        View ok = promptsView.findViewById(R.id.ok);
                        MemphisTextView text = promptsView.findViewById(R.id.main_heading);
                        ImageView alertImage = promptsView.findViewById(R.id.alert_image);
                        MemphisTextView subText = promptsView.findViewById(R.id.sub_text);

                        subText.setVisibility(View.GONE);
                        alertImage.setVisibility(View.VISIBLE);
                        text.setText("Your shares are all being used. Free up shares by removing them from other users.");
                        alertImage.setImageResource(R.drawable.no_shares);

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                LeaveMessageActivity.this);

                        // retrieve display dimensions
                        Rect displayRectangle = new Rect();
                        Window window = LeaveMessageActivity.this.getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

                        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
                        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();

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
//                    CustomToast.makeText(LeaveMessageActivity.this,response.optString("message"),Toast.LENGTH_LONG).show();
                        try {
                            fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_UPLOADS,
                                    null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        LayoutInflater li = LayoutInflater.from(LeaveMessageActivity.this);
                        View promptsView = li.inflate(R.layout.custom_music_loading_failed_popup, null);

                        View retry = promptsView.findViewById(R.id.Retry);
                        View cancel = promptsView.findViewById(R.id.Cancel);

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                LeaveMessageActivity.this);

                        // retrieve display dimensions
                        Rect displayRectangle = new Rect();
                        Window window = LeaveMessageActivity.this.getWindow();
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
                                alertDialog.hide();
                                onSendMusicButtonClicked(null);
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
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
//                    CustomToast.makeText(LeaveMessageActivity.this,response.optString("message"),Toast.LENGTH_LONG).show();
                        try {
                            fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_UPLOADS,
                                    null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
                Log.d("", "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainLoadingManger.hideLoadingIcon();
                Log.d("Error", "Error");
                try {
                    fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_UPLOADS,
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void onCancel(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
