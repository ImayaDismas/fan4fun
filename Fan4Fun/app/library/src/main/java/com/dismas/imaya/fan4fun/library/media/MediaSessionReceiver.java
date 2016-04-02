package com.dismas.imaya.fan4fun.library.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

import com.dismas.imaya.fan4fun.library.player.PlaybackService;

/**
 * Receiver used to catch media buttons events from the lock screen.
 */
public class MediaSessionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            // intent wasn't a MEDIA BUTTON event.
            return;
        }

        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int keycode = event.getKeyCode();
        int action = event.getAction();

        // Switch on keycode and fire action only on KeyDown event.
        switch (keycode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (action == KeyEvent.ACTION_DOWN) {
                    sendAction(context, MediaSessionWrapper.ACTION_TOGGLE_PLAYBACK);
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (action == KeyEvent.ACTION_DOWN) {
                    sendAction(context, MediaSessionWrapper.ACTION_NEXT_TRACK);
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (action == KeyEvent.ACTION_DOWN) {
                    sendAction(context, MediaSessionWrapper.ACTION_PREVIOUS_TRACK);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Propagate lock screen event to the player.
     *
     * @param context context used to start service.
     * @param action  action to send.
     */
    private void sendAction(Context context, String action) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
