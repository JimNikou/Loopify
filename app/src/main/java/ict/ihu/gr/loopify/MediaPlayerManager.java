package ict.ihu.gr.loopify;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class MediaPlayerManager extends Fragment {
    private MediaPlayer mediaPlayer;
    private static final String TAG = "MediaPlayerManager";
    private boolean isPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        // ImageButton for play/pause functionality
        ImageButton playPauseButton = view.findViewById(R.id.playPauseButton);

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseSong();
                Toast.makeText(getContext(), "Song paused.", Toast.LENGTH_SHORT).show();
            } else {
                // Example local file in raw folder, or replace with your URL
                Uri songUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.sample_audio);
                playSong(songUri);
                Toast.makeText(getContext(), "Playing song...", Toast.LENGTH_SHORT).show();
            }
        });

        // ImageButton for stopping the song
        ImageButton stopButton = view.findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> {
            stopSong();
            Toast.makeText(getContext(), "Song stopped.", Toast.LENGTH_SHORT).show();
        });
        view.bringToFront();
        view.setElevation(10);  // Set a high elevation for layering on top (adjust as needed)
        return view;
    }

    public void playSong(Uri songUri) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset(); // Reset the player if it's already initialized
            }

            mediaPlayer.setDataSource(getContext(), songUri); // Set the data source as a URI
            mediaPlayer.prepareAsync(); // Prepare asynchronously

            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start(); // Start playback when the player is ready
                isPlaying = true; // Update the state
            });

            mediaPlayer.setOnCompletionListener(mp -> release()); // Release resources when the song is complete
        } catch (IOException e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false; // Update the state
        }
    }

    public void stopSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset(); // Reset after stopping
            isPlaying = false; // Update the state
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false; // Reset the state
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release(); // Release MediaPlayer resources when the fragment is destroyed
    }
}
