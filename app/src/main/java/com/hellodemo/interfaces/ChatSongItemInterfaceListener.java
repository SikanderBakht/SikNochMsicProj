package com.hellodemo.interfaces;

import com.hellodemo.models.Song;

public interface ChatSongItemInterfaceListener {
    void onClickDeleteMusic(Song musicListItem);
    void onClickArchiveMusic(Song musicListItem);
    void onClickSurface(Song musicListItem);
}
