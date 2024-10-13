package ict.ihu.gr.loopify;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener {
    private MediaPlayerManager mediaPlayerManager;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Button playButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationView navigationView = binding.navView;
        NavigationUI.setupWithNavController(navigationView, navController);

        mediaPlayerManager = new MediaPlayerManager();

        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);


        String artist = "light";
        String track = "mosca";

        new ApiManager().fetchTrackInfo(artist, track, this); // When this is finished it sends the jsonResponse to the onResponseReceived func
                                                                     // The listener is here because the MainActivity is the one listening
        playButton.setOnClickListener(v -> mediaPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/Light - Mosca (Official Music Video).mp3?alt=media"));
        stopButton.setOnClickListener(v -> mediaPlayerManager.stopSong());
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
        mediaPlayerManager.release(); // Release MediaPlayer resources
    }
}
