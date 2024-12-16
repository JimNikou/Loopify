package ict.ihu.gr.loopify;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {
    public ExoPlayerManager exoPlayerManager;
    private boolean isPlaying = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isTrackServing = false;
    private String currentTrack = ""; // To keep track of the currently requested track
    public ImageButton playPauseButton;

    // Variables to store metadata until both duration and track info are known
    private long cachedDuration = -1;
    private String cachedTitle = null;
    private String cachedArtist = null;
    private boolean metadataNotified = false; // Ensure we only notify once after we have metadata

    // Handler and Runnable to update track position periodically
    private Handler positionHandler = new Handler(Looper.getMainLooper());
    private Runnable positionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (exoPlayerManager != null && isPlaying) {
                long currentPosition = exoPlayerManager.getCurrentPosition();
                notifyPlaybackPosition(currentPosition);
                // Also update the notification with current playback position
                if (metadataNotified && cachedTitle != null && cachedArtist != null && cachedDuration > 0) {
                    updateNotification(cachedTitle, cachedArtist, cachedDuration, currentPosition, isPlaying);
                }
            }
            positionHandler.postDelayed(this, 1000); // Update every 1 second
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        exoPlayerManager = new ExoPlayerManager(this);

        // Set a DurationListener so we know when the player is ready and duration is available
        exoPlayerManager.setDurationListener(durationStr -> {
            long durationMs = exoPlayerManager.getSongDuration();
            cachedDuration = durationMs;
            tryNotifyMetadata();
        });
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        switch (action) {
            case "PLAY":
                String trackName = intent.getStringExtra("TRACK_NAME");
                if (trackName == null || trackName.isEmpty()) {
                    if (currentTrack != null && !currentTrack.isEmpty()) {
                        trackName = currentTrack;
                    } else {
                        Log.e("MediaPlayerService", "TRACK_NAME extra is missing and currentTrack is empty. Cannot play.");
                        break;
                    }
                }
                Log.d("MediaPlayerService", "PLAY action received for track: " + trackName);

                if (!trackName.equals(currentTrack)) {
                    currentTrack = trackName;
                    stopCurrentTrack();
                    runStartTrackServe(currentTrack, exoPlayerManager);
                    Intent broadcastIntent = new Intent("SHOW_MEDIA_PLAYER");
                    sendBroadcast(broadcastIntent);
                } else {
                    exoPlayerManager.continuePlaying();
                    Log.d("MediaPlayerService", "Resumed playing: " + currentTrack);
                }
                isPlaying = true;
                notifyCurrentTrack();
                // If we already have metadata, update notification now
                if (metadataNotified && cachedTitle != null && cachedArtist != null && cachedDuration > 0) {
                    updateNotification(cachedTitle, cachedArtist, cachedDuration, exoPlayerManager.getCurrentPosition(), isPlaying);
                }
                break;

            case "PAUSE":
                exoPlayerManager.pauseSong();
                isPlaying = false;
                Log.d("MediaPlayerService", "Playback paused");
                notifyCurrentTrack();
                // Update notification to reflect paused state
                if (metadataNotified && cachedTitle != null && cachedArtist != null && cachedDuration > 0) {
                    updateNotification(cachedTitle, cachedArtist, cachedDuration, exoPlayerManager.getCurrentPosition(), isPlaying);
                }
                break;

            case "SEEK":
                int newPosition = intent.getIntExtra("NEW_POSITION", 0);
                exoPlayerManager.seekTo(newPosition);
                break;

            case "STOP":
                Log.d("MediaPlayerService", "Stopping service");
                stopSelf();
                notifyCurrentTrack();
                return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    private void notifyCurrentTrack() {
        Intent trackInfoIntent = new Intent("CURRENT_TRACK_INFO");
        trackInfoIntent.putExtra("CURRENT_TRACK_NAME", currentTrack);
        trackInfoIntent.putExtra("IS_PLAYING", isPlaying);
        sendBroadcast(trackInfoIntent);
    }

    private void notifyMetadata(long duration, String title, String artist) {
        Intent metadataIntent = new Intent("CURRENT_TRACK_METADATA");
        metadataIntent.putExtra("DURATION", duration);
        metadataIntent.putExtra("TITLE", title);
        metadataIntent.putExtra("ARTIST", artist);
        sendBroadcast(metadataIntent);

        // Also start/refresh the notification as soon as we have metadata
        updateNotification(title, artist, duration, exoPlayerManager.getCurrentPosition(), isPlaying);
        // Start foreground now that we have a meaningful notification
        // Make sure you have created the notification channel "MEDIA_CHANNEL_ID" beforehand
        startForeground(1, createNotification(title, artist, duration, exoPlayerManager.getCurrentPosition(), isPlaying));
    }

    private void notifyPlaybackPosition(long position) {
        Intent positionIntent = new Intent("CURRENT_TRACK_POSITION");
        positionIntent.putExtra("POSITION", position);
        sendBroadcast(positionIntent);
    }

    private void stopCurrentTrack() {
        if (exoPlayerManager != null && isPlaying) {
            exoPlayerManager.stopSong();
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
                    isTrackServing = false;
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

        String trackName = currentTrack;

        mainHandler.post(() -> {
            exo.stopSong();
            // Reset cached metadata variables and flag each time we start a new track
            cachedDuration = -1;
            cachedTitle = null;
            cachedArtist = null;
            metadataNotified = false;

            exo.playSong(completeUrl); // start playback
            isPlaying = true;
            isTrackServing = false;

            ApiManager apiManager = new ApiManager();

            // First, get the artist from the track name
            apiManager.fetchArtistFromTrack(trackName, artistJsonResponse -> {
                if (artistJsonResponse != null && !artistJsonResponse.isEmpty()) {
                    final String derivedArtistName = artistJsonResponse.trim(); // Make it final

                    // Now fetch detailed track info
                    apiManager.fetchTrackInfo(trackName, derivedArtistName, trackInfoJson -> {
                        if (metadataNotified) return;

                        String finalArtist = derivedArtistName;
                        if (trackInfoJson != null && trackInfoJson.contains("\"track\"")) {
                            try {
                                org.json.JSONObject jsonObject = new org.json.JSONObject(trackInfoJson);
                                org.json.JSONObject trackObj = jsonObject.getJSONObject("track");
                                String artistNameFromJson = trackObj.getJSONObject("artist").getString("name");

                                if (!isArtistNameValid(artistNameFromJson)) {
                                    artistNameFromJson = derivedArtistName;
                                }
                                finalArtist = artistNameFromJson;
                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                // fallback if parsing fails
                                if (!isArtistNameValid(derivedArtistName)) {
                                    finalArtist = "Unknown Artist";
                                } else {
                                    finalArtist = derivedArtistName;
                                }
                            }
                        } else {
                            if (!isArtistNameValid(derivedArtistName)) {
                                finalArtist = "Unknown Artist";
                            }
                        }

                        cachedTitle = trackName;
                        cachedArtist = finalArtist;
                        tryNotifyMetadata();
                    });
                } else {
                    cachedTitle = trackName;
                    cachedArtist = "Unknown Artist";
                    tryNotifyMetadata();
                }
            });

            startPositionUpdates();
        });
    }

    private boolean isArtistNameValid(String artistName) {
        if (artistName == null) return false;
        String lower = artistName.toLowerCase();
        return !(lower.contains("http") || lower.contains("{\"message\""));
    }

    private void tryNotifyMetadata() {
        if (metadataNotified) return; // Already notified once
        if (cachedDuration > 0 && cachedTitle != null && cachedArtist != null) {
            notifyMetadata(cachedDuration, cachedTitle, cachedArtist);
            metadataNotified = true;
        }
    }

    private void startPositionUpdates() {
        positionHandler.post(positionUpdateRunnable);
    }

    private RemoteViews createNotificationViews(String title, String artist, long duration, long currentPosition, boolean isPlaying) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.custom_notification_media_player);

        views.setTextViewText(R.id.notif_track_title, title);
        views.setTextViewText(R.id.notif_artist_name, artist);
        views.setTextViewText(R.id.notif_current_time, formatTime(currentPosition));
        views.setTextViewText(R.id.notif_total_duration, formatTime(duration));

        int max = (int)(duration/1000);
        int progress = (int)(currentPosition/1000);

        // Update progress bar
        views.setInt(R.id.notif_progress_bar, "setMax", max);
        views.setInt(R.id.notif_progress_bar, "setProgress", progress);

        // Play/Pause icon
        views.setImageViewResource(R.id.notif_play_pause_button,
                isPlaying ? R.drawable.ic_fullscreen_media_player_pause_button
                        : R.drawable.ic_fullscreen_media_player_play_button);

        // PendingIntents for buttons
        PendingIntent playPauseIntent = createPendingIntent(isPlaying ? "PAUSE" : "PLAY");
        views.setOnClickPendingIntent(R.id.notif_play_pause_button, playPauseIntent);

        PendingIntent nextIntent = createPendingIntent("NEXT");
        views.setOnClickPendingIntent(R.id.notif_next_button, nextIntent);

        PendingIntent previousIntent = createPendingIntent("PREVIOUS");
        views.setOnClickPendingIntent(R.id.notif_previous_button, previousIntent);

        return views;
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private Notification createNotification(String title, String artist, long duration, long currentPosition, boolean isPlaying) {
        RemoteViews views = createNotificationViews(title, artist, duration, currentPosition, isPlaying);

        return new NotificationCompat.Builder(this, "MEDIA_CHANNEL_ID")
                .setSmallIcon(R.drawable.music_note)
                .setCustomContentView(views)
                .setCustomBigContentView(views) // expanded look same as collapsed
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String title, String artist, long duration, long currentPosition, boolean isPlaying) {
        Notification notification = createNotification(title, artist, duration, currentPosition, isPlaying);
        startForeground(1, notification);
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
        positionHandler.removeCallbacks(positionUpdateRunnable);
    }
}
