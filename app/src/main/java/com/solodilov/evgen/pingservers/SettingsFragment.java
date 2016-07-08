package com.solodilov.evgen.pingservers;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class SettingsFragment extends PreferenceFragment {
    public static final String COUNT_PACKET = "count";
    public static final String INTERVAL_REQUES = "interval";
    public static final String PACKET_SIZE = "packetsize";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }

}
