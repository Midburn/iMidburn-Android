package com.elders.imidburn.api;

import com.elders.imidburn.api.response.Art;
import com.elders.imidburn.api.response.Camp;
import com.elders.imidburn.api.response.DataManifest;
import com.elders.imidburn.api.response.Event;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Streaming;
import rx.Observable;

/**
 * IBurn API Definition
 * Created by dbro on 8/1/15.
 */
public interface IBurnApi {

    @GET("/update.json.js")
    Observable<DataManifest> getDataManifest();

    @GET("/camps.json.js")
    Observable<List<Camp>> getCamps();

    @GET("/art.json.js")
    Observable<List<Art>> getArt();

    @GET("/events.json.js")
    Observable<List<Event>> getEvents();

    @GET("/iburn.mbtiles.jar")
    @Streaming
    Observable<Response> getTiles();
}
