package ict.ihu.gr.loopify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ict.ihu.gr.loopify.R;

public class ImageCarouselFragment extends Fragment {

    // RecyclerView object to display the carousel of images
    private RecyclerView recyclerView;

    // Array of images that are going to be in the carousel
    // The images are located at 'res/drawable' folder
    private int[] imageResources = {
            R.drawable.hiphop_photo,
            R.drawable.house_photo,
            R.drawable.pop_photo,
            R.drawable.rap_photo,
            R.drawable.rock_photo,
            R.drawable.techno_photo
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View root = inflater.inflate(R.layout.fragment_image_carousel, container, false);

        // Initialize the RecyclerView from the inflated layout
        recyclerView = root.findViewById(R.id.recycler_view);

        // Set the RecyclerView to display items in a horizontal list
        // LinearLayoutManager organizes the items in a row (we set it horizontal)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Create an instance of the custom adapter (ImageCarouselAdapter)
        // and pass the array of image resources to it
        ict.ihu.gr.loopify.ui.ImageCarouselAdapter adapter = new ict.ihu.gr.loopify.ui.ImageCarouselAdapter(imageResources);

        // Attach the adapter to the RecyclerView to display the images
        recyclerView.setAdapter(adapter);

        // Return the root view that contains the RecyclerView
        return root;
    }
}
