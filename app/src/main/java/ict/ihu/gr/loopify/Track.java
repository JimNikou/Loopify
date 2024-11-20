package ict.ihu.gr.loopify;

import java.util.List;
import ict.ihu.gr.loopify.Artist;

// Class representing the track information you need
public class Track {
    private String name;  // Track name
    private ict.ihu.gr.loopify.Artist artist;  // Artist object
    private List<ict.ihu.gr.loopify.Image> image;  // List of image objects

    // Getters
    public String getName() {
        return name;
    }

    public Artist getArtist() {
        return artist;
    }

    public List<Image> getImage() {
        return image;
    }
}

