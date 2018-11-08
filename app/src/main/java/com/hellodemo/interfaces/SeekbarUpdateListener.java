package com.hellodemo.interfaces;

/**
 * Created by new user on 2/18/2018.
 */

public interface SeekbarUpdateListener {
    void onChangeSeekbar(int progress,int totalMilliSeconds, int elapsedMilliSeconds);
    void onDowloadSeekbar(int progress);
    void onCompletion();
    void onMediaPlayingError();
}
