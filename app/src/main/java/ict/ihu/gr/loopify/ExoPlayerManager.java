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
                    if (durationListener != null) {
                        durationListener.onDurationReady(formatDuration(storedDuration));
                    }
                }
            }
        });
    }

    // Define a listener interface to be notified when duration is ready
    public interface DurationListener {
        void onDurationReady(String duration);
    }
    public void seekTo(long positionMs) {
        if (exoPlayer != null && isMediaLoaded && positionMs >= 0) {
            exoPlayer.seekTo(positionMs);
        }
    }

    public void setDurationListener(DurationListener listener) {
        this.durationListener = listener;
    }

    public String getStoredDuration() {
        return formatDuration(storedDuration);
    }

    // Convert a duration in milliseconds to a "m:ss" format string
    private String formatDuration(long durationMs) {
        if (durationMs == C.TIME_UNSET || durationMs < 0) {
            return "Duration not available";
        } else {
            long minutes = (durationMs / 1000) / 60;
            long seconds = (durationMs / 1000) % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Method to get the duration of the currently loaded song in milliseconds.
    // Returns -1 if no media is loaded or duration is unset.
    public long getSongDuration() {
        if (isMediaLoaded && exoPlayer != null) {
            long durationMs = exoPlayer.getDuration();
            if (durationMs == C.TIME_UNSET) {
                return -1; // Duration not available
            } else {
                return durationMs;
            }
        } else {
            return -1; // No media loaded
        }
    }

    // Check if the player is actively playing
    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    public void continuePlaying() {
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    public long getCurrentPosition() {
        if (exoPlayer != null) {
            return exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void playSong(String url) {
        if (exoPlayer == null) return;

        if (!isMediaLoaded) {
            MediaItem mediaItem = MediaItem.fromUri(url);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            // Do not add another listener here; rely on the one added in the constructor
            // isMediaLoaded will be set to true once STATE_READY is reached by the existing listener
        }

        exoPlayer.play();
    }

    // Pause the song
    public void pauseSong() {
        if (exoPlayer != null && exoPlayer.isPlaying()) {
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
