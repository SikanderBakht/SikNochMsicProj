package com.hellodemo.fragment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.swipe.util.Attributes;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.BuildConfig;
import com.hellodemo.ChatActivity;
import com.hellodemo.MainActivity;
import com.hellodemo.MyFirebaseMessagingService;
import com.hellodemo.NotesActivity;
import com.hellodemo.R;
import com.hellodemo.SendSongPopupActivity;
import com.hellodemo.adapter.MusicRecyclerViewAdapter;
import com.hellodemo.adapter.NotificationsAdapter;
import com.hellodemo.interfaces.MusicListInterfaceListener;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.interfaces.XmlClickableInterface;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.AudioStreamingService;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.NotificationManager;

import static android.view.View.GONE;
import static com.hellodemo.ChatActivity.ACTION_OPEN_CHAT_BY_GROUP;
import static com.hellodemo.ChatActivity.ACTION_OPEN_CHAT_BY_SONG;

public class MusicPlayerFragment extends Fragment implements XmlClickableInterface,
        MusicListInterfaceListener, CurrentSessionCallback {

    View view;
    private String TAG = "HelloDemoMusicPlayerFragment";
    // these are the argument parameter keys used to send data
    public static final String ARG_PARAM_SCREEN = "screen_to_display";
    public static final String ARG_PARAM_GROUP = "group_param";

    // These keys are used to identify which music list is being shown at the moment..
    // these are passed in the argument when the fragment is instantiated...
    public static final String KEY_SCREEN_INBOX = "inbox_screen";
    public static final String KEY_SCREEN_UPLOADS = "uploads_screen";
    public static final String KEY_SCREEN_FAVORITE = "favorites_screen";
    public static final String KEY_SCREEN_GROUP_MUSIC = "group_screen";
    public static final String KEY_SCREEN_CHAT = "chat_screen";

    // manages loading icons...
    LoadingManager mainLoadingManger;

    public static SharedPreferences shuffle_repeat_prefs;
    public static boolean check_chat_music = false;

    // This variable is used to identify the currentScreen...
    // it is set at the initialization of the fragment based on argument key...
    private String currentScreen = null;
    private String activityName = null;
    private String notficationSongTitle = null;

    // Group that we are displaying music from... used only in case of group musics...
    NavGroupItem currentGroup = null;

    // Contains logged in user's info....
    UserModel mUserModel;

    TextView player_ticker_title, player_ticker_elapsed_time, player_ticker_total_time, artist_name;
    ImageView player_ticker_play_pause_image, image_shuffle, image_repeat;

    // Seekbar of Music Player...
    SeekBar player_ticker_seekbar;
    ProgressBar progressbar;

    // Recycler view is being used to display music items list...
    RecyclerView music_list_recycler_view;
    MusicRecyclerViewAdapter mAdapter;

    // This list contains musics currently being played....
    List<MusicListItem> mMusicList = new ArrayList<>();
    MusicListItem musicItem;
    // manages streaming of currently playing music...
    private AudioStreamingManager mStreamingManager;
    private AudioStreamingService s;
    private ArrayList<MediaMetaData> mMediaMetaDataArrayList = new ArrayList<>();
    private int mSelectedMusicIndex = -1;

    private int prevProgress = 0;

    @SuppressLint("ValidFragment")
    public MusicPlayerFragment() {
        super();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param paramScreen the screen to display (inbox, uploads etc)<br>
     *                    eg. MusicPlayerFragment.KEY_KEY_SCREEN_INBOX<br><br>
     * @param paramGroup  Group to display music from<br>
     *                    null in case some other screen than group is to be shown
     * @return A new instance of fragment MusicPlayerFragment.
     */
    public static MusicPlayerFragment newInstance(String paramScreen, NavGroupItem paramGroup) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_SCREEN, paramScreen);
        args.putParcelable(ARG_PARAM_GROUP, paramGroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClickNotesMusic(MusicListItem musicListItem) {
        Intent intent = new Intent(getContext(), NotesActivity.class);
        intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_SONG);
        intent.putExtra(NotesActivity.KEY_MUSIC_UPLOAD_ID, musicListItem.getId());
        startActivity(intent);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shuffle_repeat_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        AudioStreamingManager.shuffle_status = shuffle_repeat_prefs.getBoolean("shuffle_status", false);
        AudioStreamingManager.repeat_status1 = shuffle_repeat_prefs.getInt("repeat_status", 0);
        if (getArguments() != null) {
            currentScreen = getArguments().getString(ARG_PARAM_SCREEN);
            currentGroup = getArguments().getParcelable(ARG_PARAM_GROUP);
        }

        // if screen argument is not provided...
        if (currentScreen == null) {
            Log.v(TAG, "ARG_PARAM_SCREEN parameter not passed to the fragment.");
            Toast.makeText(getActivity(), "Cant open fragment!", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        // group screen but group param not provided
        if (currentScreen.equals(KEY_SCREEN_GROUP_MUSIC)) {
            if (currentGroup == null) {
                Log.v(TAG, "ARG_PARAM_GROUP parameter not passed to the fragment.");
                Toast.makeText(getActivity(), "Cant open fragment!", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }

            Log.v(TAG, "Group ID Argument: " + currentGroup.getId());
        }


        // Getting user model...
        mUserModel = Utils.parseJson(UserSharedPreferences.getString(getActivity(),
                UserSharedPreferences.USER_MODEL), UserModel.class);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // Assigning views ...
        music_list_recycler_view = getView().findViewById(R.id.music_list_recycler_view);
        music_list_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new MusicRecyclerViewAdapter(this, getActivity().getApplicationContext());

        mAdapter.setMode(Attributes.Mode.Single);
        showHideMusicPlayer();

        music_list_recycler_view.setAdapter(mAdapter);


        mStreamingManager = AudioStreamingManager.getInstance(getActivity());
        mStreamingManager.setPlayMultiple(true);
        mStreamingManager.setShowPlayerNotification(true);
        mStreamingManager.setPendingIntentAct(getNotificationPendingIntent());

        // Seek Bar
        player_ticker_seekbar = getView().findViewById(R.id.player_ticker_seekbar);

        // TextView
        player_ticker_title = getView().findViewById(R.id.player_ticker_title);
        player_ticker_elapsed_time = getView().findViewById(R.id.player_ticker_elapsed_time);
        player_ticker_total_time = getView().findViewById(R.id.player_ticker_total_time);
        artist_name = getView().findViewById(R.id.artis_name);

        // ImageView
        player_ticker_play_pause_image = getView().findViewById(R.id.player_ticker_play_pause_image);
        image_shuffle = getView().findViewById(R.id.image_shuffle);
        image_repeat = getView().findViewById(R.id.image_repeat);

        if(AudioStreamingManager.repeat_status1 == 0) {
            image_repeat.setImageResource(R.drawable.player_repeat);
        } else if(AudioStreamingManager.repeat_status1 == 1) {
            image_repeat.setImageResource(R.drawable.player_repeat_active);
        } else if(AudioStreamingManager.repeat_status1 == 2) {
            image_repeat.setImageResource(R.drawable.repeat_single);
        }
        image_shuffle.setSelected(AudioStreamingManager.shuffle_status);

        // content of the fragment...
        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = getView().findViewById(R.id.fragment_content);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) getView().findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = getView().findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(getActivity(), fragmentContent, loadingIcon, loadingContainer);

        setupClickListeners();

//        onLoadService();

        super.onViewCreated(view, savedInstanceState);
    }

    public void playSpecificSong() {
        int i = 0;
        while(mMusicList.size() > 0 && notficationSongTitle != null && i < mMusicList.size() && !mMusicList.get(i).getTitle().equals(notficationSongTitle)) {
            i++;
        }
        if(i < mMusicList.size()) {
            mSelectedMusicIndex = i;
        } else {
            mSelectedMusicIndex = 0;
        }

        if(mMusicList.size() > 0 && (activityName.equals(NotificationsAdapter.ACTIVITY_NAME) || activityName.equals(MyFirebaseMessagingService.ACTIVITY_NAME))) {
            setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
            playSelectedMusic();
        }
    }

    public void cleanPlayer(boolean notify, boolean stopService) {
        if(!mStreamingManager.isPlaying()) {
            mStreamingManager.cleanupPlayer(notify, stopService);
            mStreamingManager.clearList();
            mStreamingManager.setCurrentAudio(null);
        }
    }

    @Override
    public void onResume() {
        if(mStreamingManager.isPlaying()) {
            //player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
        } else {
            cleanPlayer(true, true);
            //player_ticker_play_pause_image.setImageResource(R.drawable.player_big_play_active);
        }
        if(currentScreen.equals(KEY_SCREEN_CHAT)) {
             //mStreamingManager.cleanupPlayer(true, true);
             loadMusicFromChat();
        } else {
             onLoadService();
        }
        super.onResume();
    }

    public void loadMusicFromChat() {
        ChatActivity activity = (ChatActivity) getActivity();
        musicItem = activity.getMusicListItem();
        mMusicList.clear();
        mMusicList.add(musicItem);
        mAdapter.setData(mMusicList, currentGroup, true, MusicRecyclerViewAdapter.KEY_CHAT_SCREEN);
        createMediaMetaDataList();
        selectFirstItem();
        showHideMusicPlayer();
        if(currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT) && mStreamingManager.isPlaying() && mStreamingManager.getCurrentAudio().getMediaUrl().equals(String.valueOf(musicItem.getMusicUrl()))) {
            player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
        } else {
            player_ticker_play_pause_image.setImageResource(R.drawable.player_big_play_active);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(!currentScreen.equals(KEY_SCREEN_CHAT)) {
            view = inflater.inflate(R.layout.fragment_music_player, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_music_player_1, container, false);
        }

        progressbar = view.findViewById(R.id.progress_bar2);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This method is called when ever a view is clicked through onClick in XML of Fragment...
     *
     * @param v View Clicked....
     */
    @Override
    public void onViewClicked(View v) {
        switch (v.getId()) {

            case R.id.player_ticker_play_pause_image:
                onPlayPause(v);
                break;

            case R.id.play_next_button:
                onPlayNext(v);
                break;

            case R.id.play_prev_button:
                onPlayBack(v);
                break;

            case R.id.image_repeat:
                onRepeat(v);
                break;

            case R.id.image_shuffle:
                onShuffle(v);
                break;
        }
    }

    private void selectFirstItem() {
        if (mMusicList == null || mMusicList.isEmpty()) {
            return;
        }

        mSelectedMusicIndex = 0;
        onLoadMusic(mMusicList.get(0));

    }

    private void createMediaMetaDataList() {
        mMediaMetaDataArrayList.clear();
        for (MusicListItem musicItem :
                mMusicList) {
            MediaMetaData infoData = new MediaMetaData();
            infoData.setMediaId(String.valueOf(musicItem.getId()));
            infoData.setMediaUrl(musicItem.getMusicUrl());
            infoData.setMediaTitle(musicItem.getTitle());
            infoData.setMediaArtist(musicItem.getArtist());
            infoData.setAvatar(musicItem.getAvatar());
            mMediaMetaDataArrayList.add(infoData);
        }
        mStreamingManager.setMediaList(mMediaMetaDataArrayList);
    }

    @Override
    public void onClickPlayMusic(MusicListItem musicListItem) {
        /*setMusicPlayer(musicListItem);
        if (mMediaMetaDataArrayList != null && !mMediaMetaDataArrayList.isEmpty()) {
            mStreamingManager.onLoad(mMediaMetaDataArrayList.get(mSelectedMusicIndex));
        }

        // this will start secondary bar.. (loading bar)
        if (mStreamingManager != null && mStreamingManager.audioPlayback != null && mStreamingManager.audioPlayback.mMediaPlayer != null) {
            mStreamingManager.audioPlayback.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
                    player_ticker_seekbar.setSecondaryProgress(progress);
                }
            });
        }*/
        if (mMusicList == null || mMusicList.isEmpty()) return;
        mSelectedMusicIndex = mMusicList.indexOf(musicListItem);
        setMusicPlayer(musicListItem);
        playSelectedMusic();

        // this will start secondary bar.. (loading bar)
        if (mStreamingManager != null && mStreamingManager.audioPlayback != null && mStreamingManager.audioPlayback.mMediaPlayer != null) {
            mStreamingManager.audioPlayback.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
                    player_ticker_seekbar.setSecondaryProgress(progress);
                }
            });
        }
    }

    private void setMusicPlayer(MusicListItem musicListItem) {
        player_ticker_title.setText(musicListItem.getTitle());
        artist_name.setText(musicListItem.getArtist());
        //player_ticker_title.startAnimation((Animation) AnimationUtils.loadAnimation(getActivity(),R.anim.scroll));
        player_ticker_title.setSelected(true);
        player_ticker_elapsed_time.setText("00:00");
        player_ticker_total_time.setText("00:00");
        player_ticker_seekbar.setProgress(0);
        player_ticker_seekbar.setSecondaryProgress(0);
        if (mStreamingManager != null && mStreamingManager.audioPlayback != null && mStreamingManager.audioPlayback.mMediaPlayer != null) {
            mStreamingManager.audioPlayback.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
                    player_ticker_seekbar.setSecondaryProgress(progress);
                }
            });
        }

        // we also have to highlight the music from the list and scroll to it...
        mAdapter.setSelectedItem(musicListItem);
        ((LinearLayoutManager) music_list_recycler_view.getLayoutManager()).scrollToPositionWithOffset(mAdapter.getPosition(musicListItem), 20);
    }


    private void playMusic() {
        if(currentScreen.equals(KEY_SCREEN_CHAT)) {
            if (mStreamingManager.getCurrentAudio() != null && mStreamingManager.getCurrentAudio().getMediaUrl().equals(String.valueOf(musicItem.getMusicUrl()))) {
                mStreamingManager.handlePlayRequest();
            } else {
                playSelectedMusic();
            }
            //check_chat_music = true;
        } else {
            /*if(check_chat_music == true) {
                playSelectedMusic();
                check_chat_music = false;
            } else {
                mStreamingManager.handlePlayRequest();
            }*/
            mStreamingManager.handlePlayRequest();
        }
    }

    private void pauseMusic() {
        mStreamingManager.handlePauseRequest();
    }

    // View onClick Listener...
    public void onPlayPause(View view) {
        if (mStreamingManager == null) return;
        if(currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT) && mStreamingManager.isPlaying() && !mStreamingManager.getCurrentAudio().getMediaUrl().equals(String.valueOf(musicItem.getMusicUrl()))) {
            pauseMusic();
        }
        if (mStreamingManager.isPlaying()) {
//            Log.v(TAG, "HAHAHAHAHAHA pause Music");
            pauseMusic();
        } else {
//            Log.v(TAG, "HAHAHAHAHAHA play Music");
            playMusic();
        }
        if (mStreamingManager.getCurrentAudio() == null) {
//            Log.v(TAG, "HAHAHAHAHAHA");
            playSelectedMusic();
        }
        // this will start secondary bar.. (loading bar)
        if (mStreamingManager != null && mStreamingManager.audioPlayback != null && mStreamingManager.audioPlayback.mMediaPlayer != null) {
            mStreamingManager.audioPlayback.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
                    player_ticker_seekbar.setSecondaryProgress(progress);
                }
            });
        }
    }

    // View onClick Listener...
    public void onPlayNext(View view) {
        // implement Shuffle...
        if (image_shuffle.isSelected() && mMusicList.size() > 1) {
            Random r = new Random();
            int randomIndex = r.nextInt(mMusicList.size());

            while (randomIndex == mSelectedMusicIndex) {
                randomIndex = r.nextInt(mMusicList.size());
            }
            mSelectedMusicIndex = randomIndex;
        }
        // if not shuffle and  last music is not playing...
        else if (mSelectedMusicIndex + 1 < mMusicList.size()) {
            mSelectedMusicIndex = mSelectedMusicIndex + 1;
        }
        // else if last music....
        else if ((mSelectedMusicIndex + 1 >= mMusicList.size())) {
            mSelectedMusicIndex = 0;
        }
        // else (last and repeat is off)
        else { //Reached last track
            mSelectedMusicIndex = 0;
        }

        setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
        playSelectedMusic();
    }

    // View onClick Listener...
    public void onPlayBack(View view) {
        // implement Shuffle...
        if (image_shuffle.isSelected() && mMusicList.size() > 1) {
            Random r = new Random();
            int randomIndex = r.nextInt(mMusicList.size());

            while (randomIndex == mSelectedMusicIndex) {
                randomIndex = r.nextInt(mMusicList.size());
            }
            mSelectedMusicIndex = randomIndex;
        }
        // if neither shuffle nor first track...
        else if (mSelectedMusicIndex - 1 >= 0) {
            mSelectedMusicIndex = mSelectedMusicIndex - 1;

        }
        // else if first track....
        else if ((mSelectedMusicIndex - 1 < 0)) {
            mSelectedMusicIndex = mMusicList.size() - 1;
        }
        // else (first track)
        else { // reached first track
            mSelectedMusicIndex = mMusicList.size() - 1;
        }
        setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
        playSelectedMusic();
    }

    // View onClick Listener...
    public void onRepeat(View view) {
        AudioStreamingManager.repeat_status1++;
        if(AudioStreamingManager.repeat_status1 == 0) {
            image_repeat.setImageResource(R.drawable.player_repeat);
        } else if(AudioStreamingManager.repeat_status1 == 1) {
            image_repeat.setImageResource(R.drawable.player_repeat_active);
        } else if(AudioStreamingManager.repeat_status1 == 2) {
            image_repeat.setImageResource(R.drawable.repeat_single);
        } else if(AudioStreamingManager.repeat_status1 == 3) {
            image_repeat.setImageResource(R.drawable.player_repeat);
            AudioStreamingManager.repeat_status1 = 0;
        }
        image_shuffle.setSelected(false);
    }

    // View onClick Listener...
    public void onShuffle(View view) {
        image_repeat.setSelected(false);
        view.setSelected(!view.isSelected());
        AudioStreamingManager.shuffle_status = view.isSelected();
    }
    private void setupClickListeners() {

        player_ticker_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT) && mStreamingManager.isPlaying() && !mStreamingManager.getCurrentAudio().getMediaUrl().equals(String.valueOf(musicItem.getMusicUrl()))) {
                    prevProgress = progress;
                    return;
                }
                if (fromUser) {
                    int duration = 0;
                    try {
                        if(mStreamingManager.getCurrentAudio() != null) {
                            duration = Integer.parseInt(mStreamingManager.getCurrentAudio().getMediaDuration());
                        } else {
                            prevProgress = progress;
                        }
                    } catch (NumberFormatException e) {
                        duration = 0;
                        mStreamingManager.onSeekTo(duration * progress / 100);
                    }
                    mStreamingManager.onSeekTo(duration * progress / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MusicRecyclerViewAdapter) music_list_recycler_view.getAdapter()).mItemManger.closeAllItems();
    }

    public void unSubscribeCallBackFragment(){
        mStreamingManager.unSubscribeCallBack();
    }

    @Override
    public void onStop() {
        super.onStop();
        shuffle_repeat_prefs.edit().putBoolean("shuffle_status", AudioStreamingManager.shuffle_status).apply();
        shuffle_repeat_prefs.edit().putInt("repeat_status", AudioStreamingManager.repeat_status1).apply();
        /*if (mStreamingManager != null && ((!currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT)))) {
            mStreamingManager.unSubscribeCallBack();
        }*/
        //mStreamingManager.unSubscribeCallBack();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mStreamingManager != null) {
            mStreamingManager.subscribesCallBack(this);
        }
    }

    // Load apis for the screen
    // group should be null if not getting music from groups...
    private void onLoadService() {

        mainLoadingManger.showLoadingIcon();

        String action = null;
        int selectedScreen = -1;
        boolean showBorder = false;

        // getting the selected screen
        if (currentScreen.equals(KEY_SCREEN_INBOX)) {
            action = "get-inbox";
            selectedScreen = MusicRecyclerViewAdapter.KEY_INBOX_SCREEN;
            showBorder = true;

        } else if (currentScreen.equals(KEY_SCREEN_UPLOADS)) {
            action = "get-uploads";
            selectedScreen = MusicRecyclerViewAdapter.KEY_UPLOADS_SCREEN;

        } else if (currentScreen.equals(KEY_SCREEN_FAVORITE)) {
            action = "get-favourite";
            selectedScreen = MusicRecyclerViewAdapter.KEY_FAVORITES_SCREEN;

        } else if (currentScreen.equals(KEY_SCREEN_GROUP_MUSIC)) {
            action = "get-group_music";
            selectedScreen = MusicRecyclerViewAdapter.KEY_GROUPS_SCREEN;

        } else {
            return;
        }

        // setting url and parameters
        String url;
        if (!currentScreen.equals(KEY_SCREEN_GROUP_MUSIC)) {
            url = BuildConfig.BASE_URL + "/" + action + "?user_id=" + mUserModel.getId();
        } else {
            url = BuildConfig.BASE_URL + "/" + action + "?group_id=" + currentGroup.getId() + "&user_id=" + mUserModel.getId();
        }

        Log.v(TAG, "1129 Calling URL:\n" + url);
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);
        final boolean finalShowBorder = showBorder;
        final int finalSelectedScreen = selectedScreen;
        volleyRequest.requestServer(Request.Method.GET, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mainLoadingManger.hideLoadingIcon();

                Log.v(TAG, "1129 Response:\n" + response.toString());
                if (!response.optBoolean("error", true)) {

                    mMusicList.clear();
                    mMusicList = Utils.parseJsonArray(response.optJSONArray("data").toString(),
                            new TypeToken<List<MusicListItem>>() {
                            }.getType());

                    mAdapter.setData(mMusicList, currentGroup, finalShowBorder, finalSelectedScreen);
                    createMediaMetaDataList();
                    //when notification is clicked then play specific song
                    if(activityName != null && (activityName.equals(NotificationsAdapter.ACTIVITY_NAME) || activityName.equals(MyFirebaseMessagingService.ACTIVITY_NAME)) && notficationSongTitle != null) {
                        playSpecificSong();
                    }
                    else {
                        if(!mStreamingManager.isPlaying()) {
                            selectFirstItem();
                        } else {
                            //if same activity in which the music is
                            String currMediaTitle = mStreamingManager.getCurrentAudio().getMediaTitle();
                            int i = 0;
                            while ( !mMusicList.isEmpty() && i < mMusicList.size() && !currMediaTitle.equals(mMusicList.get(i).getTitle())) {
                                i++;
                            }
                            if(!mMusicList.isEmpty() && i >= 0 && i < mMusicList.size()) {
                                mSelectedMusicIndex = i;
                                player_ticker_title.setText(mStreamingManager.getCurrentAudio().getMediaTitle());
                                player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
                                mAdapter.setSelectedItem(mMusicList.get(i));
                            } else {
                                mSelectedMusicIndex = -1;
                                player_ticker_title.setText(mStreamingManager.getCurrentAudio().getMediaTitle());
                                player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
                            }
                        }
                    }
                    showHideMusicPlayer();
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

    // Loads music item but doesn't play it...
    public void onLoadMusic(MusicListItem musicListItem) {
        if (mMusicList == null || mMusicList.isEmpty()) return;
        mSelectedMusicIndex = mMusicList.indexOf(musicListItem);
        if (!mStreamingManager.isPlaying()) {
            setMusicPlayer(musicListItem);
        } else if(currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT)) {
            setMusicPlayer(musicListItem);
        } else {
            player_ticker_title.setText(mStreamingManager.getCurrentAudio().getMediaTitle());
            player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
        }

//        if (mMediaMetaDataArrayList != null && !mMediaMetaDataArrayList.isEmpty()) {
//            mStreamingManager.onLoad(mMediaMetaDataArrayList.get(mSelectedMusicIndex));
//        }
//
//        // this will start secondary bar.. (loading bar)
//        if (mStreamingManager != null && mStreamingManager.audioPlayback != null && mStreamingManager.audioPlayback.mMediaPlayer != null) {
//            mStreamingManager.audioPlayback.mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
//                @Override
//                public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
//                    player_ticker_seekbar.setSecondaryProgress(progress);
//                }
//            });
//        }
    }

    @Override
    public void onClickFavoriteMusic(final MusicListItem musicListItem, final NavGroupItem group) {
        if (group == null) {

            // if we are in favorites page... we can have tracks from both inbox and from groups...
            // we need to use different api for group favorites
            if (currentScreen.equals(KEY_SCREEN_FAVORITE) && musicListItem.getType().equalsIgnoreCase("group")) {


                UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(getActivity(), UserSharedPreferences.USER_MODEL), UserModel.class);
                String url = BuildConfig.BASE_URL + "/set-group-favorite";
                VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "set-favorite", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("music_id", String.valueOf(musicListItem.getUploadsId()));
                params.put("group_id", String.valueOf((musicListItem.getGroupID())));

                Log.d("Request Params : ", params.toString());
                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response  : ", response.toString());
                        if (!response.optBoolean("error", true)) {
                            CustomToast.makeText(getActivity(), response.optString("result"), Toast.LENGTH_LONG).show();
                            try {
                                if (response.getBoolean("favorited")) {
                                    musicListItem.setFavorite(1);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    musicListItem.setFavorite(0);
                                    mAdapter.notifyDataSetChanged();
                                }

                                // and if we are in favorites screen, remove it from adapter as well..
                                if (currentScreen.equals(KEY_SCREEN_FAVORITE)) {
                                    mMusicList.remove(musicListItem);
                                    mAdapter.notifyMusicItemRemoved(musicListItem);
                                    showHideMusicPlayer();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                        Log.d("", "");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });


            } else {
                UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(getActivity(), UserSharedPreferences.USER_MODEL), UserModel.class);
                String url = BuildConfig.BASE_URL + "/set-favorite";/*?upload_id=" + musicListItem.getId() +
                "&user_ids=" + selectedArtistsIds +
                "&group_ids=" + selectedGroupsIds +
                "&message_text="+et_leave_message.getText().toString();*/
                VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "set-favorite", true);
                HashMap<String, String> params = new HashMap<>();
                params.put("inbox_id", String.valueOf(musicListItem.getId()));
                params.put("user_id", String.valueOf(mUserModel.getId()));
                Log.d(TAG, params.toString());
                volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response  : ", response.toString());
                        if (!response.optBoolean("error", true)) {
                            CustomToast.makeText(getActivity(), response.optString("result"), Toast.LENGTH_LONG).show();
                            try {
                                if (response.getBoolean("favorited")) {
                                    musicListItem.setFavorite(1);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    musicListItem.setFavorite(0);
                                    mAdapter.notifyDataSetChanged();

                                    // and if we are in favorites screen, remove it from adapter as well..
                                    if (currentScreen.equals(KEY_SCREEN_FAVORITE)) {
                                        mMusicList.remove(musicListItem);
                                        mAdapter.notifyMusicItemRemoved(musicListItem);
                                        showHideMusicPlayer();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        Log.d("", "");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });
            }
        } else {

            UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(getActivity(), UserSharedPreferences.USER_MODEL), UserModel.class);
            String url = BuildConfig.BASE_URL + "/set-group-favorite";/*?upload_id=" + musicListItem.getId() +
                "&user_ids=" + selectedArtistsIds +
                "&group_ids=" + selectedGroupsIds +
                "&message_text="+et_leave_message.getText().toString();*/
            VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "set-favorite", true);
            HashMap<String, String> params = new HashMap<>();
            params.put("music_id", String.valueOf(musicListItem.getUploadsId()));
            params.put("user_id", String.valueOf(mUserModel.getId()));
            params.put("group_id", String.valueOf(group.getId()));

            Log.d("Request Params : ", params.toString());
            volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Response  : ", response.toString());
                    if (!response.optBoolean("error", true)) {
                        CustomToast.makeText(getActivity(), response.optString("result"), Toast.LENGTH_LONG).show();
                        try {
                            if (response.getBoolean("favorited")) {
                                musicListItem.setFavorite(1);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                musicListItem.setFavorite(0);
                                mAdapter.notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                    Log.d("", "");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("", "");
                }
            });
        }
    }

    @Override
    public void onClickForwardMusic(MusicListItem musicListItem) {
        Intent intent = new Intent(getActivity(), SendSongPopupActivity.class);
        intent.putExtra(UIConstants.IntentExtras.SELECTED_MUSIC, musicListItem);
        intent.putExtra(UIConstants.IntentExtras.TYPE_SELECT_FRIENDS, "send_music");
        startActivity(intent);
        //Toast.makeText(this, "forward", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mStreamingManager.cleanupPlayer(true, true);
    }

    @Override
    public void onClickDeleteMusic(final MusicListItem musicListItem, final NavGroupItem group) {


        String url = BuildConfig.BASE_URL + "/remove_music?user_id=" + mUserModel.getId() + "&music_id=" + musicListItem.getId()
                + "&type=";
        if (currentScreen.equals(KEY_SCREEN_INBOX)) {
            url += "inbox";
        } else if (currentScreen.equals(KEY_SCREEN_UPLOADS)) {
            url += "uploads";
        } else if (currentScreen.equals(KEY_SCREEN_FAVORITE)) {
            url += "favourite";
        } else if (currentScreen.equals(KEY_SCREEN_GROUP_MUSIC)) {
            url = BuildConfig.BASE_URL + "/groups/remove_music?music_id=" + musicListItem.getId()
                    + "&group_id=" + group.getId();
        } else {
            return;
        }

        final int[] count = new int[1];
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "delete_music", true);
        volleyRequest.requestServer(Request.Method.GET, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
                    mMusicList.remove(musicListItem);
                    mAdapter.notifyMusicItemRemoved(musicListItem);

                    showHideMusicPlayer();
                }
                Log.d("", "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });


    }

    // hides music player if no items in playlist...
    private void showHideMusicPlayer() {
        if (mAdapter.getItemCount() == 0 &&  mStreamingManager != null && !mStreamingManager.isPlaying()) {

            android.support.constraint.ConstraintLayout musicplayer = view.findViewById(R.id.music_player);
            musicplayer.setVisibility(View.GONE);
        } else {

            android.support.constraint.ConstraintLayout musicplayer = view.findViewById(R.id.music_player);
            musicplayer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickMessageMusic(MusicListItem musicListItem) {

//        UserSharedPreferences.putCurrentMusicItemObject(MainActivity.this, UIConstants.IntentExtras.SELECTED_MUSIC_ITEM, musicListItem);
        Intent intent = new Intent(getActivity(), ChatActivity.class);


        intent.setAction(ACTION_OPEN_CHAT_BY_SONG);

        intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, musicListItem.getUploaderId());
        intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, musicListItem.getFullName());
        intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, musicListItem.getUploadsId());
        intent.putExtra(ChatActivity.KEY_MUSIC_TITLE, musicListItem.getTitle());

        startActivity(intent);

    }

    @Override
    public Context getCallingContext() {
        return getActivity();
    }

    @Override
    public void updatePlaybackState(int state) {
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                progressbar.setVisibility(View.GONE);
                player_ticker_play_pause_image.setVisibility(View.VISIBLE);
                player_ticker_play_pause_image.setImageResource(R.drawable.player_pause_active);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                player_ticker_play_pause_image.setVisibility(View.VISIBLE);
                player_ticker_play_pause_image.setImageResource(R.drawable.player_big_play_active);
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                player_ticker_play_pause_image.setVisibility(View.VISIBLE);
                player_ticker_play_pause_image.setImageResource(R.drawable.player_big_play_active);
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                progressbar.setVisibility(View.VISIBLE);
                player_ticker_play_pause_image.setImageResource(R.drawable.bar_selector_white);
                break;
        }
    }

    @Override
    public void playSongComplete() {
        mStreamingManager.cleanupPlayer(true, false);
        if(AudioStreamingManager.repeat_status1 == 2) {
            player_ticker_seekbar.setProgress(0);
            playMusic();
        } else if ((mSelectedMusicIndex + 1 >= mMusicList.size()) && (AudioStreamingManager.repeat_status1 == 1)) {
            mSelectedMusicIndex = 0;
            setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
            playSelectedMusic();
        } else if (image_shuffle.isSelected() && mMusicList.size() > 0) {
            Random r = new Random();
            mSelectedMusicIndex = r.nextInt(mMusicList.size() - 1);
            setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
            playSelectedMusic();
        } else if (mSelectedMusicIndex < mMusicList.size() - 1) {
            Log.v("TESTTT", "Playing Next");
            mSelectedMusicIndex++;
            setMusicPlayer(mMusicList.get(mSelectedMusicIndex));
            playSelectedMusic();
        }
    }
    // This function will call media streaming manager's play method for selected music index...
    //
    // If selected music index is not opened by the user before,
    // we will inform server to mark it opened...
    private void playSelectedMusic() {
        if (mMediaMetaDataArrayList != null && !mMediaMetaDataArrayList.isEmpty()) {
            mStreamingManager.onPlay(mMediaMetaDataArrayList.get(mSelectedMusicIndex));
//            if(mStreamingManager.getCurrentAudio() != null) {
//                mStreamingManager.onPlay(mStreamingManager.getCurrentAudio());
//            }
//            else {
//                mStreamingManager.onPlay(mMediaMetaDataArrayList.get(mSelectedMusicIndex));
//            }
        }

        // lets find out whether current playing music is already opened or not...
        // if not opened before,
        if (mMusicList.get(mSelectedMusicIndex).getCheck() == 0) {

            // we make a final copy of music index to use in the Api response...
            final int mSelectedMusicIndexFinal = mSelectedMusicIndex;
            String url = BuildConfig.BASE_URL + "/" + "songs/mark-as-opened"
                    + "?user_id=" + mUserModel.getId()
                    + "&music_id=" + mMusicList.get(mSelectedMusicIndex).getId()
                    + "&group_inbox=inbox";

            Log.v(TAG, "3459 Calling URL:\n" + url);
            VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);
            volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "3459 Response:\n" + response.toString());

                    // if successful, we need to update our data in model and notify adapter...
                    // this will hide unread mark from the item
                    mMusicList.get(mSelectedMusicIndexFinal).setCheck(1);
                    mAdapter.notifyItemChanged(mSelectedMusicIndexFinal);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "3459 Error Response:\n" + error.getMessage());
                }
            });

        }
    }

    @Override
    public void currentSeekBarPosition(int progress) {
//        Log.d(TAG, "current seek bar progress: " + progress);
        if (mStreamingManager == null || mStreamingManager.audioPlayback == null
                || mStreamingManager.audioPlayback.mMediaPlayer == null) return;
        if(currentScreen.equals(MusicPlayerFragment.KEY_SCREEN_CHAT) && !mStreamingManager.getCurrentAudio().getMediaUrl().equals(String.valueOf(musicItem.getMusicUrl()))) return;
        if(prevProgress != 0) {
            mStreamingManager.onSeekTo(mStreamingManager.audioPlayback.mMediaPlayer.getDuration() * prevProgress / 100);
            prevProgress = 0;
        }
        mStreamingManager.getCurrentAudio().setMediaDuration(String.valueOf(mStreamingManager.audioPlayback.mMediaPlayer.getDuration()));

        Calendar calendarTotal = Calendar.getInstance();
        calendarTotal.setTimeInMillis(mStreamingManager.audioPlayback.mMediaPlayer.getDuration());

        Calendar calendarElapsed = Calendar.getInstance();
        calendarElapsed.setTimeInMillis(mStreamingManager.audioPlayback.mMediaPlayer.getCurrentPosition());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        player_ticker_elapsed_time.setText(simpleDateFormat.format(calendarElapsed.getTime()));
        player_ticker_total_time.setText(simpleDateFormat.format(calendarTotal.getTime()));

        int seekbarPostion = (int) (((float) mStreamingManager.audioPlayback.mMediaPlayer.getCurrentPosition() / mStreamingManager.audioPlayback.mMediaPlayer.getDuration()) * 100);
        player_ticker_seekbar.setProgress(seekbarPostion);
    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {
        Log.d("", "");
    }

    @Override
    public void playNext(int indexP, MediaMetaData CurrentAudio) {
        onPlayNext(null);
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        onPlayBack(null);
    }


    // returns intent for music player notification...
    private PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setAction(UIConstants.IntentActions.PLAYER_NOTIFICATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(getActivity(), 0, intent, 0);
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getNotficationSongTitle() {
        return notficationSongTitle;
    }

    public void setNotficationSongTitle(String notficationSongTitle) {
        this.notficationSongTitle = notficationSongTitle;
    }
}
