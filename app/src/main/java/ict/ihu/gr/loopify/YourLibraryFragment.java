package ict.ihu.gr.loopify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link YourLibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YourLibraryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public YourLibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment YourLibraryFragment.
     */
    public static YourLibraryFragment newInstance(String param1, String param2) {
        YourLibraryFragment fragment = new YourLibraryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_your_library, container, false);

        // Load the saved theme
        SharedPreferences preferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String theme = preferences.getString("app_theme", "default");

        // Set the background based on the theme
        int backgroundResource;
        switch (theme) {
            case "aquamarine":
                backgroundResource = R.drawable.background_aquamarine;
                break;
            case "beige":
                backgroundResource = R.drawable.background_beige;
                break;
            case "gold":
                backgroundResource = R.drawable.background_gold;
                break;
            case "ink":
                backgroundResource = R.drawable.background_ink;
                break;
            default:
                backgroundResource = R.drawable.background;
        }

        // Set the background resource to the root view
        root.setBackgroundResource(backgroundResource);

        return root;
    }
}
