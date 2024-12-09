package ict.ihu.gr.loopify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {
    public ExoPlayerManager exoPlayerManager;
    private boolean isPlaying = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isTrackServing = false;
    private String currentTrack = ""; // To keep track of the currently requested track
    public ImageButton playPauseButton;

    @Override
    public void onCreate() {
        super.onCreate();
        exoPlayerManager = new ExoPlayerManager(this);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case "PLAY":
                    String trackName = SearchFragment.getInstance().getData();
                    // Check if a different song is requested to avoid re-triggering the same one
                    if (!trackName.equals(currentTrack)) {
                        currentTrack = trackName;
                        stopCurrentTrack();  // Stop any currently playing track
                        runStartTrackServe(currentTrack, exoPlayerManager);
                        // Broadcast an intent to show the MediaPlayer fragment
                        Intent broadcastIntent = new Intent("SHOW_MEDIA_PLAYER");
                        sendBroadcast(broadcastIntent);
                    }else{
                        exoPlayerManager.continuePlaying();
                    }
                    isPlaying = true;
                    updateNotification();
                    break;
                case "PAUSE":
                    exoPlayerManager.pauseSong();
                    isPlaying = false;
                    updateNotification();
                    break;
                case "STOP":
                    stopSelf();
                    return START_NOT_STICKY;
            }
        }

        startForeground(1, getNotification());
        return START_STICKY;
    }

    private void stopCurrentTrack() {
        if (exoPlayerManager != null && isPlaying) {
            exoPlayerManager.stopSong(); // Ensure current song stops
            isPlaying = false;
        }
    }

    void runStartTrackServe(String track, ExoPlayerManager exo) {
        if (isTrackServing) {
            Log.d("ApiManager", "Track is already being served, ignoring further calls.");
            return;
        }

        isTrackServing = true;
        ApiManager apiManager = new ApiManager();
        checkTrackAvailability(track, apiManager, exo);
    }

    private void checkTrackAvailability(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
            if (response != null) {
                String matchedTitle = apiManager.findMatchingSong(track, response);
                if (matchedTitle != null) {
                    playTrack(matchedTitle, exo);
                } else {
                    startTrackDownload(track, apiManager, exo);
                }
            } else {
                Log.d("ApiManager", "Failed to fetch song titles.");
                isTrackServing = false;
            }
        });
    }

    private void startTrackDownload(String track, ApiManager apiManager, ExoPlayerManager exo) {
        apiManager.startTrackServe(track, jsonResponse -> {
            if (jsonResponse != null) {
                apiManager.fetchSongTitlesFromTxt("http://loopify.ddnsgeek.com:20080/downloads/downloaded_files.txt", response -> {
                    if (response != null) {
                        String matchedTitle = apiManager.findMatchingSong(track, response);
                        if (matchedTitle != null) {
                            playTrack(matchedTitle, exo);
                        } else {
                            Log.d("ApiManager", "Failed to locate the downloaded song.");
                        }
                    } else {
                        Log.d("ApiManager", "Failed to fetch song titles after download.");
                    }
                    isTrackServing = false; // Ensure flag is reset after completion
                });
            } else {
                Log.d("ApiManager", "Download response was null.");
                isTrackServing = false;
            }
        });
    }

    private void playTrack(String matchedTitle, ExoPlayerManager exo) {
        String completeUrl = "http://loopify.ddnsgeek.com:20080/downloads/" + matchedTitle.trim() + ".mp3";
        Log.d("ApiManager", "Playing track: " + completeUrl);

        mainHandler.post(() -> {
            exo.stopSong();  // Stop any currently playing song
            exo.playSong(completeUrl);  // Play the new song
            isPlaying = true;
            isTrackServing = false; // Reset isTrackServing when playback starts
        });
    }

    private void updateNotification() {
        Notification notification = getNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private Notification getNotification() {
        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction("PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action action;
        if (isPlaying) {
            action = new NotificationCompat.Action(R.drawable.pause_svgrepo_com, "Pause", pausePendingIntent);
        } else {
            action = new NotificationCompat.Action(R.drawable.play_svgrepo_com, "Play", playPendingIntent);
        }

        return new NotificationCompat.Builder(this, "MEDIA_CHANNEL_ID")
                .setSmallIcon(R.drawable.music_note)
                .setContentTitle(currentTrack)
                .setContentText(isPlaying ? "Playing music..." : "Music paused")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                .addAction(action)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayerManager != null) {
            exoPlayerManager.release();
        }
    }
}
