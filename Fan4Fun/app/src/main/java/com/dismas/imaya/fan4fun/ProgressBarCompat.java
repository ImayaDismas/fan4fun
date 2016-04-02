package com.dismas.imaya.fan4fun;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by imaya on 4/2/16.
 */
public class ProgressBarCompat extends ProgressBar {

    private int mIndeterminateColor = -1;

    /**
     * Constructor
     *
     * @param context holding context.
     */
    public ProgressBarCompat(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context holding context.
     * @param attrs   attrs from xml.
     */
    public ProgressBarCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Constructor.
     *
     * @param context      holding context.
     * @param attrs        attrs from xml.
     * @param defStyleAttr style from xml.
     */
    public ProgressBarCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIndeterminateColor = attrs.getAttributeIntValue(
                "http://schemas.android.com/apk/res/android",
                "indeterminateTint",
                context.getResources().getColor(R.color.progress_color)
        );

        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        if (mIndeterminateColor != -1) {
            getProgressDrawable().setColorFilter(mIndeterminateColor, PorterDuff.Mode.SRC_IN);
        }
    }
}
