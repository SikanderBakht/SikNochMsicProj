package com.hellodemo.utils;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.GsonBuilder;
import com.hellodemo.R;
import com.hellodemo.models.UserModel;
import com.hellodemo.preferences.UserSharedPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by new user on 2/13/2018.
 */

public class Utils {
    public static int getColor(Context context, int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(color);
        }
        return context.getResources().getColor(color);
    }

    public static <T extends Object> T parseJson(String json, Class<T> className){
        return new GsonBuilder()
                .serializeNulls()
                .create()
                .fromJson(json, className);
    }

    public static <T extends Object> ArrayList<T> parseJsonArray(String data, Type typeToken) {
        return new GsonBuilder()
                .serializeNulls()
                .create()
                .fromJson(data, typeToken);
    }

    public static String getAccessToken(Context context){
        String userModelStr = UserSharedPreferences.getString(context,UserSharedPreferences.USER_MODEL);
        if (TextUtils.isEmpty(userModelStr))return userModelStr;

        return parseJson(userModelStr, UserModel.class).getAccessToken();
    }

    public static void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {

        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }

            tabLayout.requestLayout();
        }
    }
}
