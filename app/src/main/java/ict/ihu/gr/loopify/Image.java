package ict.ihu.gr.loopify;

// Class representing the image object
public class Image {
    private String text;  // Image URL (map the #text field from JSON)

    // Getter for the image URL
    public String getText() {
        return text;
    }

    // Setter for the image URL (needed for deserialization)
    public void setText(String text) {
        this.text = text;
    }
}
