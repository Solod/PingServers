package com.solodilov.evgen.pingservers;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SettingsFragment extends PreferenceFragment {
    public static final String COUNT_PACKET = "count";
    public static final String INTERVAL_REQUES = "interval";
    public static final String PACKET_SIZE = "packetsize";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(Color.WHITE);
        }
        return view;
    }
}
