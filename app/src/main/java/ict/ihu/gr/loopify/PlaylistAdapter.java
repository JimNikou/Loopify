package ict.ihu.gr.loopify;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import ict.ihu.gr.loopify.ApiManager;
import ict.ihu.gr.loopify.ArtistInfoActivity;
import ict.ihu.gr.loopify.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final String[] songNames;
    private final String[] artistNames;
    private final String[] imageUrls;
    private final OnItemClickListener listener;

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

        ApiManager apiManager = new ApiManager();

        Glide.with(holder.songImageView.getContext())
                .load(imageUrls[position])
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(holder.songImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Use holder.getAdapterPosition() instead of position
                listener.onItemClick(songNames[holder.getAdapterPosition()], artistNames[holder.getAdapterPosition()]);
            }
        });

        // Set up click listener for the song options (⋮)
        holder.songOptionsTextView.setOnClickListener(v -> {
            // Use holder.getAdapterPosition() to get the correct position
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                apiManager.fetchArtistInfo(artistNames[adapterPosition], new ApiManager.ApiResponseListener() {
                    @Override
                    public void onResponseReceived(String artistJsonResponse) {
                        if (artistJsonResponse != null) {
                            apiManager.fetchTrackInfo(songNames[adapterPosition], artistNames[adapterPosition], new ApiManager.ApiResponseListener() {
                                @Override
                                public void onResponseReceived(String trackJsonResponse) {
                                    if (trackJsonResponse != null) {
                                        Intent intent = new Intent(v.getContext(), ArtistInfoActivity.class);
                                        intent.putExtra("artistJson", artistJsonResponse);
                                        intent.putExtra("trackJson", trackJsonResponse);
                                        v.getContext().startActivity(intent);
                                    } else {
                                        Log.e("ApiError", "Track response is null");
                                        Toast.makeText(v.getContext(), "Failed to fetch track info", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Log.e("ApiError", "Artist response is null");
                            Toast.makeText(v.getContext(), "Failed to fetch artist info", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        public final TextView songOptionsTextView; // Added field for song options (⋮)

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.song_name);
            artistNameTextView = itemView.findViewById(R.id.artist_name);
            songImageView = itemView.findViewById(R.id.song_image);
            songOptionsTextView = itemView.findViewById(R.id.song_options); // Initialize the options view
        }
    }
}
