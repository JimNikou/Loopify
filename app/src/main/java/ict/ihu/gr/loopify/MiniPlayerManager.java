package ict.ihu.gr.loopify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

public class MiniPlayerManager {
    private final View miniPlayerContainer;
    private final ImageView albumArt;
    private final TextView trackTitle;
    private final TextView artistName;
    private final ImageButton playPauseButton, nextButton, previousButton, stopButton;

    private boolean isPlaying = false;
    private String currentTrack = "";
    private String currentArtist = "";

    private final Context context;

    public MiniPlayerManager(FragmentActivity activity) {
        context = activity;
        miniPlayerContainer = activity.findViewById(R.id.miniPlayerContainer);
        albumArt = activity.findViewById(R.id.miniPlayerAlbumArt);
        trackTitle = activity.findViewById(R.id.miniPlayerTrackTitle);
        artistName = activity.findViewById(R.id.miniPlayerArtistName);
        previousButton = activity.findViewById(R.id.miniPlayerPreviousButton);
        playPauseButton = activity.findViewById(R.id.miniPlayerPlayPauseButton);
        nextButton = activity.findViewById(R.id.miniPlayerNextButton);
        stopButton = activity.findViewById(R.id.miniPlayerStopButton);

        setupListeners();
        registerReceivers();
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(context, MediaPlayerService.class);
            serviceIntent.setAction(isPlaying ? "PAUSE" : "PLAY");
            if (!isPlaying && currentTrack != null && !currentTrack.isEmpty()) {
                serviceIntent.putExtra("TRACK_NAME", currentTrack);
            }
            context.startService(serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            Intent stopIntent = new Intent(context, MediaPlayerService.class);
            stopIntent.setAction("STOP");
            context.startService(stopIntent);
        });

        // Previous and Next actions (if implemented in your service)
        previousButton.setOnClickListener(v -> {
            // Implement PREVIOUS action logic, if available
        });

        nextButton.setOnClickListener(v -> {
            // Implement NEXT action logic, if available
        });

        // Tapping on mini player container could reopen full player
        miniPlayerContainer.setOnClickListener(v -> {
            // Reopen the full MediaPlayerManager fragment if desired
            // Example:
            // ((MainActivity)context).loadFragment(new MediaPlayerManager());
        });
    }

    private void registerReceivers() {
        // Track info receiver
        context.registerReceiver(trackInfoReceiver, new IntentFilter("CURRENT_TRACK_INFO"), Context.RECEIVER_NOT_EXPORTED);

        // Metadata receiver
        context.registerReceiver(metadataReceiver, new IntentFilter("CURRENT_TRACK_METADATA"), Context.RECEIVER_NOT_EXPORTED);

        // Position updates (optional if you want to show current time on mini player)
        // If you want to display current progress, add a TextView and listen to this broadcast as well.
        context.registerReceiver(positionReceiver, new IntentFilter("CURRENT_TRACK_POSITION"), Context.RECEIVER_NOT_EXPORTED);
    }

    // Receivers
    private final BroadcastReceiver trackInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("CURRENT_TRACK_NAME")) {
                currentTrack = intent.getStringExtra("CURRENT_TRACK_NAME");
            }
            if (intent.hasExtra("IS_PLAYING")) {
                isPlaying = intent.getBooleanExtra("IS_PLAYING", false);
                playPauseButton.setImageResource(isPlaying ?
                        R.drawable.ic_fullscreen_media_player_pause_button :
                        R.drawable.ic_fullscreen_media_player_play_button);
            }
            showMiniPlayerIfNeeded();
        }
    };

    private final BroadcastReceiver metadataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("DURATION") && intent.hasExtra("TITLE") && intent.hasExtra("ARTIST")) {
                long duration = intent.getLongExtra("DURATION", 0);
                String title = intent.getStringExtra("TITLE");
                String artist = intent.getStringExtra("ARTIST");

                trackTitle.setText(title);
                artistName.setText(artist);
                currentArtist = artist;

                // If you have album art URL or resource, update albumArt here

                showMiniPlayerIfNeeded();
            }
        }
    };

    private final BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If you add a TextView for current time in the mini player, you can update it here.
        }
    };

    private void showMiniPlayerIfNeeded() {
        // Show the mini player if there's a current track playing or paused
        if ((currentTrack != null && !currentTrack.isEmpty())) {
            miniPlayerContainer.setVisibility(View.VISIBLE);
        }
    }

    public void onDestroy() {
        context.unregisterReceiver(trackInfoReceiver);
        context.unregisterReceiver(metadataReceiver);
        context.unregisterReceiver(positionReceiver);
    }
}
