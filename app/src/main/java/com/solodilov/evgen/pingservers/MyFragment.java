package com.solodilov.evgen.pingservers;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MyFragment extends Fragment {
    @BindView(R.id.et_enter_ip)
    EditText mEnterIP;
    @BindView(R.id.status_service)
    TextView mStatusServer;
    @BindView(R.id.tv_ping_log)
    TextView mPingLog;
    @BindView(R.id.start_or_stop_service)
    Button mBtn;
    @BindView(R.id.chb_service)
    CheckBox mChBox;

    private OnStartMyService mOnStartMyService;
    private SharedPreferences mPreferences;

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
            mOnStartMyService.onStopService();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String ip = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(MainActivity.CHACKABLE_SERVICE, "");
        mEnterIP.setText(ip);
        mChBox.setChecked(!ip.equals(""));
    }

    @Override
    public void onStart() {
        super.onStart();
        mOnStartMyService.onStopService();
        if (mChBox.isChecked()) {
            mOnStartMyService.onStartService(mEnterIP.getText().toString(), true);
        }
    }

    @Override
    public void onStop() {
        String strIp = mEnterIP.getText().toString();
        if (mChBox.isChecked()) {
            if (!TextUtils.isEmpty(strIp)) {
                Editor editor = mPreferences.edit();
                editor.putString(MainActivity.CHACKABLE_SERVICE, strIp);
                editor.apply();
            }
            mOnStartMyService.onStartService(strIp, false);
        }
        super.onStop();
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
                mOnStartMyService.onStartService(String.valueOf(mEnterIP.getText()), true);
                setTextButton("Stop");
                break;
            case "Stop":
                mOnStartMyService.onStopService();
                setTextButton("Start");
                break;
            default:
        }
    }

    void setTextButton(String text) {
        mBtn.setText(text);
    }

    @OnCheckedChanged(R.id.chb_service)
    void onChecked(boolean checked) {
        if (!checked) {
            mPreferences.edit().clear().apply();
        } else {
            if (TextUtils.isEmpty(mEnterIP.getText().toString())) {
                mEnterIP.setError("Enter your ip or dns");
                mChBox.setChecked(false);
            }
        }

    }

    interface OnStartMyService {
        void onStartService(String command, boolean taskServiceFragment);

        void onStopService();
    }
}
