package com.hellodemo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.hellodemo.interfaces.SeekbarUpdateListener;
import com.hellodemo.utils.CustomToast;

import java.io.IOException;

/**
 * Created by new user on 2/18/2018.
 */

public class HelloMediaPlayer implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private android.media.MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AppCompatActivity mActivity;
    private Handler mHandler;
    private SeekbarUpdateListener mSeekbarUpdateListener;
    private boolean isPrepared = false;

    public HelloMediaPlayer(AppCompatActivity appCompatActivity){
        mActivity = appCompatActivity;
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mHandler = new Handler();
    }

    void createMediaPlayer(String filePath){
        releaseMediaPlayer();
        int result = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            try {
                // Start playback.
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                mMediaPlayer.setOnErrorListener(this);
                primarySeekBarProgressUpdater();
            } catch (IOException e) {
                e.printStackTrace();
                if (mMediaPlayer != null)mMediaPlayer.release();
            }
            catch (IllegalStateException e){
                e.printStackTrace();
                if (mMediaPlayer != null)mMediaPlayer.release();
            }
        }
    }

    void playMusic(){
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
            primarySeekBarProgressUpdater();
        }
    }

    void pauseMusic(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    boolean isPlaying(){
        if (mMediaPlayer == null) return false;
        return mMediaPlayer.isPlaying();
    }

    public void releaseMediaPlayer() {
        mAudioManager.abandonAudioFocus(afChangeListener);
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    android.media.MediaPlayer.OnCompletionListener mOnCompletionListener = new android.media.MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(android.media.MediaPlayer mp) {
            //releaseMediaPlayer();
            if(mSeekbarUpdateListener != null){
                mSeekbarUpdateListener.onCompletion();
            }
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

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mSeekbarUpdateListener.onDowloadSeekbar(i);
    }

    private void primarySeekBarProgressUpdater() {
        if (mMediaPlayer == null) return;
        int mediaFileLengthInMilliseconds = mMediaPlayer.getDuration();
        // This math construction give a percentage of "was playing"/"song length"
        mSeekbarUpdateListener.onChangeSeekbar((int)(((float)mMediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100),
                mediaFileLengthInMilliseconds, mMediaPlayer.getCurrentPosition());
        if (mMediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            mHandler.postDelayed(notification,1000);
        }
    }

    public void setSeekbarUpdateListener(SeekbarUpdateListener seekbarUpdateListener) {
        mSeekbarUpdateListener = seekbarUpdateListener;
    }

    public int getDurationInMilliseconds() {
        if (mMediaPlayer != null){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int playPositionInMillisecconds) {
        mMediaPlayer.seekTo(playPositionInMillisecconds);
    }


    public void stopMusic() {
        mMediaPlayer.stop();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPrepared = true;
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        CustomToast.makeText(mActivity,"Error While Playing Music",Toast.LENGTH_LONG).show();
        if (mediaPlayer != null){
            mMediaPlayer.reset();
        }
        if (mSeekbarUpdateListener != null){
            mSeekbarUpdateListener.onMediaPlayingError();
        }
        return false;
    }

    public boolean isPrepared() {
        return isPrepared;
    }
}
