package com.hellodemo.utils;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.hellodemo.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class LoadingManager {

    Context context;
    View content, loaderContainer;
    ImageView loader;

    /**
     *  Instantiates a loading manager class.
     * @param context context of the loading manager call
     * @param content content that is being loaded
     * @param loader loading icon gif view
     * @param loaderContainer View that holds loader
     */
    public LoadingManager(Context context, View content, ImageView loader, View loaderContainer){

        this.context = context;
        this.content = content;
        this.loader = loader;
        this.loaderContainer= loaderContainer;

        setupLoadingIcon();
        hideLoadingIcon();
    }

    // sets up loading icon...
    private void setupLoadingIcon() {

        Drawable drawable = loader.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

    }


    // hides the content and shows loading...
    public void showLoadingIcon() {
        content.setVisibility(View.INVISIBLE);
        loaderContainer.setVisibility(View.VISIBLE);
    }

    // hides the loading icon and shows content ...
    public void hideLoadingIcon() {

        content.setVisibility(View.VISIBLE);
        loaderContainer.setVisibility(View.INVISIBLE);
    }
}
