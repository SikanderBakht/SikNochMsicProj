package com.hellodemo.interfaces;

/**
 * Created by new user on 2/24/2018.
 */

public interface UIConstants {
    interface IntentActions {
        String OPEN_MUSIC_LIST_SCREEN = "selected_screen_position";
        String OPEN_MUSIC_LIST_GROUP = "selected_screen_position";
        String PLAYER_NOTIFICATION = "player_notification";
    }

    interface IntentExtras {
        String SCREEN_TITLE = "screen_title";
        String SELECTED_GROUP = "selected_group";
        String SCREEN_POSITION = "screen_position";
//        String CHAT_SCREEN_TARGET_USER = "target_user";
        String SELECTED_MUSIC = "music_list_item";
        String SELECTED_MUSIC_ITEM = "music_list_item";
//        String SELECTED_MUSIC_ID = "music_id";
        String SELECTED_GROUPS_IDS_JSON_STR = "selected_groups_ids_json";
        String SELECTED_ARTISTS_IDS_JSON_STR = "selected_artists_ids_json";
//        String CHAT_SCREEN_GROUP_ID = "group_id";
        String TYPE_SELECT_FRIENDS="type_select_friends";
        String CURRENT_GROUP_ID = "current_group_id";







        String TARGET_USER_FULL_NAME = "target_user_full_name";
    }

    public interface ResultCodes {
        int SEND_REQUEST_CODE = 100;
    }
}
