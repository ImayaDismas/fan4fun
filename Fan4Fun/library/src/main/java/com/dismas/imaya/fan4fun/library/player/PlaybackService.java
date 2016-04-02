package com.dismas.imaya.fan4fun.library.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dismas.imaya.fan4fun.library.client.SoundCloudTrack;
import com.dismas.imaya.fan4fun.library.helpers.SoundCloudArtworkHelper;
import com.dismas.imaya.fan4fun.library.media.MediaSessionWrapper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

/**
 * Service used as SoundCloudPlayer.
 */
public class PlaybackService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener,
        AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnInfoListener {

    /**
     * Action used for toggle playback event
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_TOGGLE_PLAYBACK = "sound_cloud_toggle_playback";

    /**
     * Action used to skip to the next track of the sound cloud player.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_NEXT_TRACK = "sound_cloud_player_next";

    /**
     * Action used to skip to the previous track of the sound cloud player.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_PREVIOUS_TRACK = "sound_cloud_player_previous";

    /**
     * Action used to skip to clear the notification.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_CLEAR_NOTIFICATION = "sound_cloud_player_clear";

    /**
     * Action used to stop the player when audio signal has becoming noisy by the system.
     * http://developer.android.com/reference/android/media/AudioManager.html#ACTION_AUDIO_BECOMING_NOISY
     */
    static final String ACTION_AUDIO_BECOMING_NOISY = "sound_cloud_player_becoming_noisy";

    /**
     * Action used to play a track.
     */
    private static final String ACTION_PLAY = "sound_cloud_play";

    /**
     * Action used to resume the sound cloud player.
     */
    private static final String ACTION_PAUSE_PLAYER = "sound_cloud_player_resume";

    /**
     * Action used to pause the sound cloud player.
     */
    private static final String ACTION_RESUME_PLAYER = "sound_cloud_player_pause";

    /**
     * Action used to stop the sound cloud player.
     */
    private static final String ACTION_STOP_PLAYER = "sound_cloud_player_stop";

    /**
     * Action used to change the cursor of the current track.
     */
    private static final String ACTION_SEEK_TO = "sound_cloud_player_seek_to";

    /**
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Bundle key used to pass track url.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK = "sound_cloud_player_bundle_key_track_url";

    /**
     * Bundle key used to seek to a given position.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION = "sound_cloud_player_bundle_key_seek_to";

    /**
     * what id used to identify "play" message.
     */
    private static final int WHAT_PLAY = 0;

    /**
     * what id used to identify "pause player" message.
     */
    private static final int WHAT_PAUSE_PLAYER = 1;

    /**
     * what id used to identify "resume player" message.
     */
    private static final int WHAT_RESUME_PLAYER = 2;

    /**
     * what id used to identify "next track" message.
     */
    private static final int WHAT_NEXT_TRACK = 3;

    /**
     * what id used to identify "previous track" message.
     */
    private static final int WHAT_PREVIOUS_TRACK = 4;

    /**
     * what id used to identify "seek to" message.
     */
    private static final int WHAT_SEEK_TO = 5;

    /**
     * what id used to stop playback request
     */
    private static final int WHAT_STOP_PLAYER = 6;

    /**
     * what id used to stop the service.
     */
    private static final int WHAT_RELEASE_PLAYER = 7;

    /**
     * what id used to clear the player.
     */
    private static final int WHAT_CLEAR_PLAYER = 9;

    /**
     * Log cat and thread name prefix.
     */
    private static final String TAG = PlaybackService.class.getSimpleName();

    /**
     * Delay used to avoid useless action in case of spam action.
     */
    private static final int MESSAGE_DELAY_MILLI = 100;

    /**
     * Max idle period after which the service will be stopped.
     */
    private static final int IDLE_PERIOD_MILLI = 60000;

    /**
     * Path param used to access streaming url.
     */
    private static final String SOUND_CLOUD_CLIENT_ID_PARAM = "?client_id=";

    /**
     * Tag used in debugging message for wifi lock.
     */
    private static final String WIFI_LOCK_TAG = TAG + "wifi_lock";

    /**
     * Name for the internal handler thread.
     */
    private static final String THREAD_NAME = TAG + "player_thread";

    /**
     * Thread used to complete work off the main thread.
     */
    private HandlerThread mHandlerThread;

    /**
     * Handler used to execute works on an {@link android.os.HandlerThread}
     */
    private Handler mPlayerHandler;

    /**
     * Handler used to stop the service when idle period ends.
     */
    private Handler mStopServiceHandler;

    /**
     * MediaPlayer used to play music.
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Used to know if the player is paused.
     */
    private boolean mIsPaused;

    /**
     * Used to know if the player has leary played a track.
     */
    private boolean mHasAlreadyPlayed;

    /**
     * Lock used to keep wifi while playing.
     */
    private WifiManager.WifiLock mWifiLock;

    /**
     * SoundCloudClientId.
     */
    private String mSoundCloundClientId;

    /**
     * Used to broadcast events.
     */
    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Sound cloud notification manager.
     */
    private NotificationManager mNotificationManager;

    /**
     * Used to managed the internal playlist.
     */
    private PlayerPlaylist mPlayerPlaylist;

    /**
     * System service used to managed audio through user device.
     */
    private AudioManager mAudioManager;

    /**
     * Wrapper used to manage {@link android.support.v4.media.session.MediaSessionCompat}
     * as well as {@link com.dismas.imaya.fan4fun.library.remote.RemoteControlClientCompat}
     */
    private MediaSessionWrapper mMediaSession;

    /**
     * Picasso target used to retrieve the track artwork.
     */
    private Target mArtworkTarget;

    /**
     * Handler running on main thread to perform change on notification ui.
     */
    private Handler mMainThreadHandler;

    /**
     * Progress used to update current track progress.
     */
    private CountDownTimer mCountDown;


    /**
     * Start the playback.
     * <p/>
     * Play the track matching the given position in the given playlist.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     * @param track    the track which will be played.
     */
    public static void play(Context context, String clientId, SoundCloudTrack track) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK, track);
        context.startService(intent);
    }

    /**
     * Pause the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void pause(Context context, String clientId) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PAUSE_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Resume the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void resume(Context context, String clientId) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_RESUME_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Stop the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void stop(Context context, String clientId) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_STOP_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Seek to the precise track position.
     * <p/>
     * The current playing state of the SoundCloud player will be kept.
     * <p/>
     * If playing it remains playing, if paused it remains paused.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     * @param milli    time in milli of the position.
     */
    public static void seekTo(Context context, String clientId, int milli) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_SEEK_TO);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION, milli);
        context.startService(intent);
    }

    /**
     * Register a listener to catch player event.
     *
     * @param context  context used to register the listener.
     * @param listener listener to register.
     */
    public static void registerListener(Context context, PlaybackListener listener) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackListener.ACTION_ON_TRACK_PLAYED);
        filter.addAction(PlaybackListener.ACTION_ON_PLAYER_PAUSED);
        filter.addAction(PlaybackListener.ACTION_ON_SEEK_COMPLETE);
        filter.addAction(PlaybackListener.ACTION_ON_PLAYER_DESTROYED);
        filter.addAction(PlaybackListener.ACTION_ON_BUFFERING_STARTED);
        filter.addAction(PlaybackListener.ACTION_ON_BUFFERING_ENDED);
        filter.addAction(PlaybackListener.ACTION_ON_PROGRESS_CHANGED);

        LocalBroadcastManager.getInstance(context.getApplicationContext())
                .registerReceiver(listener, filter);
    }

    /**
     * Unregister a registered listener.
     *
     * @param context  context used to unregister the listener.
     * @param listener listener to unregister.
     */
    public static void unregisterListener(Context context, PlaybackListener listener) {
        LocalBroadcastManager.getInstance(context.getApplicationContext())
                .unregisterReceiver(listener);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_AUDIO);
        mHandlerThread.start();

        mPlayerHandler = new PlayerHandler(mHandlerThread.getLooper());
        mMediaPlayer = new MediaPlayer();

        initializeMediaPlayer();
        mStopServiceHandler = new StopHandler(mHandlerThread.getLooper());

        // instantiate target used to load track artwork.
        mArtworkTarget = new ArtworkTarget();

        // create handler on the main thread to avoid throwing error
        // with picasso when bitmap is retrieved and loaded in notification.
        mMainThreadHandler = new Handler(getApplicationContext().getMainLooper());

        mWifiLock = ((WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        mNotificationManager = NotificationManager.getInstance(this);

        mPlayerPlaylist = PlayerPlaylist.getInstance();

        mHasAlreadyPlayed = false;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mMediaSession = new MediaSessionWrapper(this, new MediaSessionCallback(), mAudioManager);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopTimer();
        mAudioManager.abandonAudioFocus(this);
        mMediaSession.onDestroy();

        mPlayerHandler.removeCallbacksAndMessages(null);
        stopForeground(true);

        Intent intent = new Intent(PlaybackListener.ACTION_ON_PLAYER_DESTROYED);
        mLocalBroadcastManager.sendBroadcast(intent);

        mMediaPlayer.release();
        mMediaPlayer = null;

        mHandlerThread.quit();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message message = mPlayerHandler.obtainMessage();
            Bundle extra = intent.getExtras();

            if (extra != null) {
                // client id should be passed for each command as service could have been restarted
                // by the system.
                mSoundCloundClientId = extra.getString(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID);

                // transfer args to the handler.
                message.setData(extra);
            }

            switch (intent.getAction()) {
                case ACTION_PLAY:
                    message.what = WHAT_PLAY;
                    break;
                case ACTION_PAUSE_PLAYER:
                    message.what = WHAT_PAUSE_PLAYER;
                    break;
                case ACTION_RESUME_PLAYER:
                    message.what = WHAT_RESUME_PLAYER;
                    break;
                case ACTION_STOP_PLAYER:
                    message.what = WHAT_STOP_PLAYER;
                    break;
                case ACTION_NEXT_TRACK:
                    message.what = WHAT_NEXT_TRACK;
                    break;
                case ACTION_PREVIOUS_TRACK:
                    message.what = WHAT_PREVIOUS_TRACK;
                    break;
                case ACTION_SEEK_TO:
                    message.what = WHAT_SEEK_TO;
                    break;
                case ACTION_TOGGLE_PLAYBACK:
                    if (mIsPaused) {
                        message.what = WHAT_RESUME_PLAYER;
                    } else {
                        message.what = WHAT_PAUSE_PLAYER;
                    }
                    break;
                case ACTION_AUDIO_BECOMING_NOISY:
                    if (!mIsPaused) {
                        message.what = WHAT_PAUSE_PLAYER;
                    }
                    break;
                case ACTION_CLEAR_NOTIFICATION:
                    message.what = WHAT_CLEAR_PLAYER;
                    break;
                default:
                    break;
            }
            gotoIdleState();
            mPlayerHandler.removeCallbacksAndMessages(null);
            mPlayerHandler.sendMessageDelayed(message, MESSAGE_DELAY_MILLI);
        }
        return START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer error occurred : " + what + " => reset mediaPlayer");
        initializeMediaPlayer();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // release lock on wifi.
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        gotoIdleState();
        mPlayerHandler.sendEmptyMessage(WHAT_NEXT_TRACK);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // broadcast event
        Intent intent = new Intent(PlaybackListener.ACTION_ON_SEEK_COMPLETE);
        intent.putExtra(PlaybackListener.EXTRA_KEY_CURRENT_TIME, mp.getCurrentPosition());
        mLocalBroadcastManager.sendBroadcast(intent);
        if (!mIsPaused) {
            resumeTimer();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mIsPaused) {
                    resume();
                }
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (!mIsPaused) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (!mIsPaused) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (!mIsPaused) {
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Intent i;
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                // broadcast event
                i = new Intent(PlaybackListener.ACTION_ON_BUFFERING_STARTED);
                mLocalBroadcastManager.sendBroadcast(i);
                return true;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                // broadcast event
                i = new Intent(PlaybackListener.ACTION_ON_BUFFERING_ENDED);
                mLocalBroadcastManager.sendBroadcast(i);
                return true;
            default:
                return false;
        }
    }


    /**
     * Pause the playback.
     */
    private void pause() {
        if (mHasAlreadyPlayed && !mIsPaused) {
            mIsPaused = true;
            mMediaPlayer.pause();

            // broadcast event
            Intent intent = new Intent(PlaybackListener.ACTION_ON_PLAYER_PAUSED);
            mLocalBroadcastManager.sendBroadcast(intent);

            updateNotification();

            mMediaSession.setPlaybackState(MediaSessionWrapper.PLAYBACK_STATE_PAUSED);
            pauseTimer();
        }
    }

    /**
     * Resume the playback.
     */
    private void resume() {
        if (mIsPaused) {
            mIsPaused = false;
            // Try to gain the audio focus before preparing and starting the media player.
            if (mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mMediaPlayer.start();

                Intent intent = new Intent(PlaybackListener.ACTION_ON_TRACK_PLAYED);
                intent.putExtra(PlaybackListener.EXTRA_KEY_TRACK,
                        mPlayerPlaylist.getCurrentTrack());
                mLocalBroadcastManager.sendBroadcast(intent);

                updateNotification();
                mMediaSession.setPlaybackState(MediaSessionWrapper.PLAYBACK_STATE_PLAYING);
                resumeTimer();
            }
        }
    }

    private void stopPlayer() {
        mMediaSession.setPlaybackState(MediaSessionWrapper.PLAYBACK_STATE_STOPPED);
        mMediaPlayer.stop();
        mIsPaused = true;
        stopSelf();
    }

    private void nextTrack() {
        playTrack(mPlayerPlaylist.next());
    }

    private void previousTrack() {
        playTrack(mPlayerPlaylist.previous());
    }

    private void seekToPosition(int milli) {
        mMediaPlayer.seekTo(milli);
    }

    private void initializeMediaPlayer() {
        mMediaPlayer.reset();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
    }

    /**
     * Play a track.
     * <p/>
     * This method ensures that the media player will be in the right state to be able to play a new
     * datasource.
     *
     * @param track track url.
     */
    private void playTrack(SoundCloudTrack track) {
        pauseTimer();
        try {
            // acquire lock on wifi.
            mWifiLock.acquire();

            // set media player to stop state in order to be able to call prepare.
            mMediaPlayer.reset();

            // set new data source
            mMediaPlayer.setDataSource(track.getStreamUrl() + SOUND_CLOUD_CLIENT_ID_PARAM
                    + mSoundCloundClientId);

            mIsPaused = false;
            mHasAlreadyPlayed = true;

            // 1 - UPDATE ALL VISUAL CALLBACK FIRST TO IMPROVE USER EXPERIENCE

            updateNotification();
            // update playback state as well as meta data.
            mMediaSession.setPlaybackState(MediaSessionWrapper.PLAYBACK_STATE_PLAYING);
            // start loading of the artwork.
            loadArtwork(this,
                    SoundCloudArtworkHelper.getArtworkUrl(track, SoundCloudArtworkHelper.XXXLARGE));
            // broadcast events
            Intent intent = new Intent(PlaybackListener.ACTION_ON_TRACK_PLAYED);
            intent.putExtra(PlaybackListener.EXTRA_KEY_TRACK, track);
            mLocalBroadcastManager.sendBroadcast(intent);
            Intent bufferingStart = new Intent(PlaybackListener.ACTION_ON_BUFFERING_STARTED);
            mLocalBroadcastManager.sendBroadcast(bufferingStart);

            // 2 - THEN PREPARE THE TRACK STREAMING

            // Try to gain the audio focus before preparing and starting the media player.
            if (mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // prepare synchronously as the service run on it's own handler thread.
                mMediaPlayer.prepare();
                // start the playback.
                mMediaPlayer.start();
                SoundCloudTrack currentTrack = mPlayerPlaylist.getCurrentTrack();
                if (currentTrack == null) {
                    mMediaPlayer.stop();
                } else {
                    startTimer(currentTrack.getDurationInMilli());
                }

                Intent bufferingEnds = new Intent(PlaybackListener.ACTION_ON_BUFFERING_ENDED);
                mLocalBroadcastManager.sendBroadcast(bufferingEnds);
            }

        } catch (IOException e) {
            Log.e(TAG, "File referencing not exist : " + track);
        }
    }

    /**
     * Update the notification with the current track information.
     */
    private void updateNotification() {
        mNotificationManager.notify(
                this, mPlayerPlaylist.getCurrentTrack(), mIsPaused);
    }

    /**
     * Start idle state.
     * <p/>
     * After {@link PlaybackService#IDLE_PERIOD_MILLI}
     * the service will ne stopped.
     */
    private void gotoIdleState() {
        mStopServiceHandler.removeCallbacksAndMessages(null);
        mStopServiceHandler.sendEmptyMessageDelayed(WHAT_RELEASE_PLAYER, IDLE_PERIOD_MILLI);
    }

    /**
     * Load the track artwork.
     *
     * @param context    context used by {@link com.squareup.picasso.Picasso} to load the artwork asynchronously.
     * @param artworkUrl artwork url of the track.
     */
    private void loadArtwork(final Context context, final String artworkUrl) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Picasso
                        .with(context)
                        .load(artworkUrl)
                        .into(mArtworkTarget);
            }
        });
    }

    /**
     * Start internal timer used to propagate the playback position.
     *
     * @param duration duration for which the timer should be started.
     */
    private void startTimer(long duration) {
        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }

        // refresh progress every seconds.
        mCountDown = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent i = new Intent(PlaybackListener.ACTION_ON_PROGRESS_CHANGED);
                i.putExtra(PlaybackListener.EXTRA_KEY_CURRENT_TIME, mMediaPlayer.getCurrentPosition());
                mLocalBroadcastManager.sendBroadcast(i);
            }

            @Override
            public void onFinish() {
                Intent i = new Intent(PlaybackListener.ACTION_ON_PROGRESS_CHANGED);
                i.putExtra(PlaybackListener.EXTRA_KEY_CURRENT_TIME
                        , (int) mPlayerPlaylist.getCurrentTrack().getDurationInMilli());
                mLocalBroadcastManager.sendBroadcast(i);
            }
        };
        mCountDown.start();
    }

    /**
     * Pause the internal timer used to propagate the playback position.
     */
    private void pauseTimer() {
        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }
    }

    /**
     * Resume the internal timer used to propagate the playback position.
     */
    private void resumeTimer() {
        // restart timer for the remaining time amount.
        startTimer(mPlayerPlaylist.getCurrentTrack().getDurationInMilli()
                - mMediaPlayer.getCurrentPosition());
    }

    /**
     * Pause the internal timer used to propagate the playback position.
     */
    private void stopTimer() {
        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }
    }

    /**
     * Looper used process player request.
     */
    private final class PlayerHandler extends Handler {

        /**
         * Handler used to process player request.
         *
         * @param looper must not be null.
         */
        public PlayerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case WHAT_PLAY:
                    playTrack(((SoundCloudTrack) data.getParcelable(BUNDLE_KEY_SOUND_CLOUD_TRACK)));
                    break;
                case WHAT_PAUSE_PLAYER:
                    pause();
                    break;
                case WHAT_RESUME_PLAYER:
                    resume();
                    break;
                case WHAT_STOP_PLAYER:
                    stopPlayer();
                    break;
                case WHAT_NEXT_TRACK:
                    nextTrack();
                    break;
                case WHAT_PREVIOUS_TRACK:
                    previousTrack();
                    break;
                case WHAT_SEEK_TO:
                    seekToPosition(data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION));
                    break;
                case WHAT_CLEAR_PLAYER:
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Callback implementation to catch media session events.
     */
    private final class MediaSessionCallback implements MediaSessionWrapper.MediaSessionWrapperCallback {

        @Override
        public void onPlay() {
            resume();
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onSkipToNext() {
            nextTrack();
        }

        @Override
        public void onSkipToPrevious() {
            previousTrack();
        }

        @Override
        public void onPlayPauseToggle() {
            if (mIsPaused) {
                resume();
            } else {
                pause();
            }
        }
    }

    /**
     * Custom target used to load track artwork asynchronously.
     */
    private class ArtworkTarget implements Target {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            // update meta data with artwork.
            // copy bitmap to avoid IllegalStateException "Can't parcel a recycled bitmap"
            // from the remove control client on KitKat (IRemoteControlDisplay.java:340)
            SoundCloudTrack track = mPlayerPlaylist.getCurrentTrack();
            if (track != null) {
                mMediaSession.setMetaData(mPlayerPlaylist.getCurrentTrack(),
                        bitmap.copy(bitmap.getConfig(), false));
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    /**
     * Handler used to stop the service when idle period ends.
     */
    private class StopHandler extends Handler {

        /**
         * Handler used to stop the service when idle period ends.
         *
         * @param looper lopper from handler thread to avoid running on main thread.
         */
        public StopHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what != WHAT_RELEASE_PLAYER || !mIsPaused) {
                return;
            }

            stopSelf();
        }
    }
}
