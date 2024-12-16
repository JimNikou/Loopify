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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MediaPlayerManager extends Fragment {
    public MediaPlayer mediaPlayer;
    private static final String TAG = "MediaPlayerManager";
    public boolean isPlaying = false;
    public ImageButton playPauseButton;
    public  ExoPlayerManager exoPlayerManager;
    private String currentPlayingTrack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        // ImageButton for play/pause functionality
        playPauseButton = view.findViewById(R.id.playPauseButton);

        playPauseButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(getContext(), MediaPlayerService.class);
            if (isPlaying) {
                // Pause action
                serviceIntent.setAction("PAUSE");
            } else {
                // Play action with the dynamic track name
                serviceIntent.setAction("PLAY");
                // Use the stored currentPlayingTrack instead of a hard-coded name
                if (currentPlayingTrack != null && !currentPlayingTrack.isEmpty()) {
                    serviceIntent.putExtra("TRACK_NAME", currentPlayingTrack);
                }
            }
            getContext().startService(serviceIntent);
        });


        // ImageButton for stopping the song
        ImageButton stopButton = view.findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> {
            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button); // Ensure the play icon is shown when stopped
            Toast.makeText(getContext(), "Song stopped.", Toast.LENGTH_SHORT).show();
        });

        view.bringToFront();
        view.setElevation(10);  // Set a high elevation for layering on top (adjust as needed)
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


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("CURRENT_TRACK_INFO");
        getContext().registerReceiver(trackInfoReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(trackInfoReceiver);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}