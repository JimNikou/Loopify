package ict.ihu.gr.loopify;

import android.media.MediaPlayer;
import android.util.Log;

public class MediaPlayerManager {
    private MediaPlayer mediaPlayer;
    private static final String TAG = "MediaPlayerManager";

    public void playSong(String url) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset(); // Reset the player if it's already initialized
            }
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // Prepare asynchronously
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
            mediaPlayer.setOnCompletionListener(mp -> {
                release();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
        }
    }

    public void stopSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset(); // Reset after stopping
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
