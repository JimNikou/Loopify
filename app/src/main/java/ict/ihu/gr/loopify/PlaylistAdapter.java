package ict.ihu.gr.loopify.ui;

import android.annotation.SuppressLint;
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
    private final String[] imageUrls;  // Change to String[] for image URLs

    public PlaylistAdapter(String[] songNames, String[] artistNames, String[] imageUrls) {
        this.songNames = songNames;
        this.artistNames = artistNames;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.songNameTextView.setText(songNames[position]);
        holder.artistNameTextView.setText(artistNames[position]);
        ApiManager apiManager = new ApiManager();

        // Use Glide to load the image from URL into the ImageView
        Glide.with(holder.songImageView.getContext())
                .load(imageUrls[position])  // Load image from URL
                .into(holder.songImageView);  // Set image into ImageView

        // Set up click listener for the song options (⋮)
        holder.songOptionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiManager.fetchArtistInfo(artistNames[position], new ApiManager.ApiResponseListener() {
                    @Override
                    public void onResponseReceived(String artistJsonResponse) {
                        if (artistJsonResponse != null) {
                            apiManager.fetchTrackInfo(songNames[position], artistNames[position], new ApiManager.ApiResponseListener() {
                                @Override
                                public void onResponseReceived(String trackJsonResponse) {
                                    Intent intent = new Intent(v.getContext(), ArtistInfoActivity.class);
                                    intent.putExtra("artistJson", artistJsonResponse);
                                    intent.putExtra("trackJson", trackJsonResponse);
                                    v.getContext().startActivity(intent);
                                }
                            });
                        } else {
                            Log.d("mp3song", "No artist found");
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
        public final ImageView songImageView;  // ImageView for song image
        public final TextView songOptionsTextView;  // Add reference to the song options (⋮) TextView

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.song_name);
            artistNameTextView = itemView.findViewById(R.id.artist_name);
            songImageView = itemView.findViewById(R.id.song_image);  // Initialize ImageView correctly
            songOptionsTextView = itemView.findViewById(R.id.song_options);  // Initialize song options TextView
        }
    }
}
