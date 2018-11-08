package com.hellodemo;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.adapter.NavGroupAdapter;
import com.hellodemo.adapter.NavOpenGroupsAdapter;
import com.hellodemo.adapter.NavScreenAdapter;
import com.hellodemo.adapter.NotificationsAdapter;
import com.hellodemo.adapter.ViewGroupMembersListAdapter;
import com.hellodemo.fragment.ArtistsFragment;
import com.hellodemo.fragment.GroupsFragment;
import com.hellodemo.fragment.LabelsFragment;
import com.hellodemo.fragment.MessagesFragment;
import com.hellodemo.fragment.MusicPlayerFragment;
import com.hellodemo.fragment.SettingsFragment;
import com.hellodemo.interfaces.ScreenInterfaceListener;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.interfaces.XmlClickableInterface;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.NavMenuModel;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import dm.audiostreamer.AudioStreamingManager;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UserAttributes;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, ScreenInterfaceListener {
    public static final String ACTION_OPEN_INBOX_SCREEN = "inbox_screen";
    public static final String ACTION_OPEN_GROUP_SCREEN = "group_screen";
    public static final String KEY_GROUP_ID_EXTRA = "group_id";

    private String TAG = "HelloDemoMainActivity";
    private static Context context;

    String activityName;
    String notificationSongTitle;
    RecyclerView nav_groups_recycler_view, nav_screens_recycler_view;

    ImageView toolbar_music_recycler_view_list_item_image, img_nav_back,
            img_nav_profile, nav_add_user_icon, logo, nav_message, nav_add_icon,
            nav_settings_group, nav_search_icon, nav_notification, unread_notification_dot, direct_msgs_icon;

    TextView toolbar_title, txt_nav_name, txt_nav_notif_count, txt_nav_newdemo,
            txt_logout, txt_open_groups, txt_my_groups, label_first_letter, app_bar_label_first_letter;
    View view_nav_notif_highlight, music_player;

    // This variable holds the instance of current showing fragment...
    Fragment currentFragment = null;
    MusicPlayerFragment mPlayerFragment;
    // The drawwer on the left side...
    DrawerLayout leftDrawer = null;
    FrameLayout flContent;

    UserModel mUserModel;
    private NavMenuModel mNavMenuModel;
    private NavGroupItem mSelectedGroup = null;
    private RecyclerView nav_opengroups_recycler_view;

    // Temporary holder for fragment to set...
    // this holds the fragment until leftDrawer is closed...
    // after that it will change the fragment...
    Fragment mFragmentToSet = null;
    long openGroupByGroupID = -1;
    Toolbar actionBarToolBar;

    AppCompatEditText new_name;
    MemphisTextView dialog_text, dialog_heading;

    // this variable will be true if activity is visible on screen
    private static boolean isActive = false;
    private Stack<String> titleStack;
    String currTitle, prevTitle;

    // notification actions keys
    public static final String ACTION_NEW_INBOX_TRACK_NOTIFICATION = "inbox_track_notification";
    public static final String ACTION_NEW_GROUP_TRACK_NOTIFICATION = "group_track_notification";

    private String currentDisplayedScreen = null;

    public void openChangeFullNameAlert() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.custom_popup_changename, null);
        new_name = promptsView.findViewById(R.id.newname);
        dialog_text = promptsView.findViewById(R.id.text);
        dialog_heading = promptsView.findViewById(R.id.textViewNombreVideo);
        dialog_text.setText("Say hello to your new music inbox. Before getting started, choose the name you want people to find you by. We suggest you choose your full name or artist name.");
        dialog_heading.setText("Welcome!");
        View save = promptsView.findViewById(R.id.Save);
        View cancel = promptsView.findViewById(R.id.Cancel);


        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // retrieve display dimensions
        Rect displayRectangle = new Rect();
        Window window = MainActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        promptsView.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
        promptsView.setMinimumHeight((int) (displayRectangle.height() * 0.3f));

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get user input and set it to result

                final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Changing Name. Please Wait!");
                pd.show();

                String strToPass = "";

                if(!new_name.getText().toString().isEmpty()) {
                    strToPass = new_name.getText().toString().trim();
                } else {
                    strToPass = mUserModel.getUsername();
                }
                String url = "";
                url = BuildConfig.BASE_URL + "/name/change";

                VolleyRequest volleyRequest = new VolleyRequest(MainActivity.this, url, "Change_Name", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(mUserModel.getId()));
                params.put("new_name", strToPass);//new_name.getText().toString());*/

                Log.v(TAG, "Change Name API Called:\n"
                        + "URL: " + url + "\n"
                        + "Params: " + params.toString());

                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "Change Password API Called:\n"
                                + response.toString());


                        if (!response.optBoolean("error", true)) {
//                            Toast.makeText(MainActivity.this, "Successfully saved", Toast.LENGTH_LONG).show();
                            mUserModel.setFullName(new_name.getText().toString().trim());
                            try {
                                mUserModel.setFull_name_null(Integer.parseInt(response.getString("full_name_null")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            CustomToast.makeText(MainActivity.this, "Successfully saved", Toast.LENGTH_LONG).show();

                            if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                                UserAttributes userAttributes = new UserAttributes.Builder().withName(new_name.getText().toString().trim()).build();
                                Intercom.client().updateUser(userAttributes);
                            }

                            // Saving the data to shared preferences as well
                            Gson gson = new Gson();
                            UserSharedPreferences.saveString(
                                    MainActivity.this,
                                    UserSharedPreferences.USER_MODEL,
                                    gson.toJson(mUserModel)
                            );
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        CustomToast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
                alertDialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Changing Name. Please Wait!");
                pd.show();

                String url = "";
                url = BuildConfig.BASE_URL + "/name/change";

                VolleyRequest volleyRequest = new VolleyRequest(MainActivity.this, url, "Change_Name", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(mUserModel.getId()));
                params.put("new_name", mUserModel.getUsername());//new_name.getText().toString());*/

                Log.v(TAG, "Change Name API Called:\n"
                        + "URL: " + url + "\n"
                        + "Params: " + params.toString());

                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "Change Password API Called:\n"
                                + response.toString());


                        if (!response.optBoolean("error", true)) {
//                            Toast.makeText(MainActivity.this, "Username saved as full name.", Toast.LENGTH_LONG).show();
                            mUserModel.setFullName(mUserModel.getUsername());
                            try {
                                mUserModel.setFull_name_null(Integer.parseInt(response.getString("full_name_null")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            CustomToast.makeText(MainActivity.this, "Username saved as full name", Toast.LENGTH_LONG).show();

                            if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                                UserAttributes userAttributes = new UserAttributes.Builder().withName(new_name.getText().toString().trim()).build();
                                Intercom.client().updateUser(userAttributes);
                            }

                            // Saving the data to shared preferences as well
                            Gson gson = new Gson();
                            UserSharedPreferences.saveString(
                                    MainActivity.this,
                                    UserSharedPreferences.USER_MODEL,
                                    gson.toJson(mUserModel)
                            );
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        CustomToast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
                // Intent intent = new Intent(getActivity(),SettingsFragment);
                // startActivity(intent);
                alertDialog.dismiss();
            }
        });

        // show it
        alertDialog.show();
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = MainActivity.this;

        String sentryDsn = "https://1ac864fafbf94d6e82ec7856af01b825@sentry.io/1295108";
        Sentry.init(sentryDsn, new AndroidSentryClientFactory(context));

        Log.v(TAG, "FCM Token: " + FirebaseInstanceId.getInstance().getToken());
        titleStack = new Stack<String>();
        currTitle = prevTitle = "";
        // this code is use to get, from which activity main actvity is called
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                activityName = null;
                notificationSongTitle = null;
            } else {
                activityName = extras.getString("activity_name");
                notificationSongTitle = extras.getString("song_title");
            }
        } else {
            activityName = (String) savedInstanceState.getSerializable("activity_name");
        }

        // Notification Channel for Android 8.0
        createNotificationChannel();

        setContentView(R.layout.activity_main);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //nav_settings_group.setVisibility(View.GONE);
        flContent = (FrameLayout) findViewById(R.id.flContent);

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        nav_groups_recycler_view = findViewById(R.id.nav_groups_recycler_view);
        nav_opengroups_recycler_view = findViewById(R.id.nav_open_groups_recycler_view);
        nav_screens_recycler_view = findViewById(R.id.nav_screens_recycler_view);
        toolbar_music_recycler_view_list_item_image = findViewById(R.id.notification_avatar);
        toolbar_title = findViewById(R.id.toolbar_title);
        img_nav_back = findViewById(R.id.img_nav_back);
        img_nav_profile = findViewById(R.id.img_nav_profile);
        label_first_letter = findViewById(R.id.label_first_letter);
        app_bar_label_first_letter = findViewById(R.id.app_bar_label_first_letter);
        txt_nav_name = findViewById(R.id.txt_nav_name);
        txt_my_groups = findViewById(R.id.txt_my_groups);
        txt_open_groups = findViewById(R.id.txt_open_groups);
        logo = findViewById(R.id.img_txt_logo);
        txt_nav_notif_count = findViewById(R.id.txt_nav_notif_count);
        view_nav_notif_highlight = findViewById(R.id.view_nav_notif_highlight);
        txt_nav_newdemo = findViewById(R.id.txt_nav_newdemo);
        music_player = findViewById(R.id.music_player);
        nav_add_user_icon = findViewById(R.id.nav_add_user_icon);
        nav_search_icon = findViewById(R.id.nav_search_icon);
        direct_msgs_icon = findViewById(R.id.nav_direct_msg_icon);
        nav_message = findViewById(R.id.nav_message);
        nav_settings_group = findViewById(R.id.nav_settings_group);
        nav_notification = findViewById(R.id.notification_bell);
        unread_notification_dot = findViewById(R.id.notification_unread_dot);
        nav_add_icon = findViewById(R.id.nav_add_icon);
        txt_logout = findViewById(R.id.txt_logout);
