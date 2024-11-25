package ict.ihu.gr.loopify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ict.ihu.gr.loopify.ui.settings.SettingsFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationSettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NotificationSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationSettingsFragment newInstance(String param1, String param2) {
        NotificationSettingsFragment fragment = new NotificationSettingsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        // Set up the back button
        Button backButton = view.findViewById(R.id.back_button_notif);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop this fragment off the back stack to go back to SettingsFragment
                getParentFragmentManager().popBackStack();

                // Re-show the NavigationView by accessing the parent SettingsFragment
                SettingsFragment settingsFragment = (SettingsFragment) getParentFragment();
                if (settingsFragment != null) {
                    settingsFragment.showNavigationView();  // Show the NavigationView
                }
            }
        });

        return view;
    }
}