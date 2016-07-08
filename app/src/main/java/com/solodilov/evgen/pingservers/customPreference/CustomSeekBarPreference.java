package com.solodilov.evgen.pingservers.customPreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.solodilov.evgen.pingservers.R;


public class CustomSeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private TextView mValueTextView;
    private int mMax;
    private int mCurrentValue;

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomSeekBarPreference);
        mMax = a.getInt(R.styleable.CustomSeekBarPreference_max_seek, 105);
        a.recycle();
        setLayoutResource(R.layout.seek_bar_preference_layout);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ((TextView) view.findViewById(R.id.title)).setText(getTitle());
        ((TextView) view.findViewById(R.id.summary)).setText(getSummary());
        mValueTextView = (TextView) view.findViewById(R.id.seek_value);
        mValueTextView.setText(String.valueOf(mCurrentValue));
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(mMax);
        seekBar.setProgress(mCurrentValue);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(mCurrentValue) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mValueTextView.setText(String.valueOf(i));
        mValueTextView.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mCurrentValue = seekBar.getProgress();
        setValue(mCurrentValue);

    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mCurrentValue) {
            mCurrentValue = value;
            notifyChanged();
        }
    }
}
