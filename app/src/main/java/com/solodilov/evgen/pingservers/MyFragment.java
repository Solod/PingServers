package com.solodilov.evgen.pingservers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyFragment extends Fragment {
    private static final String LOG_ = MyFragment.class.getCanonicalName();
    @BindView(R.id.et_enter_ip)
    EditText mEnterIP;
    @BindView(R.id.status_service)
    TextView mStatusServer;
    @BindView(R.id.tv_ping_log)
    TextView mPingLog;
    @BindView(R.id.start_or_stop_service)
    Button mBtn;

    OnStartMyService mOnStartMyService;

    public MyFragment() {
    }

    public static MyFragment newInstance() {
        return new MyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);
        ButterKnife.bind(this, rootView);
        if (mOnStartMyService != null) {
            if (mOnStartMyService.onIsService()) {
                mBtn.setText("Stop");
            }
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnStartMyService = (OnStartMyService) context;
    }

    @Override
    public void onDetach() {
        mOnStartMyService = null;
        super.onDetach();
    }

    @OnClick(R.id.start_or_stop_service)
    void onClick(View v) {
        Button button = (Button) v;
        String textButton = (String) button.getText();
        switch (textButton) {
            case "Start":
                button.setText("Stop");
                mOnStartMyService.onStartService(String.valueOf(mEnterIP.getText()));
                break;
            case "Stop":
                button.setText("Start");
                mOnStartMyService.onStopService();
                break;
            default:
        }
    }

    interface OnStartMyService {
        void onStartService(String command);

        void onStopService();

        boolean onIsService();
    }
}
