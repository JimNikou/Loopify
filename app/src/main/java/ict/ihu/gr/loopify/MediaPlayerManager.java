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
    private ImageButton playPauseButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        playPauseButton = view.findViewById(R.id.playPauseButton);
        ImageButton stopButton = view.findViewById(R.id.stopButton);

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseSong();
            } else {
                Uri songUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.sample_audio);
                playSong(songUri);
            }
        });

        stopButton.setOnClickListener(v -> stopSong());

        return view;
    }

    public void playSong(Uri songUri) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(getContext(), songUri);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                isPlaying = true;
                updatePlayPauseIcon();
                Toast.makeText(getContext(), "Playing song...", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                updatePlayPauseIcon();
                release();
                Toast.makeText(getContext(), "Song ended.", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            Toast.makeText(getContext(), "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayPauseIcon();
            Toast.makeText(getContext(), "Song paused.", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
            updatePlayPauseIcon();
            Toast.makeText(getContext(), "Song stopped.", Toast.LENGTH_SHORT).show();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            updatePlayPauseIcon();
        }
    }

    private void updatePlayPauseIcon() {
        // Update the icon based on the isPlaying state
        if (isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_pause_button); // Set to pause icon
        } else {
            playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button); // Set to play icon
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            pauseSong(); // Pause if the fragment is no longer visible
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release(); // Release MediaPlayer resources when the fragment is destroyed
    }
}
