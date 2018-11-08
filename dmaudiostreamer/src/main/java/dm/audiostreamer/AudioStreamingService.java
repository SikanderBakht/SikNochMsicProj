/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class AudioStreamingService extends Service implements NotificationManager.NotificationCenterDelegate {
    private static final String TAG = Logger.makeLogTag(AudioStreamingService.class);

    public static final String MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID = "dm.audiostreamer.NOTIFICATIONCHANNEL_ID";
    public static final String MUSIC_PLAYER_NOTIFICATION_CHANNEL_NAME = "NOTIFICATION CHANNEL NAME";

    private android.app.NotificationManager mManager;
    String profilepicture;
    public static final String EXTRA_CONNECTED_CAST = "dm.audiostreaming.CAST_NAME";
    public static final String ACTION_CMD = "dm.audiostreaming.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    private static final int STOP_DELAY = 30000;

    public static final String NOTIFY_PREVIOUS = "dm.audiostreamer.previous";
    public static final String NOTIFY_CLOSE = "dm.audiostreamer.close";
    public static final String NOTIFY_PAUSE = "dm.audiostreamer.pause";
    public static final String NOTIFY_PLAY = "dm.audiostreamer.play";
    public static final String NOTIFY_NEXT = "dm.audiostreamer.next";

    private static boolean supportBigNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static boolean supportLockScreenControls = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private RemoteControlClient remoteControlClient;
    private AudioManager audioManager;
    private AudioStreamingManager audioStreamingManager;
    private PhoneStateListener phoneStateListener;
    public PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        audioStreamingManager = AudioStreamingManager.getInstance(AudioStreamingService.this);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().addObserver(this, NotificationManager.setAnyPendingIntent);
        NotificationManager.getInstance().addObserver(this, NotificationManager.audioPlayStateChanged);
        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (audioStreamingManager.isPlaying()) {
                            audioStreamingManager.handlePauseRequest();
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        try {
            MediaMetaData messageObject = AudioStreamingManager.getInstance(AudioStreamingService.this).getCurrentAudio();
            if (messageObject == null) {
                Handler handler = new Handler(AudioStreamingService.this.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                });
                return START_STICKY;
            }

            if (supportLockScreenControls) {
                ComponentName remoteComponentName = new ComponentName(getApplicationContext(), AudioStreamingReceiver.class.getName());
                try {
                    if (remoteControlClient == null) {
                        audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                        mediaButtonIntent.setComponent(remoteComponentName);
                        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                        remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                        audioManager.registerRemoteControlClient(remoteControlClient);
                    }
                    remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
                } catch (Exception e) {
                    Log.e("tmessages", e.toString());
                }
            }

            createNotification(messageObject);
        } catch (Exception e) {

        }
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    audioStreamingManager.handlePauseRequest();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    //TODO FOR EXTERNAL DEVICE
                }
            }
        }
        return START_NOT_STICKY;
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inJustDecodeBounds = false;
            options.inSampleSize = 3;
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createChannels() {
        // create android channel
        NotificationChannel helloDemoChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            helloDemoChannel = new NotificationChannel(MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID,
                    MUSIC_PLAYER_NOTIFICATION_CHANNEL_NAME, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            helloDemoChannel.enableLights(false);
            // Sets whether notification posted to this channel should vibrate.
            helloDemoChannel.enableVibration(false);
            helloDemoChannel.setSound(null,null);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            helloDemoChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(helloDemoChannel);
        }
    }

    public void createNotification(MediaMetaData mSongDetail) {
        try {
            String songName = mSongDetail.getMediaTitle();
            String authorName = mSongDetail.getMediaArtist();
            String albumName = mSongDetail.getMediaAlbum();
            String avatar = mSongDetail.getAvatar();
            Log.v("Profile picture", avatar);


            Bitmap avatarBitmap = getBitmapFromURL(avatar);
            LayoutInflater li = LayoutInflater.from(getApplicationContext());
            View promptsView = li.inflate(R.layout.player_big_notification, null);

            ImageView notification_picture = promptsView.findViewById(R.id.player_album_art);
            //Picasso.with(getApplicationContext()).load(avatar).into(notification_picture);
            MediaMetaData audioInfo = AudioStreamingManager.getInstance(AudioStreamingService.this).getCurrentAudio();

            RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_small_notification);
            RemoteViews expandedView = null;
            if (supportBigNotifications) {
                expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.player_big_notification);
            }


            Notification notification = null;
            if (pendingIntent != null) {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.player)
                        .setContentIntent(pendingIntent).setContentTitle(songName);

                notificationBuilder.setVisibility(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
                notification = notificationBuilder.build();
            } else {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MUSIC_PLAYER_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.player)
                        .setContentTitle(songName);
                notificationBuilder.setVisibility(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
                notification = notificationBuilder.build();
            }

            notification.contentView = simpleContentView;
            if (supportBigNotifications) {
                notification.bigContentView = expandedView;
            }

            setListeners(simpleContentView);
            if (supportBigNotifications) {
                setListeners(expandedView);
            }

            notification.contentView.setImageViewBitmap(R.id.player_album_art, avatarBitmap);
            notification.bigContentView.setImageViewBitmap(R.id.player_album_art, avatarBitmap);

            notification.contentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            notification.contentView.setViewVisibility(R.id.player_next, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
            if (supportBigNotifications) {
                // notification.bigContentView.setImageViewUri(R.id.player_album_art,  uri);
                // notification.contentView.setImageViewBitmap(R.id.player_album_art, b);
                notification.bigContentView.setViewVisibility(R.id.player_next, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_previous, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.player_progress_bar, View.GONE);
            }

            if (!AudioStreamingManager.getInstance(AudioStreamingService.this).isPlaying()) {
                notification.contentView.setViewVisibility(R.id.player_pause, View.GONE);
                notification.contentView.setViewVisibility(R.id.player_play, View.VISIBLE);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_pause, View.GONE);
                    notification.bigContentView.setViewVisibility(R.id.player_play, View.VISIBLE);
                }
            } else {
                notification.contentView.setViewVisibility(R.id.player_pause, View.VISIBLE);
                notification.contentView.setViewVisibility(R.id.player_play, View.GONE);
                if (supportBigNotifications) {
                    notification.bigContentView.setViewVisibility(R.id.player_pause, View.VISIBLE);
                    notification.bigContentView.setViewVisibility(R.id.player_play, View.GONE);
                }
            }

            notification.contentView.setTextViewText(R.id.player_song_name, songName);
            notification.contentView.setTextViewText(R.id.player_author_name, authorName);
            if (supportBigNotifications) {
                notification.bigContentView.setTextViewText(R.id.player_song_name, songName);
                notification.bigContentView.setTextViewText(R.id.player_author_name, authorName);
//                notification.bigContentView.setTextViewText(R.id.player_albumname, albumName);
            }
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(5, notification);

            if (remoteControlClient != null) {
                RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, authorName);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, songName);
                metadataEditor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListeners(RemoteViews view) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioStreamingReceiver.class).setAction(NOTIFY_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_previous, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioStreamingReceiver.class).setAction(NOTIFY_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_close, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioStreamingReceiver.class).setAction(NOTIFY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_pause, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioStreamingReceiver.class).setAction(NOTIFY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_next, pendingIntent);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AudioStreamingReceiver.class).setAction(NOTIFY_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.player_play, pendingIntent);
            //onTrackChanged(); if method on Audio Streaming manager wont work then will use this
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* if method on Audio Streaming manager wont work then will use this
    private void onTrackChanged() {
        MediaMetaData mSongDetail = AudioStreamingManager.getInstance(AudioStreamingService.this).getCurrentAudio();

        Intent i = new Intent("com.android.music.metachanged");
        i.putExtra("id", 1);
        i.putExtra("track", mSongDetail.getMediaTitle());
        i.putExtra("artist", mSongDetail.getMediaArtist());
        i.putExtra("album", mSongDetail.getMediaAlbum());
        i.putExtra("playing", "true");
        sendStickyBroadcast(i);

        RemoteControlClient.MetadataEditor ed = remoteControlClient.editMetadata(true);
        ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mSongDetail.getMediaTitle());
        ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, mSongDetail.getMediaAlbum());
        ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mSongDetail.getMediaArtist());
        ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, Long.parseLong(mSongDetail.getMediaDuration()));
        ed.apply();
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (remoteControlClient != null) {
            RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
            metadataEditor.clear();
            metadataEditor.apply();
            audioManager.unregisterRemoteControlClient(remoteControlClient);
        }
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioProgressDidChanged);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.audioPlayStateChanged);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.setAnyPendingIntent) {
            PendingIntent pendingIntent = (PendingIntent) args[0];
            if (pendingIntent != null) {
                this.pendingIntent = pendingIntent;
            }
        } else if (id == NotificationManager.audioPlayStateChanged) {
            MediaMetaData mSongDetail = AudioStreamingManager.getInstance(AudioStreamingService.this).getCurrentAudio();
            if (mSongDetail != null) {
                createNotification(mSongDetail);
            } else {
                stopSelf();
            }
        }
    }

    @Override
    public void newSongLoaded(Object... args) {

    }
}
