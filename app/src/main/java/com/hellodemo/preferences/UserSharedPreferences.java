package com.hellodemo.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.hellodemo.LoginActivity;
import com.hellodemo.R;
import com.hellodemo.RegisterActivity;
import com.hellodemo.models.MusicListItem;

import java.io.IOException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by new user on 2/13/2018.
 */

public class UserSharedPreferences {

    private static final String HELLO_PREF = "hello_preferences";
    public static final String USER_MODEL = "user_model";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_LOGGED_IN = "is_user_logged_in";
    public static final String MENU_MODEL = "menu_model";
    public static final String KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND = "KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND";

    private static String KEYS[] = {USER_MODEL, USER_LOGGED_IN, MENU_MODEL, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND};


    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }

    public static void deleteSharedPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        for (String key : KEYS) {
            sharedPref.edit().remove(key).commit();
        }
    }

    public static void putCurrentMusicItemObject(Context context, String key, MusicListItem musicListItem) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String musicitem_json = gson.toJson(musicListItem);
        editor.putString(key, musicitem_json);
        editor.commit();
    }

    public static MusicListItem getCurrentMusicItemObject(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String musicitem_json = sharedPreferences.getString(key, "");
        Gson gson = new Gson();
        return gson.fromJson(musicitem_json, MusicListItem.class);
    }


    public static void addUserIDToBeAddedAsFriend(Context context, String userID) {
        ArrayList<String> list = getStringArrayList(context, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND);

        // check if already there in the queue...
        boolean alreadyAdded = false;
        for (String s : list) {
            alreadyAdded = s.equals(userID);
            if (alreadyAdded)
                break;
        }

        if (!alreadyAdded) {
            list.add(userID);
            saveStringArrayList(context, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND, list);
        }
    }


    public static ArrayList<String> removeUserIDToBeAddedAsFriendFromQueue(Context context, String userID) {
        ArrayList<String> list = getStringArrayList(context, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND);

        ArrayList<String> toDelete = new ArrayList<String>();

        // find the ids to be deleted...
        for (String s : list) {
            if (s.equals(userID)) {
                toDelete.add(s);
            }
        }
        // now delete...
        for (String s : toDelete) {
            list.remove(s);
        }

        saveStringArrayList(context, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND, list);
        return list;
    }

    public static ArrayList<String> getUserIDsToBeAddedAsFriend(Context context) {
        return getStringArrayList(context, KEY_QR_CODES_TO_BE_ADDED_AS_FRIEND);
    }

    private static void saveStringArrayList(Context context, String key, ArrayList<String> list) {
        Gson gson = new Gson();
        String jsonText = gson.toJson(list);

        // save the task list to preference
        SharedPreferences prefs = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, jsonText);
        editor.commit();
    }

    private static ArrayList<String> getStringArrayList(Context context, String key) {
        //Retrieve the values
        Gson gson = new Gson();
        SharedPreferences sharedPref = context.getSharedPreferences(
                HELLO_PREF, Context.MODE_PRIVATE);
        String jsonText = sharedPref.getString(key, null);

        ArrayList<String> result = new ArrayList<String>();
        if (jsonText == null) {
            return result;
        }

        String[] text = gson.fromJson(jsonText, String[].class);  //EDIT: gso to gson
        result.addAll(Arrays.asList(text));
        return result;
    }
}
