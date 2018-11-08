package com.hellodemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.hellodemo.adapter.MessagesBySongsRecyclerAdapter;
import com.hellodemo.interfaces.ChatSongItemInterfaceListener;
import com.hellodemo.models.MessagesUser;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.Song;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

public class MessagesBySongsActivity extends AppCompatActivity implements ChatSongItemInterfaceListener {

    private String TAG = "HelloDemoMessagesBySongsActivity";

    // DATA KEYS
    public static final String CHAT_SCREEN_TARGET_USER = "target_user_data";

    private RecyclerView messages_list_by_songs_recycler_view;
    private ImageView back_icon;

    private TextView title;
    private UserModel mUserModel;
    private MessagesUserGroupListItem targetUser;
    private MessagesUser mMessagesUser;
    private Socket mSocket;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupSocket() {
        try {
            if (mSocket != null && mSocket.connected()) {
                mSocket.off();
                mSocket.disconnect();
            }

            mSocket = IO.socket(BuildConfig.SOCKET_URL);
            mSocket.emit("myConnection", new JSONObject("{\"userId\": " + mUserModel.getId() + "}"));

            mSocket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args != null) {
                        if (args.length > 0) {
                            Log.v(TAG, "New message received on Socket:\n" + args[0]);
                            MessagesBySongsActivity.this.onLoadService();
                        }
                    }
                }
            });

            mSocket.connect();

            Log.v(TAG, "Emitting message on myConnection: " + "{\"userId\": " + mUserModel.getId() + "}");
            Log.v(TAG, "Socket Connected on " + BuildConfig.SOCKET_URL + " : " + mSocket.connected());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.v(TAG, "Socket Error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");


        setContentView(R.layout.activity_message_by_songs);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }
        if (getIntent() != null && getIntent().hasExtra(CHAT_SCREEN_TARGET_USER)) {
            targetUser = getIntent().getParcelableExtra(CHAT_SCREEN_TARGET_USER);
        }
        messages_list_by_songs_recycler_view = findViewById(R.id.messages_list_by_songs_recycler_view);
        back_icon = findViewById(R.id.back_icon);
        title = findViewById(R.id.title);
        Toolbar toolbar = findViewById(R.id.toolbarx);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        messages_list_by_songs_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        messages_list_by_songs_recycler_view.setAdapter(new MessagesBySongsRecyclerAdapter(this, MessagesBySongsActivity.this));
        ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).setTargetUser(targetUser);

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        onLoadService();

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessagesBySongsActivity.super.onBackPressed();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();


        // connecting socket...
        setupSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSocket.disconnect();
        mSocket.off("message");
    }

    private void onLoadService() {
        String url = BuildConfig.BASE_URL + "/messages/total_songs_shared?user_id=" + mUserModel.getId()
                + "&sender_id=" + targetUser.getId();
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);

        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, response.toString());

                if (!response.optBoolean("error", true)) {
                    mMessagesUser = Utils.parseJson(response.optJSONObject("message").toString(), MessagesUser.class);
                    title.setText(mMessagesUser.getSenderName());
                    /*if(mMessagesUser.getSongs().size() == 1) {
                        Song song = mMessagesUser.getSongs().get(0);
                        Intent intent = new Intent(MessagesBySongsActivity.this, ChatActivity.class);

                        // setting action
                        if (song.getType().equalsIgnoreCase(Song.TYPE_PACKAGE)) {
                            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_PACKAGE);
                        } else if (song.getType().equalsIgnoreCase(Song.TYPE_TRACK)) {
                            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_SONG);
                        }
                        else {
                            Toast.makeText(MessagesBySongsActivity.this, "unknown tye", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // passing data
                        intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, targetUser.getId());
                        intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, targetUser.getFullName());
                        intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, song.getId());
                        intent.putExtra(ChatActivity.KEY_MUSIC_TITLE, song.getTitle());

                        MessagesBySongsActivity.this.startActivity(intent);
                        finish();
                    }*/
                    ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).setDataNotify(mMessagesUser);

                    Log.d(TAG,
                            "count test: " + ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).getItemCount()
                    );
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).performSearch(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClickDeleteMusic(final Song musicListItem) {
        LayoutInflater li = LayoutInflater.from(MessagesBySongsActivity.this);
        View promptsView = li.inflate(R.layout.custom_music_loading_failed_popup, null);

        ImageView popupLogo = promptsView.findViewById(R.id.loading_failed_popup_logo);
        MemphisTextView mainHeading = promptsView.findViewById(R.id.main_heading);
        MemphisTextView subHeading = promptsView.findViewById(R.id.sub_text);
        MemphisTextView yes = promptsView.findViewById(R.id.Retry);
        View cancel = promptsView.findViewById(R.id.Cancel);

        Picasso.with(MessagesBySongsActivity.this).load(R.drawable.orange_delete).into(popupLogo);
        mainHeading.setText("Are you sure you want to delete this conversation?");
        subHeading.setVisibility(View.GONE);
        yes.setText("Yes");
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MessagesBySongsActivity.this);

        // retrieve display dimensions
        Rect displayRectangle = new Rect();
        Window window = MessagesBySongsActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.1f));

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = BuildConfig.BASE_URL + "/conversation/delete";

                final int[] count = new int[1];
                VolleyRequest volleyRequest = new VolleyRequest(MessagesBySongsActivity.this, url, "delete_music1", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", (String.valueOf(mUserModel.getId())));
                params.put("upload_id", String.valueOf(musicListItem.getId()));
                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!response.optBoolean("error", true)) {
                            CustomToast.makeText(MessagesBySongsActivity.this, response.optString("message"), Toast.LENGTH_LONG).show();
                            mMessagesUser.getSongs().remove(musicListItem);
                            ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).notifyMessageBySongsItemRemoved(musicListItem);
                        }
                        Log.d("", "");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });

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
                finish();
            }
        });
