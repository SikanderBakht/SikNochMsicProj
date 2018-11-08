package com.hellodemo.interfaces;

import android.content.Context;

import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.NavGroupItem;

/**
 * Created by new user on 2/24/2018.
 */

public interface MusicListInterfaceListener {
    void onClickFavoriteMusic(MusicListItem musicListItem, NavGroupItem group);
    void onClickForwardMusic(MusicListItem musicListItem);
    void onClickDeleteMusic(MusicListItem musicListItem, NavGroupItem mGroup);
    void onClickPlayMusic(MusicListItem musicListItem);
    void onClickMessageMusic(MusicListItem musicListItem);
    void onClickNotesMusic(MusicListItem musicListItem);
    Context getCallingContext();


}