//        nav_notification.setVisibility(View.GONE);
        nav_settings_group.setVisibility(View.GONE);
        txt_my_groups.setOnClickListener(this);
        txt_open_groups.setOnClickListener(this);

        nav_groups_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        nav_screens_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        nav_opengroups_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        nav_screens_recycler_view.setAdapter(new NavScreenAdapter(this));
        nav_groups_recycler_view.setAdapter(new NavGroupAdapter(this));
        nav_opengroups_recycler_view.setAdapter(new NavOpenGroupsAdapter(this));


        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
//        mUserModel.setType("label");
        if(mUserModel.getFull_name_null() == 1) {
            openChangeFullNameAlert();
        }

        bindValues();

        clickListeners();

        // we are setting this as our main_toolbar so that we can access it in fragments to change menus
        actionBarToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(actionBarToolBar);


        // leftDrawer setting...
        leftDrawer = findViewById(R.id.drawer_layout);
        leftDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                bindValues();
                getMenuService();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //Set your new fragment here
                if (mFragmentToSet != null) {
                    updateFragment();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });


        // we want to start with an open drawer
        getMenuService();
        leftDrawer.openDrawer(GravityCompat.START);

        String title = "Inbox";
        int position = 0;
        selectedScreen(position, title);
        leftDrawer.openDrawer(GravityCompat.START);
        if(activityName != null && activityName.equals(LoginActivity.ACTIVITY_NAME)) {
            AudioStreamingManager.repeat_status1 = 0;
            AudioStreamingManager.shuffle_status = false;
            if(MusicPlayerFragment.shuffle_repeat_prefs !=null) {
                MusicPlayerFragment.shuffle_repeat_prefs.edit().putBoolean("shuffle_status", AudioStreamingManager.shuffle_status).apply();
                MusicPlayerFragment.shuffle_repeat_prefs.edit().putInt("repeat_status", AudioStreamingManager.repeat_status1).apply();
            }

        }
    }

    // Changes fragment for the current activity...
    // don't call this function directly...
    // set mFragmentToSet to the new fragment and close drawer to call this method...
    private void updateFragment() {
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            if (fragment instanceof SettingsFragment || fragment instanceof ArtistsFragment ||fragment instanceof GroupsFragment || fragment instanceof LabelsFragment || fragment instanceof MessagesFragment || fragment instanceof MusicPlayerFragment) {
                continue;
            }
            else if (fragment!=null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        if (mFragmentToSet == null)
            return;

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.flContent, mFragmentToSet)
                .addToBackStack(null)
                .commit();
        mFragmentToSet = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // below line commented because it always starts inbox activity when activity resumes
        //decideForScreenFromIntent(intent);
        if (intent != null)
            setIntent(intent);
    }


    private void decideForScreenFromIntent(Intent intent) {


        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(MainActivity.ACTION_OPEN_INBOX_SCREEN)) {

//            Log.v("TESTT", "Updating Inbox");
            String title = "Inbox";
            int position = 0;
            selectedScreen(position, title);

        } else if (intent != null && intent.getAction() != null
                && intent.getAction().equals(MainActivity.ACTION_OPEN_GROUP_SCREEN)) {

            Log.v(TAG, "Opening Group Screen from Intent");

            // get group_id...
            long groupID = intent.getLongExtra(KEY_GROUP_ID_EXTRA, -1);

            if (groupID == -1) {
                CustomToast.makeText(this, "Group not found", Toast.LENGTH_LONG).show();
                return;
            }

            // find NavGroupItem for that id...
            openGroupByGroupID = groupID;

            // if menu service has already populated groups, we will use that...
            // otherwise menu service will later call this function on execution...
            if (mNavMenuModel != null) {
                openGroupFromIntent();
            }

        }
        // if notification for a new track in inbox...
        else if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(MainActivity.ACTION_NEW_INBOX_TRACK_NOTIFICATION)) {
            // incase inbox screen is already open, we will refresh it...
            /*if (currentDisplayedScreen != null && currentDisplayedScreen.equalsIgnoreCase("inbox")) {
                // refresh the screen...
                String title = "Inbox";
                int position = 0;
                selectedScreen(position, title);
            }*/
        }
        // if notification for a new track in group...
        else if (intent != null && intent.getAction() != null &&
                intent.getAction().equals(MainActivity.ACTION_NEW_GROUP_TRACK_NOTIFICATION)) {

            // getting notification data...
            long groupID = intent.getLongExtra("group_id", -1);

            Log.v(TAG, "Opening group by new notification data1:" +
                    "\ncurrentDisplayedScreen: " + currentDisplayedScreen +
                    "\ngroupID: " + groupID);

            // in case group screen is already open, and the same group id, we will refresh it...
            if (currentDisplayedScreen != null && currentDisplayedScreen.equalsIgnoreCase("group")) {
                if (mSelectedGroup != null) {
                    Log.v(TAG, "Opening group by new notification data2:" +
                            "\nmSelectedGroup.getId(): " + mSelectedGroup.getId());
                    if (mSelectedGroup.getId() == groupID) {
                        // refresh the screen...
                        selectedGroup(mSelectedGroup);
                    }
                }
            }
        } else {
            // else we will open inbox...
            /*String title = "Inbox";
            int position = 0;
            selectedScreen(position, title);*/
        }
    }

    public void bindValues() {
//        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        if (mUserModel == null) return;
        Log.v(TAG, "Binding New Values... " + mUserModel.getAvatar());
        if(mUserModel.getType().equals("label")) {
            img_nav_profile.setVisibility(View.INVISIBLE);
            toolbar_music_recycler_view_list_item_image.setVisibility(View.INVISIBLE);
            label_first_letter.setText(mUserModel.getFullName());
            app_bar_label_first_letter.setText(mUserModel.getFullName());
        }
        else {
            label_first_letter.setVisibility(View.INVISIBLE);
            app_bar_label_first_letter.setVisibility(View.INVISIBLE);
            Picasso.with(this).load(mUserModel.getAvatar()).into(img_nav_profile);
            Picasso.with(this).load(mUserModel.getAvatar()).into(toolbar_music_recycler_view_list_item_image);
        }
        String pre = "Logout from ";
//        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        String s;
        if(mUserModel.getFullName().isEmpty()) {
            s = pre + mUserModel.getUsername();
        } else {
            s = pre + mUserModel.getFullName();
        }
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new RelativeSizeSpan(1.1f), pre.length(), s.length(), 0); // set size
        ss.setSpan(new StyleSpan(Typeface.BOLD), pre.length(), s.length(), 0);// set color
        txt_logout.setText(ss);
    }


    private void clickListeners() {
        img_nav_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftDrawer.closeDrawer(GravityCompat.START);
            }
        });

        nav_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(Intent);
            }
        });

        toolbar_music_recycler_view_list_item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftDrawer.openDrawer(GravityCompat.START);

            }
        });

        app_bar_label_first_letter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftDrawer.openDrawer(GravityCompat.START);

            }
        });

        //click event for the nav add icon
        nav_add_user_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                startActivity(intent);
            }
        });

        //click event for the nav add icon
        nav_search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        direct_msgs_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DirectMessageActivity.class);
                startActivity(intent);
            }
        });


        nav_settings_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                View promptsView = li.inflate(R.layout.bottomsheet_groupsettings, null);

