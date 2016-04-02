package com.dismas.imaya.fan4fun.library.client;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Interface used to access the SoundCloud API.
 */
interface RetrofitService {

    /**
     * Retrieve a SoundCloud user profile.
     *
     * @param user SoundCloud user id as string or user name.
     * @return {@link rx.Observable} on {@link retrofit.client.Response}
     */
    @GET("/users/{user}.json")
    public Observable<Response> getUser(@Path("user") String user);

    /**
     * Retrieve all public tracks of a user.
     *
     * @param user SoundCloud user id as string or user name.
     * @return {@link rx.Observable} on {@link retrofit.client.Response}
     */
    @GET("/users/{user}/tracks.json")
    public Observable<Response> getUserTracks(@Path("user") String user);

    /**
     * Retrieve a SoundCloud track.
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable} on {@link retrofit.client.Response}
     */
    @GET("/tracks/{trackId}.json")
    public Observable<Response> getTrack(@Path("trackId") int trackId);

    /**
     * Retrieve the list of comments related to the
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable} on {@link retrofit.client.Response}
     */
    @GET("/tracks/{trackId}/comments.json")
    public Observable<Response> getTrackComments(@Path("trackId") int trackId);
}
