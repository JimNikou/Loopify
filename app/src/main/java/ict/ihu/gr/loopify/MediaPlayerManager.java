package ict.ihu.gr.loopify;

import android.media.MediaPlayer;
import android.net.Uri;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        // ImageButton for play/pause functionality
        playPauseButton = view.findViewById(R.id.playPauseButton);

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button); // Change to play icon
                Toast.makeText(getContext(), "Song paused.", Toast.LENGTH_SHORT).show();
            } else {
                // Example local file in raw folder, or replace with your URL
                Uri songUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.sample_audio);
                Toast.makeText(getContext(), "Playing song...", Toast.LENGTH_SHORT).show();
            }
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



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}