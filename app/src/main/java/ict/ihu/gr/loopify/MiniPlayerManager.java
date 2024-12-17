package ict.ihu.gr.loopify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

public class MiniPlayerManager {
    private final View miniPlayerContainer;
    private final ImageView albumArt;
    private final TextView trackTitle;
    private final TextView artistName;
    private final TextView miniPlayerCurrentTime;
    private final TextView miniPlayerTotalDuration;
    private final ImageButton playPauseButton, nextButton, previousButton;
    private final SeekBar miniPlayerSeekBar;

    private boolean isPlaying = false;
    private String currentTrack = "";
    private String currentArtist = "";
    private long totalDurationMs = -1; // Store total duration to help with progress updates

    private final Context context;
    private ImageButton miniPlayerLikeButton;
    private boolean isTrackLiked = false; // track the liked state locally
    private TrackHandler trackHandler = new TrackHandler();
    private ImageButton miniPlayerExpandButton;

    public MiniPlayerManager(FragmentActivity activity) {
        context = activity;
        miniPlayerContainer = activity.findViewById(R.id.miniPlayerContainer);
        albumArt = activity.findViewById(R.id.miniPlayerAlbumArt);
        trackTitle = activity.findViewById(R.id.miniPlayerTrackTitle);
        artistName = activity.findViewById(R.id.miniPlayerArtistName);
        miniPlayerCurrentTime = activity.findViewById(R.id.miniPlayerCurrentTime);
        miniPlayerTotalDuration = activity.findViewById(R.id.miniPlayerTotalDuration);
        previousButton = activity.findViewById(R.id.miniPlayerPreviousButton);
        playPauseButton = activity.findViewById(R.id.miniPlayerPlayPauseButton);
        nextButton = activity.findViewById(R.id.miniPlayerNextButton);
        miniPlayerSeekBar = activity.findViewById(R.id.miniPlayerSeekBar);
        miniPlayerExpandButton = activity.findViewById(R.id.miniPlayerExpandButton);


        setupListeners();
        registerReceivers();
        miniPlayerLikeButton = activity.findViewById(R.id.miniPlayerLikeButton);
        miniPlayerLikeButton.setOnClickListener(v -> {
            if (currentTrack != null && !currentTrack.isEmpty()) {
                if (isTrackLiked) {
                    trackHandler.removeSongFromLiked(currentTrack);
                    isTrackLiked = false;
                    miniPlayerLikeButton.setImageResource(R.drawable.heartlikedsongsemptyicon);
                } else {
                    trackHandler.addSongToLiked(currentTrack);
                    isTrackLiked = true;
                    miniPlayerLikeButton.setImageResource(R.drawable.heartlikedsongsfullicon);
                }
            }
        });
        miniPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userIsSeeking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // If user is dragging the seekbar, you could update the miniPlayerCurrentTime here
                // But only if fromUser is true and you want live feedback
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
                int newPositionMs = seekBar.getProgress() * 1000; // Convert seconds to ms
                Intent seekIntent = new Intent(context, MediaPlayerService.class);
                seekIntent.setAction("SEEK");
                seekIntent.putExtra("NEW_POSITION", newPositionMs);
                context.startService(seekIntent);
            }
        });
    }

    private void setupListeners() {
        miniPlayerContainer.setOnClickListener(v -> {
            // Check that we are not clicking on controls - this won't be needed as controls have their own listeners.
            // This onClick will only fire if user taps empty space.

            // Load the full media player fragment.
            // For example, if MainActivity has a method loadFragment(Fragment fragment):
            ((MainActivity) context).loadFragment(new MediaPlayerManager());

            // Hide the mini player after opening full player
            miniPlayerContainer.setVisibility(View.GONE);
        });
        playPauseButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(context, MediaPlayerService.class);
            serviceIntent.setAction(isPlaying ? "PAUSE" : "PLAY");
            if (!isPlaying && currentTrack != null && !currentTrack.isEmpty()) {
                serviceIntent.putExtra("TRACK_NAME", currentTrack);
            }
            context.startService(serviceIntent);
        });


        miniPlayerExpandButton.setOnClickListener(v -> {
            // Load the full media player fragment
            ((MainActivity) context).loadFragment(new MediaPlayerManager());

            // Hide the mini player
            miniPlayerContainer.setVisibility(View.GONE);
        });
        // Implement PREVIOUS and NEXT if your service supports these actions
        previousButton.setOnClickListener(v -> {
            // Example: send a PREVIOUS action to the service if implemented
        });

        nextButton.setOnClickListener(v -> {
            // Example: send a NEXT action to the service if implemented
        });

        // Tapping on mini player container could reopen full player
        miniPlayerContainer.setOnClickListener(v -> {
            // ((MainActivity)context).loadFragment(new MediaPlayerManager());
        });
    }

    private void registerReceivers() {
        // Track info receiver
        context.registerReceiver(trackInfoReceiver, new IntentFilter("CURRENT_TRACK_INFO"), Context.RECEIVER_NOT_EXPORTED);

        // Metadata receiver
        context.registerReceiver(metadataReceiver, new IntentFilter("CURRENT_TRACK_METADATA"), Context.RECEIVER_NOT_EXPORTED);

        // Position updates
        context.registerReceiver(positionReceiver, new IntentFilter("CURRENT_TRACK_POSITION"), Context.RECEIVER_NOT_EXPORTED);
    }

    // Track Info Receiver
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

    // Metadata Receiver
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

                totalDurationMs = duration;
                miniPlayerTotalDuration.setText(formatTime(duration));
                miniPlayerSeekBar.setMax((int)(duration / 1000)); // Set SeekBar max in seconds

                showMiniPlayerIfNeeded();
            }
        }
    };

    // Position Receiver
    private final BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("POSITION")) {
                long position = intent.getLongExtra("POSITION", 0);
                miniPlayerCurrentTime.setText(formatTime(position));
                // Update SeekBar progress based on position (in seconds)
                miniPlayerSeekBar.setProgress((int) (position / 1000));
            }
        }
    };

    private void showMiniPlayerIfNeeded() {
        if ((currentTrack != null && !currentTrack.isEmpty())) {
            miniPlayerContainer.setVisibility(View.VISIBLE);
        }
    }

    public void onDestroy() {
        context.unregisterReceiver(trackInfoReceiver);
        context.unregisterReceiver(metadataReceiver);
        context.unregisterReceiver(positionReceiver);
    }

    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
