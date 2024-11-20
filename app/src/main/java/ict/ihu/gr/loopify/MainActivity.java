package ict.ihu.gr.loopify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import ict.ihu.gr.loopify.databinding.ActivityMainBinding;

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

        // Configure AppBar with Drawer and BottomNav items
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_library,
                R.id.nav_notification, R.id.nav_settings, R.id.nav_account)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Link Navigation Drawer and BottomNav with NavController
        NavigationView navigationView = binding.navView;
        NavigationUI.setupWithNavController(navigationView, navController);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        mediaPlayerManager = new MediaPlayerManager();

        // Setup "Open Media Player" button click to load the media player fragment
        Button openMediaPlayerButton = findViewById(R.id.open_media_player_button);
        openMediaPlayerButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Button clicked, trying to load MediaPlayerManager");
            loadFragment(new MediaPlayerManager());
            Log.d("MainActivity", "MediaPlayerManager fragment should now be visible.");
        });

        // Button setup for additional media controls (if needed)
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);

        // Example of controlling playback (uncomment if needed)
        playButton.setOnClickListener(v -> startMusicService("PLAY"));
        pauseButton.setOnClickListener(v -> {
            if (exoPlayerManager != null) exoPlayerManager.pauseSong();
            startMusicService("PAUSE");
        });
        stopButton.setOnClickListener(v -> {
            exoPlayerManager.stopSong();
        });

        createNotificationChannel();
    }

    // Create notification channel for media playback
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "MEDIA_CHANNEL_ID", "Media Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // Method to start the media service with an action
    public void startMusicService(String action) {
        Intent serviceIntent = new Intent(this, MediaPlayerService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent);
    }

    // Handle API response (if needed)
    @Override
    public void onResponseReceived(String jsonResponse) {
        String songUrl = extractSongUrlFromJson(jsonResponse);
        if (songUrl != null) {
            mediaPlayerManager.playSong(Uri.parse(songUrl));
        }
    }

    // Helper method to extract song URL from JSON
    private String extractSongUrlFromJson(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONObject("track").getString("url");
        } catch (Exception e) {
            Log.e("MainActivity", "Error extracting song URL: " + e.getMessage());
            return null;
        }
    }

    // Method to load the MediaPlayerManager fragment as a full-screen overlay
    private void loadFragment(Fragment fragment) {
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container_MediaPlayerFragment);
        fragmentContainer.setVisibility(View.VISIBLE);
        fragmentContainer.setBackgroundColor(getResources().getColor(android.R.color.black));

        // Hide other views to simulate full-screen effect
        findViewById(R.id.nav_host_fragment_content_main).setVisibility(View.GONE);
        findViewById(R.id.bottomNavView).setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(
//                R.anim.slide_in_up,  // Enter animation
//                R.anim.fade_out,     // Exit animation
//                R.anim.fade_in,      // Pop enter animation
//                R.anim.slide_out_down // Pop exit animation
//        );
        transaction.replace(R.id.fragment_container_MediaPlayerFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Handle back press to close the media player fragment and restore layout
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            // Restore visibility of main content and bottom navigation
            findViewById(R.id.nav_host_fragment_content_main).setVisibility(View.VISIBLE);
            findViewById(R.id.bottomNavView).setVisibility(View.VISIBLE);

            // Hide the fragment container to avoid overlaying the restored content
            findViewById(R.id.fragment_container_MediaPlayerFragment).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_MediaPlayerFragment).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            super.onBackPressed();
        }
    }

    // Handle menu creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Support navigate up in NavController
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
        exoPlayerManager.release();
    }
}
