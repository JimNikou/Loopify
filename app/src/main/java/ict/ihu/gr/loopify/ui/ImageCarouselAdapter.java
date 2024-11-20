// ImageCarouselAdapter.java
package ict.ihu.gr.loopify.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ict.ihu.gr.loopify.R;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {

    private final int[] imageResources;
    private final String[] artistNames;
    private final OnImageClickListener clickListener;

    public interface OnImageClickListener {
        void onImageClick(int position, int imageResource);
    }

    public ImageCarouselAdapter(int[] imageResources, String[] artistNames, OnImageClickListener clickListener) {
        this.imageResources = imageResources;
        this.artistNames = artistNames;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(imageResources[position]);
        holder.artistNameTextView.setText(artistNames[position]);

        holder.imageView.setOnClickListener(v -> clickListener.onImageClick(position, imageResources[position]));
    }


    @Override
    public int getItemCount() {
        return imageResources.length;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView artistNameTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            artistNameTextView = itemView.findViewById(R.id.artist_name_text_view);
        }
    }
}
