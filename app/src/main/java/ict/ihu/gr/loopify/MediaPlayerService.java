package ict.ihu.gr.loopify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {

    private ExoPlayerManager exoPlayerManager;
    private boolean isPlaying = false; // Track the state of playback

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize ExoPlayerManager
        exoPlayerManager = new ExoPlayerManager(this);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case "PLAY":
                    String trackName = "Hooverphonic - Mad About You (Live at Koningin Elisabethzaal 2012).mp3";
                    exoPlayerManager.playSong("https://firebasestorage.googleapis.com/v0/b/loopify-ebe8e.appspot.com/o/"+trackName+"?alt=media");
                    isPlaying = true;
                    updateNotification();
                    break;
                case "PAUSE":
                    exoPlayerManager.pauseSong();
                    isPlaying = false;
                    updateNotification();
                    break;
                case "STOP":
                    stopSelf(); // Stop the service
                    return START_NOT_STICKY;
            }
        }

        // Ensure that the notification is shown as part of the foreground service
        startForeground(1, getNotification()); // Display the notification

        return START_STICKY;  // Keep the service running
    }



    private void updateNotification() {
        Notification notification = getNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification); // Update the existing notification
    }

    // Build the notification with dynamic Play/Pause actions
    private Notification getNotification() {
        // Play action
        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction("PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Pause action
        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Choose which action (play or pause) to show based on the isPlaying flag
        NotificationCompat.Action action;
        if (isPlaying) {
            // Show Pause button if music is playing
            action = new NotificationCompat.Action(
                    R.drawable.pause_svgrepo_com, "Pause", pausePendingIntent);
        } else {
            // Show Play button if music is paused
            action = new NotificationCompat.Action(
                    R.drawable.play_svgrepo_com, "Play", playPendingIntent);
        }

        // Build the notification
        return new NotificationCompat.Builder(this, "MEDIA_CHANNEL_ID")
                .setSmallIcon(R.drawable.music_note) // Icon for the notification
                .setContentTitle("My Media Player")
                .setContentText(isPlaying ? "Playing music..." : "Music paused")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)) // Show one action in compact view
                .addAction(action) // Add either Play or Pause action
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not binding to any activities
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerManager != null) {
            exoPlayerManager.release(); // Release ExoPlayer when the service is destroyed
        }
    }
}
