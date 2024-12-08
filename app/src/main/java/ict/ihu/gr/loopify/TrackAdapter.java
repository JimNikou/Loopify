package ict.ihu.gr.loopify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<Track> tracks;
    private final Context context;
    private final OnTrackClickListener listener; // Listener for item clicks

    // Constructor includes listener
    public TrackAdapter(Context context, List<Track> tracks, OnTrackClickListener listener) {
        this.context = context;
        this.tracks = tracks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.songName.setText(track.getName());
        holder.artistName.setText(track.getArtist().getName());

        // Set a click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d("TrackAdapter", "Item clicked: " + track.getName() + " by " + track.getArtist().getName());
                listener.onTrackClick(track); // Notify listener with the track details
            }
        });

    }


    @Override
    public int getItemCount() {
        return tracks.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        TextView artistName;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.song_name);
            artistName = itemView.findViewById(R.id.artist_name);
        }
    }

    // Interface for click listener
    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }
}
