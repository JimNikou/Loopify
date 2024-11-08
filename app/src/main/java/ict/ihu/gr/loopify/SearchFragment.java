package ict.ihu.gr.loopify;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import ict.ihu.gr.loopify.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    private static SearchFragment instance;
    private String data;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String trackName;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }
    public static SearchFragment getInstance() {
        if (instance == null) {
            instance = new SearchFragment();
        }
        return instance;
    }

    public String getData() {
        Log.d("ApiManager", "Data: " + data);
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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

    private Button testButton;
    public EditText searchBar;
    private FragmentHomeBinding binding;
    private Context context;
    private MainActivity mainActivity;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must be an instance of MainActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null; // Avoid memory leaks
    }

    // Call this method when you want to start the music service
    public void callStartMusicService(String action) {
        if (mainActivity != null) {
            mainActivity.startMusicService(action); // Access startMusicService directly
        } else {
            Log.e("SearchFragment", "MainActivity is not attached");
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Set up the back button
        searchBar = view.findViewById(R.id.searchBar);
        testButton = view.findViewById(R.id.testbutton);
        MainActivity mainActivity = new MainActivity();
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment.getInstance().setData(searchBar.getText().toString());
                callStartMusicService("PLAY"); // otan patithei to play me ena allo tragoudi enw paizei hdh, den allazei tragoudi.
            }
        });

        return view;
    }
}