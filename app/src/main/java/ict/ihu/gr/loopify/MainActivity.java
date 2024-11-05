package ict.ihu.gr.loopify;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import ict.ihu.gr.loopify.databinding.ActivityMainBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

//TEST TEST TEST TEST TEST TEST TEST TEST TEST
public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener {
    private MediaPlayerManager mediaPlayerManager;
    private ExoPlayerManager exoPlayerManager;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public String songDuration;

    private Button playButton, stopButton, pauseButton, resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Create AppBarConfiguration with both BottomNav and Drawer items
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_library,  // Bottom navigation items
                R.id.nav_notification, R.id.nav_settings, R.id.nav_account)  // Drawer items
                .setOpenableLayout(binding.drawerLayout)  // Associate drawer layout
                .build();

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Link the toolbar with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Link the Navigation Drawer with NavController
        NavigationView navigationView = binding.navView;
        NavigationUI.setupWithNavController(navigationView, navController);

        // Link the BottomNavigationView with NavController
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        mediaPlayerManager = new MediaPlayerManager();

        Button openMediaPlayerButton = findViewById(R.id.open_media_player_button);
        openMediaPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked, trying to load MediaPlayerManager");
                loadFragment(new MediaPlayerManager());
                Log.d("MainActivity", "MediaPlayerManager fragment should now be visible.");
            }
        });

        //button declaration
        playButton = findViewById(R.id.playButton); //uncomment if you want to test the functionalities
        stopButton = findViewById(R.id.stopButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);

//        playButton.setVisibility(View.INVISIBLE);
//        stopButton.setVisibility(View.INVISIBLE);
//        pauseButton.setVisibility(View.INVISIBLE);
//        resetButton.setVisibility(View.INVISIBLE);

//        String wrong_track = "Baet It";
//        String artist = "moby";
        String track = "heartless";

        ApiManager apiManager = new ApiManager();
        exoPlayerManager = new ExoPlayerManager(this);
//        new ApiManager().fetchArtistFromTrack(track,this); //get back the artist from a selected track
//        new ApiManager().fetchTADB_Artist_ID(track, artist,this); //get the artist id for track search
//        new ApiManager().fetchCorrectedTrackInfo(wrong_track, artist, this); //get corrected artist info
//        new ApiManager().fetchYtURL("112424", this); //get youtube URL for a specified track with the id
//        new ApiManager().fetchAlbumInfo("Cher", "Believe", this);

        // OTI EINAI PANW APO AUTO TO COMMENT EINAI DEPRECATED, XRHSIMOPOIEITAI TA KATW CALLS GIA TA API GIA NA PARETE PISW MIA TIMH TA ALLA EINAI IN SERIES CONNECTED

//        new ApiManager().startTrackServe(track,this); // kanei olh thn diadikasia apo to na brei ton kalitexnh mexri na katebasei to tragoudi (menei na kanei elenxo ean einai)
                                                        // hdh katebasmeno

//        apiManager.fetchMP3file("https://www.youtube.com/watch?v=Jy1D6caG8nU", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if(jsonResponse != null){
//                    Log.d("mp3song", jsonResponse);
//                }else {
//                    Log.d("mp3song", "No artist found");
//                }
//            }
//        });
//        apiManager.fetchArtistFromTrack("porcelain", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if(jsonResponse != null){
//                    Log.d("FetchArtistReturn", jsonResponse);
//                }else {
//                    Log.d("FetchArtistReturn", "No artist found");
//                }
//            }
//        });
//        apiManager.fetchAlbumInfo("Cher", "Believe", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null) {
//                    // Handle the album info (e.g., display it in UI)
//                    Log.d("AlbumInfo", jsonResponse);
//                } else {
//                    Log.d("AlbumInfo", "No album info found");
//                }
//            }
//        });
//
//        apiManager.fetchGenreInfo("disco", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null) {
//                    // Handle the genre info (e.g., display it in UI)
//                    Log.d("GenreInfo", jsonResponse);
//                } else {
//                    Log.d("GenreInfo", "No genre info found");
//                }
//            }
//        });
//
//        apiManager.fetchSimilarTracks("moby", "porcelain", new ApiManager.ApiResponseListener() {
//            @Override
//            public void onResponseReceived(String jsonResponse) {
//                if (jsonResponse != null){
//                    Log.d("SimilarTracks", jsonResponse);
//                }else {
//                    Log.d("SimilarTracks", "No similar tracks found");
//                }
//            }
//        });

