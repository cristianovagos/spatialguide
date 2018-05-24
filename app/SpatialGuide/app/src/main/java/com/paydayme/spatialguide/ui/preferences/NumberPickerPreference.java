package com.paydayme.spatialguide.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.paydayme.spatialguide.R;

import static com.paydayme.spatialguide.core.Constant.MAX_TRIGGER_AREA_VALUE;
import static com.paydayme.spatialguide.core.Constant.MIN_TRIGGER_AREA_VALUE;

public class NumberPickerPreference extends DialogPreference {
    private static final int DEFAULT_VALUE = 0;

    private int mMinValue;
    private int mMaxValue;
    private int mValue;
    private NumberPicker mNumberPicker;

    public NumberPickerPreference(Context context) {
        this(context, null);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get attributes specified in XML
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference, 0, 0);
        try {
            setMinValue(a.getInteger(R.styleable.NumberPickerPreference_min, MIN_TRIGGER_AREA_VALUE));
            setMaxValue(a.getInteger(R.styleable.NumberPickerPreference_android_max, MAX_TRIGGER_AREA_VALUE));
        }
        finally {
            a.recycle();
        }

        // set layout
        setDialogLayoutResource(R.layout.preference_number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mValue);

        // prevent keyboard from showing up
        mNumberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        value = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (value != mValue) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new value
        if (positiveResult) {
            int numberPickerValue = mNumberPicker.getValue();
            if (callChangeListener(numberPickerValue))
                setValue(numberPickerValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.minValue = getMinValue();
        myState.maxValue = getMaxValue();
        myState.value = getValue();

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // restore the state
        SavedState myState = (SavedState) state;
        setMinValue(myState.minValue);
        setMaxValue(myState.maxValue);
        setValue(myState.value);

        super.onRestoreInstanceState(myState.getSuperState());
    }

    private static class SavedState extends BaseSavedState {
        int minValue;
        int maxValue;
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);

            minValue = source.readInt();
            maxValue = source.readInt();
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(minValue);
            dest.writeInt(maxValue);
            dest.writeInt(value);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
