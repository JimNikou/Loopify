package ict.ihu.gr.loopify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class MusicPlayerService extends Service {

    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 1;

    // Single instance of the media player manager
    private ExoPlayerManager exoPlayerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Create notification channel for Android 8.0+
        exoPlayerManager = new ExoPlayerManager(this); // Initialize ExoPlayerManager
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        // Handle different actions (play, pause, stop)
        if ("PLAY".equals(action)) {
            String songUrl = intent.getStringExtra("song_url");
            playMusic(songUrl); // Pass the URL to play
        } else if ("PAUSE".equals(action)) {
            pauseMusic(); // Pause the playback
        } else if ("STOP".equals(action)) {
            stopMusic(); // Stop the playback and the service
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't need binding for this service
    }

    // Play the music or resume it if already loaded
    private void playMusic(String url) {
        exoPlayerManager.playSong(url); // Call the playSong method from ExoPlayerManager
        showNotification("Playing"); // Update the notification
    }

    // Pause the music
    private void pauseMusic() {
        exoPlayerManager.pauseSong(); // Pause the playback in ExoPlayerManager
        showNotification("Paused"); // Update the notification to show paused state
    }

    // Stop the music and release resources
    private void stopMusic() {
        exoPlayerManager.stopSong(); // Stop the playback in ExoPlayerManager
        stopForeground(true); // Remove the notification and stop the service
        stopSelf(); // Stop the service itself
    }

    // Helper method to show notification
    private void showNotification(String status) {
        // PendingIntents for play, pause, stop actions
        PendingIntent playIntent = PendingIntent.getService(
                this, 0, new Intent(this, MusicPlayerService.class).setAction("PLAY"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent pauseIntent = PendingIntent.getService(
                this, 0, new Intent(this, MusicPlayerService.class).setAction("PAUSE"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent stopIntent = PendingIntent.getService(
                this, 0, new Intent(this, MusicPlayerService.class).setAction("STOP"),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(status)
                .setSmallIcon(R.drawable.baseline_music_note_24) // Use a valid drawable icon
                .addAction(R.drawable.baseline_play_arrow_24, "Play", playIntent)
                .addAction(R.drawable.baseline_pause_24, "Pause", pauseIntent)
                .addAction(R.drawable.baseline_stop_24, "Stop", stopIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)) // Show controls in compact view
                .build();

        // Start service as foreground service
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Channel for music player controls");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerManager != null) {
            exoPlayerManager.release(); // Release ExoPlayer resources
            exoPlayerManager = null;
        }
    }
}
