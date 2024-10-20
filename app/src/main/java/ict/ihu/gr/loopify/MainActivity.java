package ict.ihu.gr.loopify;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONObject;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener {
    private MediaPlayerManager mediaPlayerManager;
    private ExoPlayerManager exoPlayerManager;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;


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

        //button declaration
//        playButton = findViewById(R.id.playButton); //uncomment if you want to test the functionalities
//        stopButton = findViewById(R.id.stopButton);
//        pauseButton = findViewById(R.id.pauseButton);
//        resetButton = findViewById(R.id.resetButton);


//        String wrong_track = "Baet It";
//        String artist = "michael jackson";
        String track = "porcelain";

        exoPlayerManager = new ExoPlayerManager(this);
        new ApiManager().fetchArtistFromTrack(track,this);
//        new ApiManager().fetchTADB_Artist_ID(track, artist,this);
//        new ApiManager().fetchCorrectedTrackInfo(wrong_track, artist, this);
//        new ApiManager().fetchYtURL("112424", this);
//        new ApiManager().fetchTrackMBID(track, artist, this); // When this is finished it sends the jsonResponse to the onResponseReceived func
                                                                     // The listener is here because the MainActivity is the one listening

        //uncomment if you want to test the functionalities
//        playButton.setOnClickListener(v -> exoPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Ti mou zitas (Live).mp3?alt=media"));
//        stopButton.setOnClickListener(v -> exoPlayerManager.stopSong());
//        pauseButton.setOnClickListener(v -> {if (exoPlayerManager != null) {exoPlayerManager.pauseSong();}});
//        resetButton.setOnClickListener(v -> { if (exoPlayerManager != null) {exoPlayerManager.resetSong();}});
    }


    @Override
    public void onResponseReceived(String jsonResponse) {
        // Extract the song URL and start playing it
        String songUrl = extractSongUrlFromJson(jsonResponse);
        if (songUrl != null) {
            mediaPlayerManager.playSong(songUrl);
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
        super.onDestroy();
        exoPlayerManager.release(); // Release MediaPlayer resources
    }
}
