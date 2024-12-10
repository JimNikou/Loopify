package ict.ihu.gr.loopify;

import android.content.Context;
import android.widget.ImageButton;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

public class ExoPlayerManager {
    private ExoPlayer exoPlayer;
    private boolean isMediaLoaded = false; // To check if the media has been loaded
    private DurationListener durationListener;
    private long storedDuration = C.TIME_UNSET;
    public ImageButton playPauseButton;

    // Initialize ExoPlayer with the application context
    public ExoPlayerManager(Context context) {
        exoPlayer = new ExoPlayer.Builder(context).build();

        // a listener to get duration when media is prepared
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    isMediaLoaded = true;
                    storedDuration = exoPlayer.getDuration();

                    // Trigger listener if set, and log the duration
                    if (durationListener != null) {
                        durationListener.onDurationReady(formatDuration(storedDuration));
                    }
//                    Log.d("ExoPlayerManager", "Song Duration: " + formatDuration(storedDuration));
                }
            }
        });
    }

    // Define a listener interface to be notified when duration is ready
    public interface DurationListener {
        void onDurationReady(String duration);
    }
    public String getStoredDuration() {
        return formatDuration(storedDuration);
    }
    private String formatDuration(long durationMs) {
        if (durationMs == C.TIME_UNSET) {
            return "Duration not available";
        } else {
            long minutes = (durationMs / 1000) / 60;
            long seconds = (durationMs / 1000) % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public void setDurationListener(DurationListener listener) {
        this.durationListener = listener;
    }
    // Method to get the duration of the currently loaded song
    public String getSongDuration() {
        if (isMediaLoaded) {
            long durationMs = exoPlayer.getDuration();
            if (durationMs == C.TIME_UNSET) {  // Corrected here
                return "Duration not available";
            } else {
                // Convert milliseconds to minutes and seconds
                long minutes = (durationMs / 1000) / 60;
                long seconds = (durationMs / 1000) % 60;
                return String.format("%d:%02d", minutes, seconds); // Format as "minutes:seconds"
            }
        } else {
            return "No media loaded";
        }
    }

    // Check if the player is actively playing
    public boolean isPlaying() {
        return exoPlayer.isPlaying();
    }

    public void continuePlaying(){
        exoPlayer.play();
    }
    // Prepare and play the song, or resume if already loaded
    public void playSong(String url) {
        if (exoPlayer.isPlaying()) {
            // If already playing, do nothing or continue

            return;
        }

        if (!isMediaLoaded) {
            // Only load the media if it hasn't been loaded yet
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare(); // Prepare the player (loads and buffers the media)

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {  // Media is ready to play
                        isMediaLoaded = true; // Update loaded state
                        if (durationListener != null) {
                            String duration = getSongDuration();
                            durationListener.onDurationReady(duration);
                        }
                    }
                }
            });
            isMediaLoaded = true;
        }

        exoPlayer.play(); // Start or resume the playback
    }




    // Pause the song
    public void pauseSong() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();

        }
    }

    // Reset the song to the beginning
    public void resetSong() {
        if (exoPlayer != null) {
            exoPlayer.seekTo(0); // Seek to the beginning of the media
        }
    }

    // Stop the song and reset the media state
    public void stopSong() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            isMediaLoaded = false; // Mark that the media has been unloaded

        }
    }

    // Release ExoPlayer resources when done
    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            isMediaLoaded = false; // Reset the media loaded state when releasing resources
        }
    }
}
