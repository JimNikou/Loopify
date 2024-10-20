package ict.ihu.gr.loopify;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;

//TEST TEST TEST TEST TEST TEST TEST TEST TEST
public class MainActivity extends AppCompatActivity implements ApiManager.ApiResponseListener {
    private MediaPlayerManager mediaPlayerManager;
    private ExoPlayerManager exoPlayerManager;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Button playButton, stopButton, pauseButton, resetButton;

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

        Button openMediaPlayerButton = findViewById(R.id.open_media_player_button);
        openMediaPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked, trying to load MediaPlayerManager");
                loadFragment(new MediaPlayerManager());
                Log.d("MainActivity", "MediaPlayerManager fragment should now be visible.");
            }
        });

        // Other initializations...



//        String artist = "Light";
//        String track = "mosca";
//        String wrong_artist = "Michale Jackson";

//        exoPlayerManager = new ExoPlayerManager(this);
//        new ApiManager().fetchCorrectedArtistInfo(wrong_artist, this);
//        new ApiManager().fetchArtistInfo(artist, this);
//        new ApiManager().fetchTrackInfo(track, artist, this); // When this is finished it sends the jsonResponse to the onResponseReceived func
//                                                                     // The listener is here because the MainActivity is the one listening

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
    }

}