//                promptsView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this, R.style.CustomBottomSheetDialogTheme);
                dialog.setContentView(promptsView);
                dialog.show();
                TextView invitesMembers = promptsView.findViewById(R.id.add_members_button);

                // if group type is open, we let the creator invite members...
                if (mSelectedGroup.getType().equalsIgnoreCase("open")) {
                    invitesMembers.setText("Invite Members");
                }
                // for when group type is close, we let the creator add members...
                else if (mSelectedGroup.getType().equalsIgnoreCase("close")) {
                    invitesMembers.setText("Add Members");
                }
                long userID = Utils.parseJson(UserSharedPreferences.getString(getBaseContext(),
                        UserSharedPreferences.USER_MODEL), UserModel.class).getId();

                // if user is the owner of the group, then let owner add member
                if ((userID == mSelectedGroup.getCreater())) {
                    //setting click listeners
                    invitesMembers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, AddContactsToGroupActivity.class);
                            intent.putExtra(AddContactsToGroupActivity.KEY_GROUP_ID, mSelectedGroup.getId());
                            intent.putExtra("type", mSelectedGroup.getType());
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                }
                // otherwise for members , its disabled
                else {

                    invitesMembers.setAlpha(.5f);
                    invitesMembers.setClickable(false);
                }

                promptsView.findViewById(R.id.members_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ViewGroupMembersActivity.class);
                        intent.putExtra(ViewGroupMembersActivity.KEY_GROUP_ID, mSelectedGroup.getId());
                        intent.putExtra(ViewGroupMembersActivity.KEY_GROUP_ADMIN_ID, mSelectedGroup.getCreater());
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                if (mSelectedGroup.getType().equals("open")) {
                    promptsView.findViewById(R.id.message_group_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_GROUP);
                            intent.putExtra(ChatActivity.KEY_GROUP_ID, mSelectedGroup.getId());
                            intent.putExtra(ChatActivity.KEY_GROUP_NAME, mSelectedGroup.getName());
                            intent.putExtra(String.valueOf(ChatActivity.KEY_ADMIN_NAME), mSelectedGroup.getCreater());
                            intent.putExtra(ChatActivity.KEY_ADMIN_ID, mSelectedGroup.getCreater());
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                } else {
                    promptsView.findViewById(R.id.message_group_button).setVisibility(View.GONE);
                }
                promptsView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


            }
        });
        //click event for the nav message icon
