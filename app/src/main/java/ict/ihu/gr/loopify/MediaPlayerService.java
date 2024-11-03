package ict.ihu.gr.loopify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {

    public ExoPlayerManager exoPlayerManager;
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
                    String trackName = "black and yellow";
                    new MainActivity().runStartTrackServe(trackName, exoPlayerManager);
                    // me thn apo panw sunarthsh katebainei to tragoudi kai meta prepei v
                    // na brw to onoma tou mesa sto bucket dioti den einai to idio me auto pou epsaxe o user √
                    // epeita prepei afou brw to onoma kai exei teleiwsei olh auth h diadikasia, tote kai mono tote na √
                    // xekinhsei na paizei to tragoudi. Prepei epishs na ginetai elenxos prin to download ean to tragoudi --
                    // uparxei hdh kai na skiparei ta alla steps kai na erxetai na paixei miafora me to link tou tragoudiou
//                    exoPlayerManager.playSong("http://loopify.ddnsgeek.com:20080/downloads/P.I.M.P..webm");
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
                .setContentTitle("(song tittle)")
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