//                    CustomToast.makeText(LeaveMessageActivity.this,response.optString("message"),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClickArchiveMusic(final Song musicListItem) {
        LayoutInflater li = LayoutInflater.from(MessagesBySongsActivity.this);
        View promptsView = li.inflate(R.layout.custom_music_loading_failed_popup, null);

        ImageView popupLogo = promptsView.findViewById(R.id.loading_failed_popup_logo);
        MemphisTextView mainHeading = promptsView.findViewById(R.id.main_heading);
        MemphisTextView subHeading = promptsView.findViewById(R.id.sub_text);
        MemphisTextView yes = promptsView.findViewById(R.id.Retry);
        View cancel = promptsView.findViewById(R.id.Cancel);

        Picasso.with(MessagesBySongsActivity.this).load(R.drawable.arhive_orange).into(popupLogo);
        mainHeading.setText("Are you sure you want to archive this conversation?");
        subHeading.setVisibility(View.GONE);
        yes.setText("Yes");
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MessagesBySongsActivity.this);

        // retrieve display dimensions
        Rect displayRectangle = new Rect();
        Window window = MessagesBySongsActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.1f));

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = BuildConfig.BASE_URL + "/archive";

                final int[] count = new int[1];
                VolleyRequest volleyRequest = new VolleyRequest(MessagesBySongsActivity.this, url, "Archive_Music", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", (String.valueOf(targetUser.getId())));
                params.put("upload_id", String.valueOf(musicListItem.getId()));
                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!response.optBoolean("error", true)) {
                            CustomToast.makeText(MessagesBySongsActivity.this, response.optString("message"), Toast.LENGTH_LONG).show();
                            mMessagesUser.getSongs().remove(musicListItem);
                            ((MessagesBySongsRecyclerAdapter) messages_list_by_songs_recycler_view.getAdapter()).notifyMessageBySongsItemRemoved(musicListItem);
                        }
                        Log.d("", "");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });

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
                finish();
            }
        });
    }

    @Override
    public void onClickSurface(Song song) {
        Intent intent = new Intent(MessagesBySongsActivity.this, ChatActivity.class);

        // setting action
        if (song.getType().equalsIgnoreCase(Song.TYPE_PACKAGE)) {
            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_PACKAGE);
        } else if (song.getType().equalsIgnoreCase(Song.TYPE_TRACK)) {
            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_SONG);
        }
        else {
            Toast.makeText(MessagesBySongsActivity.this, "unknown tye", Toast.LENGTH_SHORT).show();
            return;
        }

        // passing data
        intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, targetUser.getId());
        intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, targetUser.getFullName());
        intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, song.getId());
        intent.putExtra(ChatActivity.KEY_MUSIC_TITLE, song.getTitle());

        MessagesBySongsActivity.this.startActivity(intent);
    }
}
