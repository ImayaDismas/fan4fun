package com.dismas.imaya.fan4fun.library.player;

import com.dismas.imaya.fan4fun.library.client.SoundCloudPlaylist;
import com.dismas.imaya.fan4fun.library.client.SoundCloudTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to manage the player playlist.
 */
final class PlayerPlaylist {

    /**
     * Singleton pattern.
     */
    private static PlayerPlaylist sInstance;

    /**
     * Current playlist.
     */
    private SoundCloudPlaylist mSoundCloudPlaylist;

    /**
     * Index of the current track.
     */
    private int mCurrentTrackIndex;

    /**
     * Singleton.
     */
    private PlayerPlaylist() {
        mSoundCloudPlaylist = new SoundCloudPlaylist();
        mCurrentTrackIndex = -1;
    }

    /**
     * Retrieve the instance of the playlist manager.
     *
     * @return instance.
     */
    public static PlayerPlaylist getInstance() {
        if (sInstance == null) {
            sInstance = new PlayerPlaylist();
        }
        return sInstance;
    }

    /**
     * Retrieve the current playlist.
     *
     * @return current playlist.
     */
    public SoundCloudPlaylist getPlaylist() {
        return mSoundCloudPlaylist;
    }

    /**
     * Return the current track.
     *
     * @return current track or null if none has been added to the player playlist.
     */
    public SoundCloudTrack getCurrentTrack() {
        if (mCurrentTrackIndex > -1 && mCurrentTrackIndex < mSoundCloudPlaylist.getTracks().size()) {
            return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
        }
        return null;
    }

    /**
     * Return the current track index.
     *
     * @return current track index.
     */
    public int getCurrentTrackIndex() {
        return mCurrentTrackIndex;
    }

    /**
     * Add a track at the end of the playlist.
     *
     * @param track track to be added.
     */
    public void add(SoundCloudTrack track) {
        add(mSoundCloudPlaylist.getTracks().size(), track);
    }

    /**
     * Add all tracks at the end of the playlist.
     *
     * @param tracks tracks to add.
     */
    public void addAll(List<SoundCloudTrack> tracks) {
        for (SoundCloudTrack track : tracks) {
            add(mSoundCloudPlaylist.getTracks().size(), track);
        }
    }

    /**
     * Add a track to the given position.
     *
     * @param position position of the track to insert
     * @param track    track to insert.
     */
    public void add(int position, SoundCloudTrack track) {
        if (mCurrentTrackIndex == -1) {
            mCurrentTrackIndex = 0;
        }
        mSoundCloudPlaylist.addTrack(position, track);
    }

    /**
     * Remove a track from the SoundCloud player playlist.
     * <p/>
     * If the track is currently played, it will be stopped before being removed.
     *
     * @param trackIndex index of the track to be removed.
     * @return track removed or null if given index can't be found.
     */
    public SoundCloudTrack remove(int trackIndex) {

        SoundCloudTrack removedTrack = null;
        ArrayList<SoundCloudTrack> tracks = mSoundCloudPlaylist.getTracks();

        // check if track is in the playlist
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            removedTrack = tracks.remove(trackIndex);

            if (tracks.size() == 0) {
                // track list empty after removal
                mCurrentTrackIndex = 0;
            } else if (trackIndex == tracks.size()) {
                // last song removed
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % tracks.size();
            } else if (trackIndex >= 0 && trackIndex < mCurrentTrackIndex) {
                // tracks translated on the right after a deletion before current played one.
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % tracks.size();
            }
        }
        return removedTrack;
    }

    /**
     * Retrieve the next track.
     *
     * @return next track to be played.
     */

    public SoundCloudTrack next() {
        mCurrentTrackIndex = (mCurrentTrackIndex + 1) % mSoundCloudPlaylist.getTracks().size();
        return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
    }

    /**
     * Retrieve the previous track.
     *
     * @return previous track to be played.
     */
    public SoundCloudTrack previous() {
        int tracks = mSoundCloudPlaylist.getTracks().size();
        mCurrentTrackIndex = (tracks + mCurrentTrackIndex - 1) % tracks;
        return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
    }

    /**
     * Retrieve the size of the playlist.
     *
     * @return Number of tracks in the playlist.
     */
    public int size() {
        return mSoundCloudPlaylist.getTracks().size();
    }

    /**
     * Used to know if the playlist is empty.
     *
     * @return true if the current playlist is empty.
     */
    public boolean isEmpty() {
        return mSoundCloudPlaylist.getTracks().size() == 0;
    }


    /**
     * Allow to set the current playing song index.
     * private package.
     *
     * @param playingTrackPosition current playing song index.
     */
    void setPlayingTrack(int playingTrackPosition) {
        if (playingTrackPosition < 0 || playingTrackPosition >= mSoundCloudPlaylist.getTracks().size()) {
            throw new IllegalArgumentException("No tracks a the position " + playingTrackPosition);
        }
        mCurrentTrackIndex = playingTrackPosition;
    }
}
