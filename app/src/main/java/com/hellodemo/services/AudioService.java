package com.hellodemo.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;

import com.hellodemo.R;
import com.hellodemo.utils.PermissionUtil;


/**
 * Created by Muzammil on 9/28/2016.
 * com.algotrick.quranforeveryone.services
 */

public class AudioService extends Service {

    public static final String TICKER_MESSAGE = "ticker_ui_message";
    public static boolean IS_SERVICE_RUNNING = false;
    public static final String START_ACTION = "start_action";
    public static final String STOP_ACTION = "stop_action";
    public static final String PAUSE_ACTION = "pause_action";
    public static final String ACTIVITY_ACTION = "activity_action";
    public static final String PLAY_ACTION = "play_action";
    public static final int NOTIFICATION_ID = 51214;
    public static final String UI_ACTION = "update_audio_ticker";
    public static boolean IS_PLAYING = false;

    MediaPlayer mMediaPlayer;
    AudioManager mAudioManager;

    public  static  String surrahName = "";
    public  static  String reciterName = "";

    public AudioService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null){
            return START_NOT_STICKY;
        }
        if (intent.getAction().equals(START_ACTION)){
            IS_SERVICE_RUNNING = true;
            IS_PLAYING = true;
            //TODO: File Path must be passed first
            handleActionStart("");
            sendBrodcast("start");
        }else if(intent.getAction().equals(PLAY_ACTION)){
            IS_PLAYING = true;
            //updateNotification(false);
            handleActionPlay();
            sendBrodcast("play");
        } else if (intent.getAction().equals(PAUSE_ACTION)){
            IS_PLAYING = false;
            //updateNotification(true);
            handleActionPause();
            sendBrodcast("pause");
        }else if (intent.getAction().equals(STOP_ACTION)){
            IS_SERVICE_RUNNING = false;
            IS_PLAYING = false;
            handleActionStop();
            sendBrodcast("stop");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void sendBrodcast(String message) {
        Intent intent = new Intent(UI_ACTION);
        intent.putExtra(TICKER_MESSAGE,message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleActionStop() {
        releaseMediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void releaseMediaPlayer() {
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    void handleActionStart(String filePath){
        releaseMediaPlayer();
        int result = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback.
            mMediaPlayer = MediaPlayer.create(this, Uri.parse(filePath));
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        }
    }

    private void handleActionPlay() {
        if (mMediaPlayer != null  && !mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }
    }

    private void handleActionPause(){
        if (mMediaPlayer != null  && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
            mAudioManager.abandonAudioFocus(afChangeListener);
            IS_SERVICE_RUNNING = false;
            IS_PLAYING = false;
            sendBrodcast("stop");
            stopForeground(true);
            stopSelf();
        }
    };

    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        // The AUDIOFOCUS_LOSS_TRANSIENT case means that we've lost audio focus for a
                        // short amount of time.
                        // Pause playback because your Audio Focus was
                        // temporarily stolen, but will be back soon.
                        // i.e. for a phone call
                        if (mMediaPlayer != null){
                            mMediaPlayer.pause();
                            //mMediaPlayer.seekTo(0);
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Stop playback, because you lost the Audio Focus.
                        // i.e. the user started some other playback app
                        // Remember to unregister your controls/buttons here.
                        // And release the kra — Audio Focus!
                        // You’re done.
                        mAudioManager.abandonAudioFocus(afChangeListener);
                        if (mMediaPlayer != null){
                            releaseMediaPlayer();
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        // The AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK case means that
                        // our app is allowed to continue playing sound but at a lower volume. We'll treat
                        // both cases the same way because our app is playing short sound files.

                        // Pause playback and reset player to the start of the file. That way, we can
                        // play the word from the beginning when we resume playback.
                        // Lower the volume, because something else is also
                        // playing audio over you.
                        // i.e. for notifications or navigation directions
                        // Depending on your audio playback, you may prefer to
                        // pause playback here instead. You do you.
                        if (mMediaPlayer != null){
                            mMediaPlayer.pause();
                            //mMediaPlayer.seekTo(0);
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                        // Resume playback, because you hold the Audio Focus
                        // again!
                        // i.e. the phone call ended or the nav directions
                        // are finished
                        // If you implement ducking and lower the volume, be
                        // sure to return it to normal here, as well.
                        if (mMediaPlayer != null){
                            mMediaPlayer.start();
                        }
                    }
                }
            };
}
