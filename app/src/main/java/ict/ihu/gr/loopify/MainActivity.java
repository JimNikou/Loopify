package ict.ihu.gr.loopify;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import ict.ihu.gr.loopify.databinding.ActivityMainBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.media.MediaPlayer;
import org.json.JSONObject;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Button playButton, stopButton;
    private static final String TAG = "Result";
    private static final String JSON_URL = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=a38306489162f067667f1b911c8345c5&artist=light&track=mosca&format=json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        new GetJsonTask().execute(JSON_URL);

//        String audioUrl = "http://192.168.1.6:27000/getAudioStream?youtubeLink=https://www.youtube.com/watch?v=RexKHDaSzkI";

        // Replace with the URL from the GET api (the audiodb)
        String url = "https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Light - Mosca (Official Music Video).mp3?alt=media";


        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        mediaPlayer = new MediaPlayer();
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // If the media player is null or not playing, start the music
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }

                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.reset(); // Reset media player to avoid conflicts
                        mediaPlayer.setDataSource(url); // Set data source to the URL
                        mediaPlayer.prepare();  // Prepare asynchronously or synchronously (blocking)
                        mediaPlayer.start();    // Start playing the music
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set up the stop button to stop the music
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset(); // Reset player after stopping
                }
            }
        });
    }


    //=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
    //=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=


    public String getTrackUrl(String jsonResponse) {
            try {
                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(jsonResponse);
                // Get the track object
                JSONObject track = jsonObject.getJSONObject("track");
                // Get the URL from the track object
                String trackUrl = track.getString("url");
                return trackUrl; // Return the track URL
            } catch (Exception e) {
                e.printStackTrace(); // Print the stack trace for debugging
                return null; // Return null if there's an error
            }
        }

    private class GetJsonTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String jsonResponse = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000); // 10 seconds timeout
                urlConnection.setReadTimeout(10000);    // 10 seconds timeout

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
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            // Log the JSON response
            String actual_URL = getTrackUrl(result);
            if (result != null && !result.isEmpty()) {
                Log.d(TAG, "JSON Response: " + actual_URL);
                playSong(result); // Call method to play the song
            } else {
                Log.e(TAG, "No response received");
            }
        }
    }

    private void playSong(String jsonResponse) {
        try {
            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String songUrl = jsonObject.getJSONObject("track").getString("url"); // Extract the song URL

            // Initialize MediaPlayer with the song URL
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync(); // Prepare asynchronously to avoid blocking the UI thread
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start()); // Start playback when prepared
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release(); // Release the MediaPlayer resources when done
                mediaPlayer = null;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}