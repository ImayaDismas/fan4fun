package com.dismas.imaya.fan4fun;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by imaya on 4/2/16.
 */
public class PickActivity extends ActionBarActivity implements
        TextView.OnEditorActionListener, View.OnClickListener, ViewTreeObserver.OnScrollChangedListener {

    private ExtraHintEditText mArtistName;
    private Animation mWiggle;
    private View mBanner;
    private ScrollView mScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);

        mArtistName = ((ExtraHintEditText) findViewById(R.id.activity_pick_artist_edit));
        View extraHintView = findViewById(R.id.activity_pick_extra_hint);
        mArtistName.setExtraHintView(extraHintView);
        mArtistName.setOnEditorActionListener(this);

        mScrollView = ((ScrollView) findViewById(R.id.activity_pick_scrollview));
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        mBanner = findViewById(R.id.activity_pick_banner);

        mWiggle = AnimationUtils.loadAnimation(this, R.anim.wiggle);

        findViewById(R.id.activity_pick_search_btn).setOnClickListener(this);
        findViewById(R.id.activity_pick_section_links_git).setOnClickListener(this);
        findViewById(R.id.activity_pick_section_links_web).setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handle = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (checkField(mArtistName)) {
                handle = true;
                ArtistActivity.startActivity(this, mArtistName.getText().toString());
            }
        }
        return handle;
    }

    /**
     * Check if the edit text is valid or not.
     *
     * @param editText field to check.
     * @return true if the edit text isn't empty
     */
    private boolean checkField(EditText editText) {
        boolean valid = true;
        if (TextUtils.isEmpty(editText.getText())) {
            editText.startAnimation(mWiggle);
            editText.requestFocus();
            valid = false;
        }
        return valid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_pick_search_btn:
                if (checkField(mArtistName)) {
                    ArtistActivity.startActivity(this, mArtistName.getText().toString());
                }
                break;

            case R.id.activity_pick_section_links_git:
                final Intent gitLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.link_git)));
                startActivity(gitLinkIntent);
                break;

            case R.id.activity_pick_section_links_web:
                final Intent websiteLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.link_website)));
                startActivity(websiteLinkIntent);
                break;

            default:
                throw new IllegalStateException("Click not handled on " + v);
        }
    }

    @Override
    public void onScrollChanged() {
        mBanner.setTranslationY(-mScrollView.getScrollY() / 2f);
    }
}