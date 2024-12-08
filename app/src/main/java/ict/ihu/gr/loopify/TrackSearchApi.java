package ict.ihu.gr.loopify;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TrackSearchApi {
    @GET("?method=track.search&format=json")
    Call<TrackSearchResponse> searchTracks(
            @Query("track") String trackName,
            @Query("api_key") String apiKey
    );
}
