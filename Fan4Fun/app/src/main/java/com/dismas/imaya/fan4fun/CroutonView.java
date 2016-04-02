package com.dismas.imaya.fan4fun;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by imaya on 4/2/16.
 */
public class CroutonView extends LinearLayout {

    /**
     * CompoundView to display a person.
     *
     * @param context calling context.
     * @param text    displayed.
     */
    public CroutonView(Context context, String text) {
        super(context);
        if (!isInEditMode()) {
            init(context, text);
        }
    }

    /**
     * Initialize internal component.
     *
     * @param context calling context.
     * @param text    text displayed.
     */
    private void init(Context context, String text) {
        this.setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.crouton_view, this);
        ((TextView) this.findViewById(R.id.crouton_view_message)).setText(text);

        setOrientation(VERTICAL);
    }
}
