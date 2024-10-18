package ict.ihu.gr.loopify;

import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


//Diagram of the calls that need to be made in chronological order for the correct load of a track that the user searched:
//1. the user types the song of his choice
//2. the song then needs to be searched with this function "fetchArtistFromTrack" so that the returned JSON embeds the actual artist name of the searched song
//3. then since we have the artist name and also the track name, we can start calling the functions that ultimately give us the youtube URL
//4. we use the fetchTADB_Artist_ID function to get the artist TADB_Artist_ID so we can search with the last API for the actual Youtube URL
//5. last but not least, we use the fetchYtURL function to get the Youtube URL
//6. then we brake down the JSON string to only get the Youtube URL

//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=" + lastFMapiKey + "&artist=" + artist + // this is the LastFM api call
//                "&track=" + track + "&format=json";
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getcorrection&artist=micahel jackson&api_key=a38306489162f067667f1b911c8345c5&format=json";
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=a38306489162f067667f1b911c8345c5&artist=cher&track=believe&format=json";


public class ApiManager {
    private static final String TAG = "ApiManager";
    private final String lastFMapiKey = "a38306489162f067667f1b911c8345c5"; // Na ta kanoume store ektos public repo!
    private final String theaudioDBapiKey = "523532";

    /**
     * This function fetches the Artist ID from TheAudioDB API using the provided track and artist name,
     * then retrieves the JSON response in which the YouTube URL for that artist's music videos is embedded.
     *
     * @param track The name of the track to search for.
     * @param artist The name of the artist associated with the track.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchTADB_Artist_ID(String track, String artist, ApiResponseListener listener) { // number 2 in execute order
        String jsonUrl = "https://www.theaudiodb.com/api/v1/json/" + theaudioDBapiKey + "/searchtrack.php?s="+artist+"&t="+track; // this is the audioDB api call

        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                String TADB_Artist_ID = getIdArtistFromJson(jsonResponse);
                if (TADB_Artist_ID != null){
                    Log.d(TAG, "Artist ID: " + TADB_Artist_ID);
                    fetchYtURL(TADB_Artist_ID, listener, track);
                }else{
                    Log.d(TAG, "Artist ID not found!");
                }
            }
        }).execute(jsonUrl); // if the user has searched for an artist name, then this returns a null in which
                                                    // we can use to also do a search for an artist instead of a track
    }

    /**
     * This function fetches the YouTube URL of the specified track from the artist's music videos
     * using the Artist ID obtained from TheAudioDB API.
     *
     * @param TADB_Artist_ID The ID of the artist to search for in the music videos.
     * @param listener The callback listener for handling the API response.
     * @param track The name of the track to find the YouTube URL for.
     */
    public void fetchYtURL(String TADB_Artist_ID, ApiResponseListener listener, String track) { //number 3 in execute order
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=" + lastFMapiKey +
//                "&artist=" + artist + "&format=json";
        String jsonUrl = "https://www.theaudiodb.com/api/v1/json/" + theaudioDBapiKey + "/mvid.php?i=" + TADB_Artist_ID;
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                String youtubeURL = getYouTubeUrlFromJson(jsonResponse, track);
                if (youtubeURL != null){
                    Log.d(TAG, "Url from Youtube: " + youtubeURL);
                    fetchMP3file(youtubeURL, listener);
                }else{
                    Log.d(TAG, "Url Not found");
                }
            }
        }).execute(jsonUrl);
    }

    /**
     * This function searches for an artist associated with a specific track using LastFM's API,
     * then calls fetchTADB_Artist_ID to get the Artist ID and further information.
     *
     * @param track The name of the track to search for.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchArtistFromTrack(String track, ApiResponseListener listener) { //number 1 in execute order
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.search&api_key=" + lastFMapiKey +
                "&track=" + track + "&format=json";

        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                // Find the first matching artist from LastFM's response
                String matchingArtist = findFirstMatchingArtistFromLastFM(jsonResponse);

                if (matchingArtist != null) {
                    Log.d(TAG, "Matching artist from LastFM found: " + matchingArtist);

                    // once artist is found, call fetchTADB_Artist_ID which is second in the execute order
                    fetchTADB_Artist_ID(track, matchingArtist, listener);
                } else {
                    Log.d(TAG, "No matching artist found on LastFM for track: " + track);

                    // notify the listener that no matching artist was found
                    listener.onResponseReceived(null);
                }
            }
        }).execute(jsonUrl);
    }

    /**
     * This function fetches information about the specified artist from TheAudioDB API.
     *
     * @param artist The name of the artist to retrieve information for.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchArtistInfo(String artist, ApiResponseListener listener) {
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=" + lastFMapiKey +
//                "&artist=" + artist + "&format=json";
        String jsonUrl = "https://www.theaudiodb.com/api/v1/json/" + theaudioDBapiKey + "/search.php?s=" + artist;
        new GetJsonTask(listener).execute(jsonUrl);
    }
    public void fetchCorrectedArtistInfo(String artist, ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getCorrection&api_key=" + lastFMapiKey +
                "&artist=" + artist + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }

    public void fetchCorrectedTrackInfo(String track, String artist, ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getcorrection&api_key=" + lastFMapiKey + "&artist=" + artist +
                "&track=" + track + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }

    //top chart functions
    public void fetchTopArtistCharts(ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=chart.gettopartists&api_key=" + lastFMapiKey + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }

    public void fetchTopTrackCharts(ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&api_key=" + lastFMapiKey + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }


    // function to change when we migrate to Cloudfare
    public void fetchMP3file(String youtubeURL, ApiResponseListener listener){
        OkHttpClient client = new OkHttpClient();

        // Create JSON payload
        String json = "{ \"url\": \"" + youtubeURL + "\" }"; // the served info format our api handles
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request.Builder()
                .url("http://192.168.1.3:3000/download") // we will change this to Petros's server ip/Cloudfare directory when we migrate
                .post(body)
                .build();

        new Thread(() -> { // occupy a thread to handle the POST of the formated data
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    listener.onResponseReceived(responseData);
                } else {
                    listener.onResponseReceived(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.onResponseReceived(null);
            }
        }).start();
    }



    private static class GetJsonTask extends AsyncTask<String, Void, String> { // async task for the retrieval of the json info about the artist and the song
        private final ApiResponseListener listener;

        public GetJsonTask(ApiResponseListener listener) { // class constructor, declaring up the listener that is MainActivity in this case, because the call was from there.
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... urls) { // breakdown the json so that the data is separated
            String jsonResponse = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    jsonResponse = stringBuilder.toString();
                    reader.close();
                } else {
                    Log.e(TAG, "HTTP error code: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
            Log.d(TAG,jsonResponse);
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            listener.onResponseReceived(result); // inform MainActivity that the data is ready to be parsed
        }
    }

    public interface ApiResponseListener { // call back mechanism to inform the MainActivity that the data is ready
        void onResponseReceived(String jsonResponse);
    }


    @Nullable
    private String findFirstMatchingArtistFromLastFM(String jsonResponse) {
        // parse the JSON response and extract the artist's name from the trackmatches object
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray trackArray = jsonObject.getJSONObject("results").getJSONObject("trackmatches").getJSONArray("track");

            if (trackArray.length() > 0) {
                // extract the artist's name from the first track
                return trackArray.getJSONObject(0).getString("artist");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null; // return null if no artist was found
    }

    @Nullable
    private static String getIdArtistFromJson(String jsonString) {
        try {
            // Create a JSONObject from the input JSON string
            JSONObject jsonObject = new JSONObject(jsonString);
            // Get the array of tracks
            JSONArray trackArray = jsonObject.getJSONArray("track");

            // Check if the track array is not empty
            if (trackArray.length() > 0) {
                // Get the first track object
                JSONObject firstTrack = trackArray.getJSONObject(0);
                // Extract the idArtist from the first track
                return firstTrack.getString("idArtist");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;  // Return null if the idArtist is not found or if there's an error
    }

    @Nullable
    public static String getYouTubeUrlFromJson(String jsonString, String trackName) {
        try {
            // Create a JSONObject from the input JSON string
            JSONObject jsonObject = new JSONObject(jsonString);
            // Get the array of music videos
            JSONArray mvidsArray = jsonObject.getJSONArray("mvids");

            // Iterate through the array to find the matching track
            for (int i = 0; i < mvidsArray.length(); i++) {
                JSONObject musicVideo = mvidsArray.getJSONObject(i);
                // Check if the strTrack matches the given trackName
                if (musicVideo.getString("strTrack").equalsIgnoreCase(trackName)) {
                    // Return the strMusicVid URL if a match is found
                    return musicVideo.getString("strMusicVid");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();  // Print the stack trace for debugging
        }
        return null;  // Return null if the track is not found or if there's an error
    }
}
