package com.hellodemo.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.hellodemo.R;
import com.hellodemo.utils.FontCache;

/**
 * Code written by Qandeel Abbassi on 2/11/2018 at 6:25 AM.
 */


/**
 * Created by Qandeel Abbassi on 5/15/2017 at 6:25 PM.
 */

public class MemphisTextView extends AppCompatTextView {

    private String mFontType = "MemphisLTStd-Light.otf";
    private String[] fonts = new String[]{"MemphisLTStd-Bold.otf", "MemphisLTStd-BoldItalic.otf", "MemphisLTStd-ExtraBold.otf",
            "MemphisLTStd-Light.otf", "MemphisLTStd-LightItalic.otf", "MemphisLTStd-Medium.otf", "MemphisLTStd-MediumItalic.otf"};

    public MemphisTextView(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public MemphisTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MemphisFont,
                0, 0);

        try {
            mFontType = fonts[a.getInteger(R.styleable.MemphisFont_fontType, 5)];
        } finally {
            a.recycle();
        }
        applyCustomFont(context);
    }

    public MemphisTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface("fonts/" + mFontType, context);
        setTypeface(customFont);
    }

    public String[] getFonts() {
        return fonts;
    }

    public void setFonts(String[] fonts) {
        this.fonts = fonts;
    }
}
