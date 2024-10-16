package ict.ihu.gr.loopify;

import android.content.Context;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

public class ExoPlayerManager {
    private ExoPlayer exoPlayer;
    private boolean isMediaLoaded = false; // To check if the media has been loaded

    // Initialize ExoPlayer with the application context
    public ExoPlayerManager(Context context) {
        exoPlayer = new ExoPlayer.Builder(context).build();
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
