package ict.ihu.gr.loopify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ict.ihu.gr.loopify.R;

public class MusicPlayerService extends Service {

    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Create notification channel for Android 8.0+
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if ("PLAY".equals(action)) {
            playMusic(); // Your play logic here
            showNotification("Playing");
        } else if ("PAUSE".equals(action)) {
            pauseMusic(); // Your pause logic here
            showNotification("Paused");
        } else if ("STOP".equals(action)) {
            stopMusic(); // Your stop logic here
            stopSelf();
        }

        return START_NOT_STICKY; // Adjust to your needs
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't need binding for this service
    }

    // Helper method to create and show notification
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    // Placeholder methods for music control
    private void playMusic() {
        // Your existing play logic (call MediaPlayerManager or ExoPlayerManager)
    }

    private void pauseMusic() {
        // Your existing pause logic (call MediaPlayerManager or ExoPlayerManager)
    }

    private void stopMusic() {
        // Your existing stop logic (call MediaPlayerManager or ExoPlayerManager)
    }
}
