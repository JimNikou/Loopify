package ict.ihu.gr.loopify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MediaPlayerManager extends Fragment {
    public MediaPlayer mediaPlayer;
    private static final String TAG = "MediaPlayerManager";
    public boolean isPlaying = false;
    public ImageButton playPauseButton;
    public ExoPlayerManager exoPlayerManager;
    private String currentPlayingTrack;
    private TextView songTitleTextView, artistTextView, currentTimeTextView, totalDurationTextView;
    private SeekBar seekBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        playPauseButton = view.findViewById(R.id.playPauseButton);
        ImageButton stopButton = view.findViewById(R.id.stopButton);
        songTitleTextView = view.findViewById(R.id.songTitleTextView);
        artistTextView = view.findViewById(R.id.artistTextView);
        currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
        totalDurationTextView = view.findViewById(R.id.totalDurationTextView);
        seekBar = view.findViewById(R.id.seekBar);

        playPauseButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(getContext(), MediaPlayerService.class);
            if (isPlaying) {
                // Pause action
                serviceIntent.setAction("PAUSE");
            } else {
                // Play action with the dynamic track name
                serviceIntent.setAction("PLAY");
                if (currentPlayingTrack != null && !currentPlayingTrack.isEmpty()) {
                    serviceIntent.putExtra("TRACK_NAME", currentPlayingTrack);
                }
            }
            getContext().startService(serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            // Send a STOP action to the service
            Intent stopIntent = new Intent(getContext(), MediaPlayerService.class);
            stopIntent.setAction("STOP");
            getContext().startService(stopIntent);

            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button);
            Toast.makeText(getContext(), "Song stopped.", Toast.LENGTH_SHORT).show();
        });

        // Set a listener for the SeekBar to allow user seeking
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userIsSeeking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // No immediate action needed on every progress change unless desired
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
                // Send a SEEK action to the service
                int newPositionMs = seekBar.getProgress() * 1000; // Convert to ms
                Intent seekIntent = new Intent(getContext(), MediaPlayerService.class);
                seekIntent.setAction("SEEK");
                seekIntent.putExtra("NEW_POSITION", newPositionMs);
                getContext().startService(seekIntent);
            }
        });

        view.bringToFront();
        view.setElevation(10);
        return view;
    }

    private BroadcastReceiver trackInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("CURRENT_TRACK_NAME")) {
                currentPlayingTrack = intent.getStringExtra("CURRENT_TRACK_NAME");
            }
            if (intent.hasExtra("IS_PLAYING")) {
                isPlaying = intent.getBooleanExtra("IS_PLAYING", false);
                // Update the UI button icon based on isPlaying
                if (isPlaying) {
                    playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_pause_button);
                } else {
                    playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button);
                }
            }
        }
    };

    private BroadcastReceiver metadataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("DURATION") && intent.hasExtra("TITLE") && intent.hasExtra("ARTIST")) {
                long duration = intent.getLongExtra("DURATION", 0);
                String title = intent.getStringExtra("TITLE");
                String artist = intent.getStringExtra("ARTIST");

                songTitleTextView.setText(title);
                artistTextView.setText(artist);
                totalDurationTextView.setText(formatTime(duration));
                seekBar.setMax((int) (duration / 1000));
            }
        }
    };

    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("POSITION")) {
                long position = intent.getLongExtra("POSITION", 0);
                currentTimeTextView.setText(formatTime(position));
                seekBar.setProgress((int) (position / 1000));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter infoFilter = new IntentFilter("CURRENT_TRACK_INFO");
        getContext().registerReceiver(trackInfoReceiver, infoFilter, Context.RECEIVER_NOT_EXPORTED);

        IntentFilter metadataFilter = new IntentFilter("CURRENT_TRACK_METADATA");
        getContext().registerReceiver(metadataReceiver, metadataFilter, Context.RECEIVER_NOT_EXPORTED);

        IntentFilter positionFilter = new IntentFilter("CURRENT_TRACK_POSITION");
        getContext().registerReceiver(positionReceiver, positionFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(trackInfoReceiver);
        getContext().unregisterReceiver(metadataReceiver);
        getContext().unregisterReceiver(positionReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
