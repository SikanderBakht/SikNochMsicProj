package com.hellodemo.utils;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.hellodemo.R;

/**
 * Created by new user on 3/10/2018.
 */

public class CustomToast {

    public static Toast makeText(Context context, String text, int duration){
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast,null,false);
        ((AppCompatTextView)view.findViewById(R.id.toast_text)).setText(text);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 125);
        toast.setView(view);
        return toast;
    }
}
