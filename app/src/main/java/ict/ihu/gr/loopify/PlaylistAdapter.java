package ict.ihu.gr.loopify.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import ict.ihu.gr.loopify.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final String[] songNames;
    private final String[] artistNames;
    private final String[] imageUrls; // For image URLs
    private final OnItemClickListener listener; // Listener for item clicks

    public interface OnItemClickListener {
        void onItemClick(String songName, String artistName);
    }

    public PlaylistAdapter(String[] songNames, String[] artistNames, String[] imageUrls, OnItemClickListener listener) {
        this.songNames = songNames;
        this.artistNames = artistNames;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.songNameTextView.setText(songNames[position]);
        holder.artistNameTextView.setText(artistNames[position]);

        // Use Glide to load the image from URL into the ImageView
        Glide.with(holder.songImageView.getContext())
                .load(imageUrls[position])
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(holder.songImageView);

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(songNames[position], artistNames[position]); // Notify listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return songNames.length;
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public final TextView songNameTextView;
        public final TextView artistNameTextView;
        public final ImageView songImageView;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.song_name);
            artistNameTextView = itemView.findViewById(R.id.artist_name);
            songImageView = itemView.findViewById(R.id.song_image);
        }
    }
}
