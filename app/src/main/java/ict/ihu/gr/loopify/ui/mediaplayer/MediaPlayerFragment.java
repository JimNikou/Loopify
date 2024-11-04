package ict.ihu.gr.loopify.ui.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.TimeUnit;

import ict.ihu.gr.loopify.R;

public class MediaPlayerFragment extends Fragment {

    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private static MediaPlayer mediaPlayer;  // Singleton MediaPlayer instance
    private VideoView videoView;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    private TextView currentTimeTextView, totalDurationTextView;
    private boolean isUserSeeking = false;  // To track if the user is manually moving the SeekBar
    private boolean isPlaying = false;  // Track the play/pause state
    private View albumArtImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_player_fragment, container, false);

        // Initialize UI elements
        playPauseButton = view.findViewById(R.id.playPauseButton);
        seekBar = view.findViewById(R.id.seekBar);
        currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
        totalDurationTextView = view.findViewById(R.id.totalDurationTextView);
//        videoView = view.findViewById(R.id.backgroundVideoView);
        albumArtImageView = view.findViewById(R.id.albumArtImageView);

        // Get singleton MediaPlayer instance
        mediaPlayer = getMediaPlayerInstance();

        // Setup VideoView with background video
        Uri videoUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.background_video);  // Replace with your video
        videoView.setVideoURI(videoUri);

        // Set the total duration
        totalDurationTextView.setText(formatTime(mediaPlayer.getDuration()));

        // Set the max value of the SeekBar to the duration of the media file
        seekBar.setMax(mediaPlayer.getDuration());

        // Play/Pause Button Click Listener
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseMedia();
            } else {
                startMedia();
            }
        });

        // SeekBar Change Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isUserSeeking = true;
                    mediaPlayer.seekTo(progress);  // Seek to the position chosen by the user
                    videoView.seekTo(progress);  // Sync video to the same position
                    currentTimeTextView.setText(formatTime(progress));  // Update current time immediately
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);  // Stop SeekBar updates while user is dragging
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());  // Seek to the final position
                videoView.seekTo(seekBar.getProgress());  // Sync video to the same position
                isUserSeeking = false;  // User is done seeking
                updateSeekBar();  // Resume automatic updates
            }
        });

        // SeekBar Update Logic
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && !isUserSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);  // Update SeekBar with current position
                    currentTimeTextView.setText(formatTime(currentPosition));  // Update current time text
                }
                handler.postDelayed(this, 200);  // Run again after 200ms
            }
        };

        return view;
    }

    // Singleton method to create/get MediaPlayer instance
    private static MediaPlayer getMediaPlayerInstance() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(null, R.raw.sample_audio);  // Assuming sample_audio.mp3 exists in res/raw
        }
        return mediaPlayer;
    }

    // Function to start media (both audio and video)
    private void startMedia() {
        mediaPlayer.start();  // Start audio playback
        videoView.setVisibility(View.VISIBLE);  // Show the VideoView
        albumArtImageView.setVisibility(View.GONE);  // Hide the album art
//        videoView.start();  // Start video playback
        playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_pause_button);  // Change to pause icon
        isPlaying = true;
        updateSeekBar();  // Start updating the SeekBar
    }


    // Function to pause media (both audio and video)
    private void pauseMedia() {
        mediaPlayer.pause();  // Pause audio playback
        videoView.pause();  // Pause video playback
//        videoView.setVisibility(View.GONE);  // Hide the VideoView if needed
        albumArtImageView.setVisibility(View.VISIBLE);  // Show the album art
        playPauseButton.setImageResource(R.drawable.ic_fullscreen_media_player_play_button);  // Change to play icon
        isPlaying = false;
    }

    // Function to start SeekBar updates
    private void updateSeekBar() {
        if (mediaPlayer != null && !isUserSeeking) {
            seekBar.setMax(mediaPlayer.getDuration());  // Ensure SeekBar max is set to media duration
            handler.post(updateSeekBarRunnable);  // Start updating immediately
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release media player resources
            mediaPlayer = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();  // Stop video playback when fragment is destroyed
        }
        handler.removeCallbacks(updateSeekBarRunnable);  // Stop SeekBar updates
    }

    // Utility function to format time in mm:ss
    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }
}

