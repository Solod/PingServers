package com.solodilov.evgen.pingservers;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MyFragment extends Fragment {
    private static final String STATE_LOG = "log";
    @BindView(R.id.et_enter_ip)
    EditText mEnterIP;
    @BindView(R.id.tv_ping_log)
    TextView mPingLog;
    @BindView(R.id.start_or_stop_service)
    Button mBtn;
    @BindView(R.id.chb_service)
    CheckBox mChBox;
    @BindView(R.id.btn_clean_text)
    ImageView mImageView;
    @BindView(R.id.scroll)
    ScrollView mScrollView;

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
        Log.d(MyFragment.class.getCanonicalName(), mOnStartMyService == null ? "null" : mOnStartMyService.toString());
        if (mOnStartMyService != null) {
            mOnStartMyService.onStopService();
            if (mChBox.isChecked()) {
                mOnStartMyService.onStartService(mEnterIP.getText().toString(), true);
            }
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            appendTextAndScroll(savedInstanceState.getString(STATE_LOG, ""));
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
            if (mOnStartMyService != null) {
                mOnStartMyService.onStartService(strIp, false);
            }
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
        String textButton = String.valueOf(button.getText());
        switch (textButton) {
            case "Start":
                String address = String.valueOf(mEnterIP.getText());
                mOnStartMyService.onStartService(address, true);
                setTextButton(getString(R.string.text_button_stop));
                break;
            case "Stop":
                mOnStartMyService.onStopService();
                setTextButton(getString(R.string.text_button_start));
                break;
            default:
        }
    }


    @OnClick(R.id.btn_clean_text)
    void onClick() {
        mEnterIP.setText("");
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
                mEnterIP.setError(getString(R.string.error_text_empty_string));
                mChBox.setChecked(false);
            }
        }
    }


    @OnTextChanged(R.id.et_enter_ip)
    void textChanged() {
        String address = mEnterIP.getText().toString().trim();
        if (address.length() > 0) {
            mImageView.setVisibility(View.VISIBLE);
            if (validIp(address)) {
                mBtn.setEnabled(true);
            } else {
                mBtn.setEnabled(false);
            }
        } else {
            mImageView.setVisibility(View.GONE);
            mBtn.setEnabled(false);
        }
    }

    public void appendTextAndScroll(String text) {
        if (mPingLog != null) {
            mPingLog.append(text + "\n");
            final Layout layout = mPingLog.getLayout();
            if (layout != null) {
                int scrollDelta = layout.getLineBottom(mPingLog.getLineCount() - 1)
                        - mPingLog.getScrollY() - mPingLog.getHeight();
                if (scrollDelta > 0)
                    mPingLog.scrollBy(0, scrollDelta);
            }
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private boolean validIp(String address) {
        return address.matches(getString(R.string.reg_exp_ip)) || address.matches(getString(R.string.reg_exp_dns));
    }

    interface OnStartMyService {
        void onStartService(String command, boolean taskServiceFragment);

        void onStopService();
    }
}