//        new ApiManager().fetchTrackMBID(track, artist, this); // When this is finished it sends the jsonResponse to the onResponseReceived func
                                                                     // The listener is here because the MainActivity is the one listening
      

        //uncomment if you want to test the functionalities
        playButton.setOnClickListener(v -> {
//            exoPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Ti%20mou%20zitas%20(Live).mp3?alt=media");
            startMusicService("PLAY");
        });

        pauseButton.setOnClickListener(v -> {
            if (exoPlayerManager != null) {
                exoPlayerManager.pauseSong();
            }
            startMusicService("PAUSE");
        });
//        stopButton.setOnClickListener(v -> exoPlayerManager.stopSong());
//        resetButton.setOnClickListener(v -> { if (exoPlayerManager != null) {exoPlayerManager.resetSong();}});

        createNotificationChannel();
    }

    private boolean isTrackServing = false; // Flag to prevent multiple calls

    void runStartTrackServe(String track, ExoPlayerManager exo) {
        if (isTrackServing) {
            Log.d("ApiManager", "Track is already being served, ignoring further calls.");
            return;
        }

        isTrackServing = true; // Set the flag to true to prevent further calls

        ApiManager apiManager = new ApiManager();

        // Step 1: Check if the track is available in downloads
        checkTrackAvailability(track, apiManager, exo);
    }

    private void checkTrackAvailability(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
            if (response != null) {
                String matchedTitle = apiManager.findMatchingSong(track, response);
                if (matchedTitle != null) {
                    playTrack(matchedTitle, exo); // Track found, play directly
                } else {
                    Log.d("ApiManager", "No matching song found in downloads, starting download process.");
                    startTrackDownload(track, apiManager, exo); // Track not found, proceed to download
                }
            } else {
                Log.d("ApiManager", "Failed to fetch song titles.");
                isTrackServing = false;
            }
        });
    }

    private void startTrackDownload(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.startTrackServe(track, jsonResponse -> {
            if (jsonResponse != null) {
                Log.d("ApiManager", "Streaming started successfully: " + jsonResponse);
                // After downloading, check again if the track is available
                apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
                    if (response != null) {
                        String matchedTitle = apiManager.findMatchingSong(track, response);
                        if (matchedTitle != null) {
                            playTrack(matchedTitle, exo); // Play the newly downloaded track
                        } else {
                            Log.d("ApiManager", "Failed to locate the downloaded song.");
                        }
                    } else {
                        Log.d("ApiManager", "Failed to fetch song titles after download.");
                    }
                    isTrackServing = false; // Reset flag after final processing
                });
            } else {
                Log.d("ApiManager", "Download response was null.");
                isTrackServing = false;
            }
        });
    }

    private void playTrack(String matchedTitle, ExoPlayerManager exo) {
        String completeUrl = "http://loopify.ddnsgeek.com:20080/downloads/" + matchedTitle.trim() + ".webm";
        Log.d("ApiManager", "Playing track: " + completeUrl);

        // Run on the UI thread to start playback
        runOnUiThread(() -> {
            exo.playSong(completeUrl);
            exo.setDurationListener(duration -> Log.d("ApiManager", "Song duration: " + duration));
        });

        isTrackServing = false; // Reset flag after playback begins
    }



    public static String chatGPT(String message){
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "";
        return "";
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "MEDIA_CHANNEL_ID",
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void startMusicService(String action) {
        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent); // Start the service with the action
    }

    @Override
    public void onResponseReceived(String jsonResponse) {
        // Extract the song URL and start playing it
        String songUrl = extractSongUrlFromJson(jsonResponse);
        if (songUrl != null) {
            mediaPlayerManager.playSong(Uri.parse(songUrl));
        }
    }

    private String extractSongUrlFromJson(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("track").getString("url"); // Adjust as needed for the actual URL
        } catch (Exception e) {
            Log.e("MainActivity", "Error extracting song URL: " + e.getMessage());
            return null;
        }
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        if (exoPlayerManager != null) {
            exoPlayerManager.release();
        }
        super.onDestroy();
        startMusicService("STOP");
        exoPlayerManager.release(); // Release MediaPlayer resources
    }

}
