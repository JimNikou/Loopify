package ict.ihu.gr.loopify;

import ict.ihu.gr.loopify.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


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
    private final String lastFMapiKey = BuildConfig.LAST_FM_API_KEY;
    private final String theaudioDBapiKey = BuildConfig.THE_AUDIO_DB_KEY;

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
        String jsonUrl = "https://www.theaudiodb.com/api/v1/json/" + theaudioDBapiKey + "/mvid.php?i=" + TADB_Artist_ID;
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                String youtubeURL = getYouTubeUrlFromJson(jsonResponse, track);
                if (youtubeURL != null){
                    Log.d(TAG, "Url from Youtube: " + youtubeURL);
                    listener.onResponseReceived(youtubeURL);
                    fetchMP3file(youtubeURL, listener);
                }else{
                    Log.d(TAG, "Url Not found");
                    listener.onResponseReceived(null);
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
                    listener.onResponseReceived(matchingArtist);
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
     * This function fetches the album information from LastFM API based on artist and album names.
     *
     * @param artist The name of the artist.
     * @param album The name of the album.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchAlbumInfo(String artist, String album, ApiResponseListener listener) {
        // Construct the URL with the provided artist and album name
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" + lastFMapiKey +
                "&artist=" + artist + "&album=" + album + "&format=json";

        // Fetch the album information using GetJsonTask
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                // Parse the response to get the album information
                String albumInfo = getAlbumTracksFromJson(jsonResponse);

                if (albumInfo != null) {
                    Log.d(TAG, "Album Info: " + albumInfo);
                    listener.onResponseReceived(albumInfo);
                } else {
                    Log.d(TAG, "Album not found!");
                    listener.onResponseReceived(null);
                }
            }
        }).execute(jsonUrl);
    }

    /**
     * This function parses the JSON response to extract album information.
     *
     * @param jsonResponse The JSON response returned by the API.
     * @return The album information as a formatted string.
     */
    @Nullable
    private String getAlbumTracksFromJson(String jsonResponse) {
        try {
            // Parse the JSON object
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject albumObject = jsonObject.getJSONObject("album");

            // Extract the album name
            String albumName = albumObject.getString("name");

            // Extract the artist name
            String artistName = albumObject.getString("artist");

            // Extract the listeners count
            String listenersCount = albumObject.getString("listeners");

            // Extract the playcount
            String playCount = albumObject.getString("playcount");

            // Extract album images (let's use the "extralarge" image)
            JSONArray imagesArray = albumObject.getJSONArray("image");
            String imageUrl = "";
            for (int i = 0; i < imagesArray.length(); i++) {
                JSONObject imageObject = imagesArray.getJSONObject(i);
                String size = imageObject.getString("size");
                if (size.equals("extralarge")) {
                    imageUrl = imageObject.getString("#text");
                    break;
                }
            }

            // Extract the tracks from the album
            JSONArray tracksArray = albumObject.getJSONObject("tracks").getJSONArray("track");

            // Build the result string to return
            StringBuilder result = new StringBuilder();
            result.append("Album: ").append(albumName).append("\n");
            result.append("Artist: ").append(artistName).append("\n");
            result.append("Listeners: ").append(listenersCount).append("\n");
            result.append("Playcount: ").append(playCount).append("\n");
            result.append("Album Image: ").append(imageUrl).append("\n\n");
            result.append("Tracks:\n");

            // Loop through the tracks and append track names to the result
            for (int i = 0; i < tracksArray.length(); i++) {
                JSONObject trackObject = tracksArray.getJSONObject(i);
                String trackName = trackObject.getString("name");

                result.append(i + 1).append(". ").append(trackName).append("\n");
            }

            return result.toString(); // Return formatted album info with tracks and album image

        } catch (JSONException e) {
            e.printStackTrace();  // Print error for debugging
        }

        return null;  // Return null if an error occurred
    }

    /**
     * This function fetches information about a specific genre (tag) from LastFM API.
     *
     * @param genre The name of the genre (tag) to retrieve information for.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchGenreInfo(String genre, ApiResponseListener listener) {
        // Construct the URL for the tag.getinfo API call
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=tag.getinfo&tag=" + genre +
                "&api_key=" + lastFMapiKey + "&format=json";

        // Use GetJsonTask to fetch the genre info
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                // Parse the response to get the genre information
                String genreInfo = getGenreInfoFromJson(jsonResponse);

                if (genreInfo != null) {
                    Log.d(TAG, "Genre Info: " + genreInfo);
                    listener.onResponseReceived(genreInfo);
                } else {
                    Log.d(TAG, "Genre not found!");
                    listener.onResponseReceived(null);
                }
            }
        }).execute(jsonUrl);
    }

    /**
     * This function parses the JSON response to extract genre information.
     *
     * @param jsonResponse The JSON response returned by the API.
     * @return The genre information as a formatted string.
     */
    @Nullable
    private String getGenreInfoFromJson(String jsonResponse) {
        try {
            // Parse the JSON object
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Get the "tag" object from the JSON response
            JSONObject tagObject = jsonObject.getJSONObject("tag");

            // Extract relevant genre details (name, total usage, reach, description, etc.)
            String genreName = tagObject.getString("name");
            int totalUsage = tagObject.getInt("total");
            int reach = tagObject.getInt("reach");
            String wikiSummary = tagObject.getJSONObject("wiki").getString("summary");

            // Build a result string to return
            StringBuilder result = new StringBuilder();
            result.append("Genre: ").append(genreName).append("\n");
            result.append("Total Usage: ").append(totalUsage).append("\n");
            result.append("Reach: ").append(reach).append("\n");
            result.append("Description: ").append(wikiSummary).append("\n");

            return result.toString(); // Return formatted genre info

        } catch (JSONException e) {
            e.printStackTrace();  // Print error for debugging
        }

        return null;  // Return null if an error occurred
    }

    /**
     * This function fetches similar tracks to a given artist and track using the LastFM API.
     *
     * @param artist The name of the artist.
     * @param track  The name of the track.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchSimilarTracks(String artist, String track, ApiResponseListener listener) {
        // Construct the URL for the track.getsimilar API call
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=" + artist +
                "&track=" + track + "&api_key=" + lastFMapiKey + "&format=json";

        // Use GetJsonTask to fetch the similar tracks info
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                // Parse the response to get the similar tracks and artists
                String similarTracksInfo = getSimilarTracksFromJson(jsonResponse);

                if (similarTracksInfo != null) {
                    Log.d(TAG, "Similar Tracks Info: " + similarTracksInfo);
                    listener.onResponseReceived(similarTracksInfo);
                } else {
                    Log.d(TAG, "No similar tracks found!");
                    listener.onResponseReceived(null);
                }
            }
        }).execute(jsonUrl);
    }

    /**
     * This function parses the JSON response to extract similar track names and their respective artists.
     *
     * @param jsonResponse The JSON response returned by the API.
     * @return The similar tracks and artists as a formatted string.
     */
    @Nullable
    private String getSimilarTracksFromJson(String jsonResponse) {
        try {
            // Parse the JSON object
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject similarTracksObject = jsonObject.getJSONObject("similartracks");

            // Extract the similar tracks array
            JSONArray tracksArray = similarTracksObject.getJSONArray("track");

            // Build the result string to return
            StringBuilder result = new StringBuilder();
            result.append("Similar Tracks:\n");

            // Loop through the tracks and append track names, artist names, and image URLs to the result
            for (int i = 0; i < tracksArray.length(); i++) {
                JSONObject trackObject = tracksArray.getJSONObject(i);
                String trackName = trackObject.getString("name");

                // Get the artist name of the similar track
                String artistName = trackObject.getJSONObject("artist").getString("name");

                // Get the first available image URL (assuming first image object is the required size)
                JSONArray imagesArray = trackObject.getJSONArray("image");
                String imageUrl = "";
                if (imagesArray.length() > 0) {
                    imageUrl = imagesArray.getJSONObject(0).getString("#text");
                }

                // Append the details to the result
                result.append(i + 1).append(". ")
                        .append(trackName).append(" by ").append(artistName)
                        .append(" (Image: ").append(imageUrl).append(")\n");
            }

            return result.toString();  // Return formatted similar tracks with artist names and image URLs

        } catch (JSONException e) {
            e.printStackTrace();  // Print error for debugging
        }

        return null;  // Return null if an error occurred
    }

    public void fetchSimilarTracksRAW(String artist, String track, ApiResponseListener listener) {
        // Construct the URL for the track.getsimilar API call
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=" + artist +
                "&track=" + track + "&api_key=" + lastFMapiKey + "&format=json";

        // Use GetJsonTask to fetch the similar tracks info
        new GetJsonTask(new ApiResponseListener() {
            @Override
            public void onResponseReceived(String jsonResponse) {
                // Parse the response to get the similar tracks and artists
                JSONArray similarTracksInfo = getSimilarTracksFromJsonRAW(jsonResponse);
                if (similarTracksInfo != null) {
                    Log.d(TAG, "Similar Tracks Info: " + similarTracksInfo);
                    listener.onResponseReceived(String.valueOf(similarTracksInfo));
                } else {
                    Log.d(TAG, "No similar tracks found!");
                    listener.onResponseReceived(null);
                }
            }
        }).execute(jsonUrl);
    }

    @Nullable
    private JSONArray getSimilarTracksFromJsonRAW(String jsonResponse) {
        try {
            // Parse the JSON object
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject similarTracksObject = jsonObject.getJSONObject("similartracks");

            // Extract the similar tracks array
            JSONArray tracksArray = similarTracksObject.getJSONArray("track");
//
//            // Build the result string to return
//            StringBuilder result = new StringBuilder();
//            result.append("Similar Tracks:\n");
//
//            // Loop through the tracks and append track names, artist names, and image URLs to the result
//            for (int i = 0; i < tracksArray.length(); i++) {
//                JSONObject trackObject = tracksArray.getJSONObject(i);
//                String trackName = trackObject.getString("name");
//
//                // Get the artist name of the similar track
//                String artistName = trackObject.getJSONObject("artist").getString("name");
//
//                // Get the first available image URL (assuming first image object is the required size)
//                JSONArray imagesArray = trackObject.getJSONArray("image");
//                String imageUrl = "";
//                if (imagesArray.length() > 0) {
//                    imageUrl = imagesArray.getJSONObject(0).getString("#text");
//                }
//
//                // Append the details to the result
//                result.append(i + 1).append(". ")
//                        .append(trackName).append(" by ").append(artistName)
//                        .append(" (Image: ").append(imageUrl).append(")\n");
//            }
//
//            return result.toString();  // Return formatted similar tracks with artist names and image URLs
            return tracksArray;

        } catch (JSONException e) {
            e.printStackTrace();  // Print error for debugging
        }

        return null;  // Return null if an error occurred
    }


    /**
     * This function fetches information about the specified artist from TheAudioDB API.
     *
     * @param artist The name of the artist to retrieve information for.
     * @param listener The callback listener for handling the API response.
     */
    public void fetchArtistInfo(String artist, ApiResponseListener listener) {
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

    public void fetchTrackInfo(String track, String artist, ApiResponseListener listener) {
        // Construct the URL for the track info
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=" + lastFMapiKey
                + "&artist=" + artist + "&track=" + track + "&format=json";
        // Execute the task to fetch JSON
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


    public static String parseTrackDetails(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject trackObject = jsonObject.getJSONObject("track");

            String trackName = trackObject.getString("name");
            String artistName = trackObject.getJSONObject("artist").getString("name");
            String albumName = trackObject.getJSONObject("album").getString("title");
            int listeners = trackObject.getInt("listeners");
            int playcount = trackObject.getInt("playcount");
            String trackUrl = trackObject.getString("url");

            // Extracting album image
            JSONArray imageArray = trackObject.getJSONObject("album").getJSONArray("image");
            String albumImage = imageArray.getJSONObject(imageArray.length() - 1).getString("#text");

            // Extracting tags
            JSONArray tagsArray = trackObject.getJSONObject("toptags").getJSONArray("tag");
            StringBuilder tags = new StringBuilder();
            for (int i = 0; i < tagsArray.length(); i++) {
                tags.append(tagsArray.getJSONObject(i).getString("name"));
                if (i < tagsArray.length() - 1) tags.append(", ");
            }

            return "Track: " + trackName + "\n"
                    + "Artist: " + artistName + "\n"
                    + "Album: " + albumName + "\n"
                    + "Listeners: " + listeners + "\n"
                    + "Play Count: " + playcount + "\n"
                    + "Tags: " + tags + "\n"
                    + "Track URL: " + trackUrl + "\n"
                    + "Album Image: " + albumImage;
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error parsing track details.";
        }
    }

    /**
     * This method sends a POST request to retrieve an MP3 file URL from a specified YouTube video URL.
     * It constructs a JSON payload with the YouTube URL and handles the network request in a background thread.
     * The server's response, either the MP3 file URL or an error response, is returned via the provided listener.
     *
     * @param youtubeURL The URL of the YouTube video to be processed for MP3 file retrieval.
     * @param listener   The callback listener to handle the response, either the MP3 file URL or null if unsuccessful.
     */
    public void fetchMP3file(String youtubeURL, ApiResponseListener listener) {
        // Initialize OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(60, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(60, TimeUnit.SECONDS)   // Write timeout
                .build();
        Log.d(TAG, "OkHttpClient Initialized");

        // Create JSON payload
        String json = "{ \"url\": \"" + youtubeURL + "\" }";  // Match payload to API requirement
        Log.d(TAG, "JSON Payload Created: " + json);

        // Set the server URL
        String requestUrl = "http://loopify.ddnsgeek.com:20100/download";
        Log.d(TAG, "Request URL: " + requestUrl);

        // Create request body with JSON payload
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        // Build the request
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .header("Content-Type", "application/json")
                .build();

        // Start a new thread to handle network operations
        new Thread(() -> {
            Log.d(TAG, "Starting network request on a new thread");

            try {
                // Execute the request
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Response Received: " + responseData);
                    listener.onResponseReceived(responseData);
                } else {
                    Log.e(TAG, "Request Failed. Response Code: " + response.code());
                    listener.onResponseReceived(null);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException occurred during request", e);
                listener.onResponseReceived(null);
            }
        }).start();
    }


    public void fetchSongTitlesFromTxt(String url, ApiResponseListener listener) {
        OkHttpClient client = new OkHttpClient();

        // Start a new thread for network operations
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    // Read response body as a string
                    String responseData = response.body().string();

                    // Split the response into individual song titles
                    String[] titlesArray = responseData.split("\n");
                    StringBuilder titlesBuilder = new StringBuilder();

                    for (String title : titlesArray) {
                        titlesBuilder.append(title.trim()).append("\n"); // Append each title to the StringBuilder
                    }

                    // Notify the listener with the concatenated string of titles
                    listener.onResponseReceived(titlesBuilder.toString().trim());
                } else {
                    listener.onResponseReceived(null); // Handle failure
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.onResponseReceived(null); // Handle exception
            }
        }).start();
    }



    // Helper method to parse HTML and extract song names
    private List<String> parseSongListFromHtml(String html) {
        List<String> songNames = new ArrayList<>();

        // Parse links that end in .mp3 or .webm
        Pattern pattern = Pattern.compile("href=\"([^\"]+\\.(mp3|webm))\"");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String songName = matcher.group(1);  // Extract song file name
            songNames.add(songName);
        }

        return songNames;
    }




    /**
     * AsyncTask to retrieve JSON data from a specified URL in the background.
     * It opens an HTTP connection, handles timeouts, and reads the response data as a JSON string.
     * The response data is passed back to the calling activity or fragment via the provided listener.
     *
     * @param 'listener' A callback interface for handling the JSON response in the calling activity or fragment.
     */
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


    /**
     * Parses a JSON response from LastFM to find and return the name of the first matching artist.
     * The method navigates through the JSON structure to locate the artist’s name within the
     * "trackmatches" object in the "results" section.
     *
     * @param jsonResponse The JSON response string from LastFM containing potential artist matches.
     * @return The name of the first matching artist, or null if no match is found or an error occurs.
     */
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


    /**
     * Extracts the artist ID from a JSON response containing an array of tracks.
     * This method parses the JSON string to locate and return the "idArtist" value
     * from the first track object in the "track" array.
     *
     * @param jsonString The JSON string response to be parsed.
     * @return The artist's ID from the first track in the array, or null if no ID is found or if an error occurs.
     */
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

    /**
     * Searches through a JSON response to locate the YouTube URL of a specified track.
     * This method iterates through an array of music videos ("mvids") and checks each video's track name
     * against the provided trackName. When a match is found, the URL of the music video is returned.
     *
     * @param jsonString The JSON string containing an array of music videos.
     * @param trackName  The name of the track for which the YouTube URL is requested.
     * @return The URL of the music video if a matching track name is found, or null if not found or on error.
     */
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


    public void startTrackServe(String track, ApiResponseListener listener){
        fetchArtistFromTrack(track,listener);
    }

    private long trackDuration;
    public long getTrackDuration(){
        return trackDuration;
    }
    public String findMatchingSong(String userInput, String allTitles) {
        // Normalize the user input for better matching
        String normalizedInput = userInput.trim().toLowerCase();

        // Split the allTitles string into an array of individual titles
        String[] titlesArray = allTitles.split("\n");
        String bestMatch = null;
        trackDuration = -1; // Variable to store duration in milliseconds

        for (String title : titlesArray) {
            // Normalize the title for comparison
            String normalizedTitle = title.trim().toLowerCase();

            // Check if the normalized user input is contained within the normalized title
            if (normalizedTitle.contains(normalizedInput)) {
                bestMatch = title; // Store the matched title

                // Check if the title contains a duration at the end (e.g., "\210000")
                int backslashIndex = bestMatch.lastIndexOf('\\');
                if (backslashIndex != -1) {
                    // Extract the part after the backslash
                    String durationString = bestMatch.substring(backslashIndex + 1);

                    try {
                        // Parse the duration in milliseconds
                        trackDuration = Long.parseLong(durationString);
                        Log.d("ApiManager","Track Duration: " + trackDuration + " ms");

                        // Remove the backslash and duration from the title
                        bestMatch = bestMatch.substring(0, backslashIndex);
                    } catch (NumberFormatException e) {
                        Log.d("ApiManager", "Failed to parse duration: " + durationString);
                    }
                }

                break; // Exit loop after first match
            }
        }

        // Return the best matching title without the appended duration
        return bestMatch;
    }


    /**
     * Parses a JSON response to extract detailed information about an artist.
     * The method navigates through the "artists" array to retrieve attributes like the artist's name,
     * genre, biography, website, and more.
     *
     * @param jsonResponse The JSON response containing artist details.
     * @return A formatted string containing the artist's details, or null if no artist is found or an error occurs.
     */
    @Nullable
    public static String getArtistDetailsFromJson(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray artistsArray = jsonObject.getJSONArray("artists");

            if (artistsArray.length() > 0) {
                // Get the first artist object
                JSONObject artistObject = artistsArray.getJSONObject(0);

                // Extract artist details
                String artistName = artistObject.optString("strArtist", "N/A");
                String label = artistObject.optString("strLabel", "N/A");
                String formedYear = artistObject.optString("intFormedYear", "N/A");
                String bornYear = artistObject.optString("intBornYear", "N/A");
                String style = artistObject.optString("strStyle", "N/A");
                String genre = artistObject.optString("strGenre", "N/A");
                String mood = artistObject.optString("strMood", "N/A");
                String website = artistObject.optString("strWebsite", "N/A");
                String biography = artistObject.optString("strBiographyEN", "Biography not available.");

                // Format the artist details into a readable string
                return "Artist Name: " + artistName + "\n"
                        + "Label: " + label + "\n"
                        + "Formed Year: " + formedYear + "\n"
                        + "Born Year: " + bornYear + "\n"
                        + "Style: " + style + "\n"
                        + "Genre: " + genre + "\n"
                        + "Mood: " + mood + "\n"
                        + "Website: " + website + "\n\n"
                        + "Biography:\n" + biography;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing artist JSON: " + e.getMessage());
        }

        return null; // Return null if no artist was found or an error occurred
    }

    /**
     * Logs detailed artist information retrieved from a JSON response.
     * This method calls getArtistDetailsFromJson to parse the response and logs the result.
     *
     * @param jsonResponse The JSON response containing artist details.
     */
    public static void logArtistDetails(String jsonResponse) {
        String artistDetails = getArtistDetailsFromJson(jsonResponse);

        if (artistDetails != null) {
            Log.d(TAG, "Artist Details:\n" + artistDetails);
        } else {
            Log.d(TAG, "No artist details found or an error occurred.");
        }
    }

}
