package ict.ihu.gr.loopify;

import java.util.List;

public class TrackSearchResponse {
    public Results results;

    public static class Results {
        public TrackMatches trackmatches;

        public static class TrackMatches {
            public List<Track> track;

            public static class Track {
                public String name;
                public String artist;
                public String url;
            }
        }
    }
}
