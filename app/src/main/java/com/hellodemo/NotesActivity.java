package com.hellodemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.adapter.NotesAdapter;
import com.hellodemo.models.ChatNote;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NotesActivity extends AppCompatActivity {

    private String TAG = "HelloDemoNotesActivity";

    // DATA KEYS
    public static final String KEY_MUSIC_UPLOAD_ID = "music_upload_id";
    public static final String KEY_PACKAGE_ID = "package_id";
    public static final String KEY_GROUP_ID = "group_id";


    // ACTION KEYS
    public static final String ACTION_OPEN_NOTES_BY_SONG = "notes_by_song";
    public static final String ACTION_OPEN_NOTES_BY_PACKAGE = "notes_by_package";
    public static final String ACTION_OPEN_NOTES_BY_GROUP = "notes_by_group";


    private UserModel mUserModel;
    private String mAction = null;

    private long mMusicId, packageID, groupID;
    private ArrayList<ChatNote> chatNotes = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private NotesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);


        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // close if no action is found...
        if (getIntent() == null && getIntent().getAction() == null) {
            CustomToast.makeText(this, "Can not open notes", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "No Action Provided. finishing!");
            finish();
        }

        // get action...
        mAction = getIntent().getAction();
        Log.v(TAG, "Chat screen action: " + mAction);


        switch (mAction) {
            case ACTION_OPEN_NOTES_BY_SONG:
                mMusicId = getIntent().getLongExtra(KEY_MUSIC_UPLOAD_ID, -1);
                Log.v(TAG, "onCreate mMusicId value: " + mMusicId);

                if (mMusicId == -1) {
                    CustomToast.makeText(this, "Can not open notes", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "music upload id not provided.");
                    finish();
                }

                break;
            case ACTION_OPEN_NOTES_BY_PACKAGE:
                packageID = getIntent().getLongExtra(KEY_PACKAGE_ID, -1);
                Log.v(TAG, "onCreate packageID value: " + packageID);

                if (packageID == -1) {
                    CustomToast.makeText(this, "Can not open notes", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "music upload id not provided.");
                    finish();
                }
                break;
            case ACTION_OPEN_NOTES_BY_GROUP:
                groupID = getIntent().getLongExtra(KEY_GROUP_ID, -1);
                Log.v(TAG, "onCreate groupID value: " + groupID);

                if (groupID == -1) {
                    CustomToast.makeText(this, "Can not open notes", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "music upload id not provided.");
                    finish();
                }
                break;
            default:
                CustomToast.makeText(this, "Can not open notes", Toast.LENGTH_SHORT).show();
                Log.v(TAG, "Action Not Recognized.");
                finish();
                break;
        }

        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatNotes.size() == 0) {
                    finish();
                } else {
                    onBackPressed();
                }
            }
        });
        setRecyclerView();
        onLoadWebservice();
    }

    private void onLoadWebservice() {

        String url = "";

        switch (mAction) {
            case ACTION_OPEN_NOTES_BY_SONG:

                url = BuildConfig.BASE_URL + "/notes/get_notes" +
                        "?upload_id=" + mMusicId
                        + "&user_id=" + mUserModel.getId();

                break;
            case ACTION_OPEN_NOTES_BY_PACKAGE:

                url = BuildConfig.BASE_URL + "/notes/get_notes" +
                        "?package_id=" + packageID
                        + "&user_id=" + mUserModel.getId();

                break;
            case ACTION_OPEN_NOTES_BY_GROUP:

                url = BuildConfig.BASE_URL + "/notes/get_group_notes"
                        + "?group_id=" + groupID
                        + "&user_id=" + mUserModel.getId();

                break;
        }


        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);


        Log.v(TAG, "4557 Get Notes API Called:\n"
                + "URL: " + url);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "4557 Get Notes API Response:\n"
                        + response.toString());
                if (!response.optBoolean("error", true)) {
                    if (response.optJSONObject("message") != null) {
                        String notesString = response.optJSONObject("message").optString("notes");
                        String[] notes = notesString.split("\n");

                        for (String note : notes) {
                            chatNotes.add(new ChatNote(note));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(chatNotes.size() - 1);
                    mAdapter.focusPosition = chatNotes.size() - 1;
                    Log.d("", "");
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
        if (chatNotes.size() == 0) {
            NotesActivity.super.onBackPressed();
        } else {
            onSaveNotes();
        }
    }

    private void onSaveNotes() {
//        mAdapter.notifyDataSetChanged();
        final ProgressDialog pd = new ProgressDialog(NotesActivity.this);
        pd.setMessage("Saving Notes. Please Wait!");
        pd.show();


        String url = "";

        switch (mAction) {
            case ACTION_OPEN_NOTES_BY_SONG:
                url = BuildConfig.BASE_URL + "/notes/save_note";

                break;
            case ACTION_OPEN_NOTES_BY_PACKAGE:
                url = BuildConfig.BASE_URL + "/notes/save_note";

                break;
            case ACTION_OPEN_NOTES_BY_GROUP:
                url = BuildConfig.BASE_URL + "/notes/save_group_note";

                break;
        }

        VolleyRequest volleyRequest = new VolleyRequest(this, url, "save_note", true);
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(mUserModel.getId()));
        params.put("notes", constructNotesString());

        Log.v(TAG + "userid", String.valueOf(mUserModel.getId()));
        Log.v(TAG + "notes", constructNotesString());
        switch (mAction) {
            case ACTION_OPEN_NOTES_BY_SONG:
                params.put("upload_id", String.valueOf(mMusicId));

                break;
            case ACTION_OPEN_NOTES_BY_PACKAGE:
                params.put("package_id", String.valueOf(packageID));

                break;
            case ACTION_OPEN_NOTES_BY_GROUP:
                params.put("group_id", String.valueOf(groupID));

                break;
        }

        Log.v(TAG, "87667 Save Notes API Called:\n"
                + "URL: " + url + "\n"
                + "Params: " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "87667 Save Notes API Response:\n"
                        + response.toString());

                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(NotesActivity.this, "Successfully saved", Toast.LENGTH_LONG).show();
                    NotesActivity.super.onBackPressed();
                }
                pd.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //String value = new String(error.networkResponse.data);
                //Log.d("", value);
                CustomToast.makeText(NotesActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                NotesActivity.super.onBackPressed();
                pd.dismiss();

            }
        });
    }

    private String constructNotesString() {
//        Log.v(TAG, "Saving " + chatNotes.size() + " Notes...");
//
//        String notes = "";
//        for (int i = 0; i < chatNotes.size(); i++) {
//            Log.v(TAG, "Notes Test: " + chatNotes.get(i).getNote());
//            if (chatNotes.get(i).getNote().length() != 0) {
//                if (i != 0) {
//                    notes = notes + "\n";
//                }
//                notes = notes + chatNotes.get(i).getNote();
//            }
//        }
//
//        Log.v(TAG, "Saving Final Note Text: " + notes);
//        return notes;
        return mAdapter.constructNotesString();
    }

    private void setRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_notes);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new NotesAdapter(this, chatNotes);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void addNewNote(int position) {
        chatNotes.add(position, new ChatNote(""));
        mAdapter.notifyItemInserted(position);
        mAdapter.focusPosition = position;
        mRecyclerView.smoothScrollToPosition(position);
    }

    public void removeNote(int position) {
        chatNotes.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.focusPosition = position - 1;
    }
}
