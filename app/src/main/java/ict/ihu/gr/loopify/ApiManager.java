package ict.ihu.gr.loopify;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class ApiManager {
    private static final String TAG = "ApiManager";
    private final String apiKey = "a38306489162f067667f1b911c8345c5"; // Na to kanoume store ektos public repo!

    public void fetchTrackInfo(String track, String artist, ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=" + apiKey + "&artist=" + artist +
                "&track=" + track + "&format=json";
//        Log.d(TAG, jsonUrl);
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getcorrection&artist=micahel jackson&api_key=a38306489162f067667f1b911c8345c5&format=json";
//        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=a38306489162f067667f1b911c8345c5&artist=cher&track=believe&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }

    public void fetchArtistInfo(String artist, ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=" + apiKey +
                "&artist=" + artist + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }

    public void fetchCorrectedArtistInfo(String artist, ApiResponseListener listener) {
        String jsonUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getCorrection&api_key=" + apiKey +
                "&artist=" + artist + "&format=json";
        new GetJsonTask(listener).execute(jsonUrl);
    }


    private static class GetJsonTask extends AsyncTask<String, Void, String> { // Async task for the retrieval of the json info about the artist and the song
        private final ApiResponseListener listener;

        public GetJsonTask(ApiResponseListener listener) { // Class constructor, declaring up the listener that is MainActivity in this case, because the call was from there.
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... urls) { // Breakdown the json so that the data is separate
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
            listener.onResponseReceived(result); // Inform MainActivity that the data is ready to be parsed
        }
    }

    public interface ApiResponseListener { // Call back mechanism to inform the MainActivity that the data is ready
        void onResponseReceived(String jsonResponse);
    }
}