//        nav_message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//                intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_GROUP);
//                intent.putExtra(ChatActivity.KEY_GROUP_ID, mSelectedGroup.getId());
//                intent.putExtra(ChatActivity.KEY_GROUP_NAME, mSelectedGroup.getName());
//                startActivity(intent);
//
//
//            }
//        });
        //click event for group add button
//        nav_add_icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, AddContactsToGroupActivity.class);
//                intent.putExtra(AddContactsToGroupActivity.KEY_GROUP_ID, mSelectedGroup.getId());
//                startActivity(intent);
//
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        if (leftDrawer.isDrawerOpen(GravityCompat.START)) {
            leftDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                nav_message.setVisibility(View.GONE);
                nav_add_icon.setVisibility(View.GONE);
                nav_add_user_icon.setVisibility(View.GONE);
                nav_search_icon.setVisibility(View.GONE);
                direct_msgs_icon.setVisibility(View.GONE);
                nav_settings_group.setVisibility(View.GONE);
                nav_notification.setVisibility(View.GONE);
                unread_notification_dot.setVisibility(View.GONE);

                currTitle = prevTitle;
                toolbar_title.setText(titleStack.pop());
                if(!titleStack.isEmpty()) {
                    prevTitle = titleStack.peek();
                }

                if(toolbar_title.getText().toString().equals("Inbox")) {
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    nav_notification.setVisibility(View.VISIBLE);
                    unread_notification_dot.setVisibility(View.VISIBLE);
                    nav_search_icon.setVisibility(View.VISIBLE);
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(0);
                    currentDisplayedScreen = "inbox";
                } else if(toolbar_title.getText().toString().equals("Uploads")) {
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    nav_search_icon.setVisibility(View.VISIBLE);
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(1);
                    currentDisplayedScreen = "uploads";
                } else if(toolbar_title.getText().toString().equals("Messages")) {
                    direct_msgs_icon.setVisibility(View.VISIBLE);
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(2);
                    currentDisplayedScreen = "messages";
                } else if(toolbar_title.getText().toString().equals("Favorites")) {
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(3);
                    currentDisplayedScreen = "favorite";
                } else if(toolbar_title.getText().toString().equals("Settings")) {
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(4);
                    currentDisplayedScreen = "settings";
                } else {
                    nav_message.setVisibility(View.GONE);
                    nav_settings_group.setVisibility(View.VISIBLE);
                    ((NavOpenGroupsAdapter) nav_opengroups_recycler_view.getAdapter()).setSelectedItemPosition(0);
                    currentDisplayedScreen = "group";
                }
                super.onBackPressed();
            } else {
                if(mPlayerFragment instanceof MusicPlayerFragment) {
                    mPlayerFragment.unSubscribeCallBackFragment();
                }
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            activityName = null;
        } else {
            activityName = extras.getString("activity_name");
            notificationSongTitle = extras.getString("song_title");
        }
        isActive = true;
        getMenuService();
        // managing add friend queue for the scanned qr codes in offline mode...
        manageOfflineQRCodes();
        decideForScreenFromIntent(getIntent());
        if(activityName != null && (activityName.equals(NotificationsAdapter.ACTIVITY_NAME) || activityName.equals(MyFirebaseMessagingService.ACTIVITY_NAME))) {
            mPlayerFragment.setActivityName(activityName);
            mPlayerFragment.setNotficationSongTitle(notificationSongTitle);
        }
        super.onResume();
    }

    private void manageOfflineQRCodes() {
        if (!UserSharedPreferences.getUserIDsToBeAddedAsFriend(MainActivity.this).isEmpty()) {

            final String userID = UserSharedPreferences.getUserIDsToBeAddedAsFriend(MainActivity.this).get(0);

            String url = BuildConfig.BASE_URL + "/friends/add_friend_for_mobile?user1_id="
                    + mUserModel.getId() + "&user2_id=" + UserSharedPreferences.getUserIDsToBeAddedAsFriend(MainActivity.this);

            Log.v(TAG, "4753 Add Friends API Called:\n" + "URL: " + url);

            VolleyRequest volleyRequest = new VolleyRequest(this, url, "add_by_qr", true);
            volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "4753 Add Friends API Response:\n"
                            + response.toString());

                    if (!response.optBoolean("error", true)) {

                        // remove from the queue..
                        // and call the next user ID in queue...
                        if (!UserSharedPreferences.removeUserIDToBeAddedAsFriendFromQueue(MainActivity.this,
                                userID).isEmpty()) {
                            manageOfflineQRCodes();

                        }

                    }
                    Log.d("", "");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("", "");

                    // if friend can not be added because of network issue...
                    // we will add the qr code to a list to be added as friend later...
                    UserSharedPreferences.addUserIDToBeAddedAsFriend(MainActivity.this,
                            userID);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        isActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getMenuService() {
        String url = BuildConfig.BASE_URL + "/get-menu?user_id=" + mUserModel.getId();
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("Response get menu :", response.toString());
                if (!response.optBoolean("error", true)) {
                    mNavMenuModel = Utils.parseJson(response.optJSONObject("data").toString(), NavMenuModel.class);
                    mNavMenuModel.separteOpenAndCloseGroups();

                    UserSharedPreferences.saveString(MainActivity.this, UserSharedPreferences.MENU_MODEL,
                            response.optJSONObject("data").toString());
                    if (mNavMenuModel.getDemoNum() > 0) {
                        logo.setVisibility(View.GONE);
                        txt_nav_notif_count.setVisibility(View.VISIBLE);
                        view_nav_notif_highlight.setVisibility(View.VISIBLE);
                        txt_nav_newdemo.setVisibility(View.VISIBLE);
                    } else {
                        logo.setVisibility(View.VISIBLE);
                        txt_nav_notif_count.setVisibility(View.INVISIBLE);
                        view_nav_notif_highlight.setVisibility(View.INVISIBLE);
                        txt_nav_newdemo.setVisibility(View.INVISIBLE);
                    }
                    txt_nav_notif_count.setText(String.valueOf(mNavMenuModel.getDemoNum()));
                    ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setData(mNavMenuModel);
                    ((NavGroupAdapter) nav_groups_recycler_view.getAdapter()).setData(mNavMenuModel);
                    ((NavOpenGroupsAdapter) nav_opengroups_recycler_view.getAdapter()).setData(mNavMenuModel);

                    // Need to set up the notification unread icon based on the unreadNotifications
                    Picasso.with(MainActivity.this).load(R.drawable.bellicon).into(nav_notification);
                    if (mNavMenuModel.getUnreadNotificationsNumWOMsgs() > 0) {
//                        nav_notification.setImageDrawable(getResources().getDrawable(R.drawable.bellicon_unread));
//                        if(unread_notification_dot.getVisibility() == View.INVISIBLE) {
//                            unread_notification_dot.setVisibility(View.VISIBLE);
//                        }
//                        Animation animation = new AlphaAnimation(1, 0);
//                        animation.setDuration(500);
//                        animation.setInterpolator(new LinearInterpolator());
//                        animation.setRepeatCount(Animation.INFINITE);
//                        animation.setRepeatMode(Animation.REVERSE);
//                        unread_notification_dot.startAnimation(animation);
                        setUnreadAnimation();
                    } else {
                        unread_notification_dot.clearAnimation();
                        unread_notification_dot.setVisibility(View.INVISIBLE);
//                        nav_notification.setImageDrawable(getResources().getDrawable(R.drawable.bellicon));
                    }

                    // its possible that this activity was opened by by an intent which needs to
                    // open a group and is waiting for this api to load...
                    openGroupFromIntent();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }

    public void setUnreadAnimation() {
        if(unread_notification_dot.getVisibility() == View.INVISIBLE) {
            unread_notification_dot.setVisibility(View.VISIBLE);
        }
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        unread_notification_dot.startAnimation(animation);
    }


    // this function is called to open group from intent
    private void openGroupFromIntent() {
        if (openGroupByGroupID != -1) {
            Log.v(TAG, "openGroupFromIntent started");
            List<NavGroupItem> groupsList = mNavMenuModel.getGroupList();
            NavGroupItem foundGroup = null;
            for (NavGroupItem g : groupsList) {
                if (g.getId() == openGroupByGroupID) {
                    foundGroup = g;
                    break;
                }
            }

            // if found, call selected group
            if (foundGroup != null) {
                openGroupByGroupID = -1;
//                Log.v(TAG, "Getting ready to call selectedGroup Lolz");
                selectedGroup(foundGroup);
            } else {

                // if not found, set a boolean variable, by which we can open group in get MenuService
                CustomToast.makeText(this, "Group Not Found", Toast.LENGTH_LONG).show();
                return;
            }
        }

        openGroupByGroupID = -1;
    }

    public void logout(View view) {
        String url = BuildConfig.BASE_URL + "/logout?user_id=" + mUserModel.getId();
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "logout", true);
        volleyRequest.requestServer(Request.Method.GET, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)){
                    leftDrawer.closeDrawer(GravityCompat.START);
                    if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
                        Intercom.client().logout();
                    }
                    UserSharedPreferences.deleteSharedPreferences(MainActivity.this);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("","");
            }
        });
    }

    public void newGroup(View view) {
        leftDrawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void selectedScreen(int position, String title) {
        getMenuService();

        //show add icon and hide icons for group screen
        nav_message.setVisibility(View.GONE);
        nav_add_icon.setVisibility(View.GONE);
        nav_add_user_icon.setVisibility(View.GONE);
        nav_search_icon.setVisibility(View.GONE);
        direct_msgs_icon.setVisibility(View.GONE);
        nav_settings_group.setVisibility(View.GONE);
        nav_notification.setVisibility(View.GONE);
        unread_notification_dot.setVisibility(View.GONE);

        ((NavGroupAdapter) nav_groups_recycler_view.getAdapter()).setSelectedItemPosition(-1);
        ((NavOpenGroupsAdapter) nav_opengroups_recycler_view.getAdapter()).setSelectedItemPosition(-1);
        ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(position);
        if(!currTitle.equals("")) {
            prevTitle = currTitle;
            titleStack.push(prevTitle);
        }
        currTitle = title;
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;

        currentDisplayedScreen = null;
        switch (position) {
            case 0: { //inbox
                toolbar_title.setText(title);
                nav_add_user_icon.setVisibility(View.VISIBLE);
                nav_notification.setVisibility(View.VISIBLE);
                unread_notification_dot.setVisibility(View.VISIBLE);
                nav_search_icon.setVisibility(View.VISIBLE);
                try {
                    fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_INBOX,
                            null);
                    currentDisplayedScreen = "inbox";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 1: { //uploads
                if(mUserModel.getType().equals("label")) {
                    direct_msgs_icon.setVisibility(View.VISIBLE);
                    toolbar_title.setText(title);
                    try {
                        fragment = MessagesFragment.newInstance();
                        currentDisplayedScreen = "messages";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    toolbar_title.setText(title);
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    nav_search_icon.setVisibility(View.VISIBLE);
                    try {
                        fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_UPLOADS,
                                null);
                        currentDisplayedScreen = "uploads";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            case 2: { //messages
                if(mUserModel.getType().equals("label")) {
                    toolbar_title.setText(title);
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    try {
                        fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_FAVORITE,
                                null);
                        currentDisplayedScreen = "favorite";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    direct_msgs_icon.setVisibility(View.VISIBLE);
                    toolbar_title.setText(title);
                    try {
                        fragment = MessagesFragment.newInstance();
                        currentDisplayedScreen = "messages";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            case 3: { //Favorite
                if(mUserModel.getType().equals("label")) {
                    try {
                        //getSupportActionBar().hide();
                        toolbar_title.setText("Settings");
                        fragment = SettingsFragment.newInstance(this);
                        currentDisplayedScreen = "settings";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    toolbar_title.setText(title);
                    nav_add_user_icon.setVisibility(View.VISIBLE);
                    try {
                        fragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_FAVORITE,
                                null);
                        currentDisplayedScreen = "favorite";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            case 4: { //Settings
                try {
                    //getSupportActionBar().hide();
                    toolbar_title.setText("Settings");
                    fragment = SettingsFragment.newInstance(this);
                    currentDisplayedScreen = "settings";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        mFragmentToSet = fragment;
        if(fragment instanceof MusicPlayerFragment) {
            mPlayerFragment = (MusicPlayerFragment) fragment;
        }
        updateFragment();
        leftDrawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void setActivityNameNull() {
        this.activityName = null;
        this.notificationSongTitle = null;
    }

    @Override
    public void selectedGroup(NavGroupItem group) {
        Log.v(TAG, "selectedGroup started");
        getMenuService();

        (nav_groups_recycler_view.getAdapter()).notifyDataSetChanged();
        (nav_opengroups_recycler_view.getAdapter()).notifyDataSetChanged();

        //hide add icon and show icons for group screen

        // now visible in  musicplayerfragment
        // nav_add_icon.setVisibility(View.VISIBLE);
        nav_add_icon.setVisibility(View.GONE);
        nav_add_user_icon.setVisibility(View.GONE);
        nav_search_icon.setVisibility(View.GONE);
        direct_msgs_icon.setVisibility(View.GONE);
        nav_notification.setVisibility(View.GONE);
        unread_notification_dot.setVisibility(View.GONE);

        if(!currTitle.equals("")) {
            prevTitle = currTitle;
            titleStack.push(prevTitle);
        }
        currTitle = group.getName();
        // set group's name as activity title...
        toolbar_title.setText(group.getName());

        mSelectedGroup = group;
        ((NavScreenAdapter) nav_screens_recycler_view.getAdapter()).setSelectedItemPosition(-1);
        if (group.getType().equals("open")) {
            // now visible in  musicplayerfragment
            // nav_message.setVisibility(View.VISIBLE);
            nav_message.setVisibility(View.GONE);
            nav_settings_group.setVisibility(View.VISIBLE);
            ((NavOpenGroupsAdapter) nav_opengroups_recycler_view.getAdapter()).setSelectedGroup(group);
            ((NavGroupAdapter) nav_groups_recycler_view.getAdapter()).setSelectedGroup(null);
        } else {
            nav_message.setVisibility(View.GONE);
            nav_settings_group.setVisibility(View.VISIBLE);
            ((NavOpenGroupsAdapter) nav_opengroups_recycler_view.getAdapter()).setSelectedGroup(null);
            ((NavGroupAdapter) nav_groups_recycler_view.getAdapter()).setSelectedGroup(group);
        }

        // when drawer is closed, fragment will change to fragment in mFragmentToSet
        // see drawer listener in onCreate...
        mFragmentToSet = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_GROUP_MUSIC,
                group);
        mPlayerFragment = (MusicPlayerFragment) mFragmentToSet;

        if(activityName != null && (activityName.equals(NotificationsAdapter.ACTIVITY_NAME) || activityName.equals(MyFirebaseMessagingService.ACTIVITY_NAME))) {
            mPlayerFragment.setActivityName(activityName);
            mPlayerFragment.setNotficationSongTitle(notificationSongTitle);
        }
        currentDisplayedScreen = "group";
        updateFragment();
        leftDrawer.closeDrawer(GravityCompat.START);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_my_groups:
                if (nav_groups_recycler_view.getVisibility() == View.GONE) {
                    nav_groups_recycler_view.setVisibility(View.VISIBLE);
                    txt_my_groups.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0);
                } else {
                    txt_my_groups.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
                    nav_groups_recycler_view.setVisibility(View.GONE);
                }
                break;
            case R.id.txt_open_groups:
                if (nav_opengroups_recycler_view.getVisibility() == View.GONE) {
                    nav_opengroups_recycler_view.setVisibility(View.VISIBLE);

                    txt_open_groups.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0);
                } else {
                    txt_open_groups.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
                    nav_opengroups_recycler_view.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void onFragmentElementClicked(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            if (fragmentManager.getFragments().size() == 1) {

                // all fragments must implement XmlClickableInterface or the code will crash...
                ((XmlClickableInterface) fragmentManager.getFragments().get(0)).onViewClicked(v);
            }
        }
    }

    //Used to create notification channels for Android 8.0
    private void createNotificationChannel() {
        Log.v(TAG, "Creating notification channel");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "All Notifications";
            String description = "All Notifications sent by HelloDemo";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("all_notifications", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public static boolean isActive() {
        return isActive;
    }
}
