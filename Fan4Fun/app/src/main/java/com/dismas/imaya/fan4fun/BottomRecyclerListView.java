package com.dismas.imaya.fan4fun;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by imaya on 4/2/16.
 */
public class BottomRecyclerListView extends RecyclerView {

    public BottomRecyclerListView(Context context) {
        super(context);
    }

    public BottomRecyclerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomRecyclerListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        View child = getChildAt(0);
        if (child == null || ev.getY() < child.getY()) {
            handled = false;
        }
        return handled;
    }
}
