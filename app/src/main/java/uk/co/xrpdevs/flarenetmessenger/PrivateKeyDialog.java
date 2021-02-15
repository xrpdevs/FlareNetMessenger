package uk.co.xrpdevs.flarenetmessenger;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class PrivateKeyDialog extends DialogPreference {

    private String pKey = "";

    public PrivateKeyDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_privkey);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //value = picker.getCurrent();
        super.onDismiss(dialog);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

      //  picker.setCurrent((int) value);
    }

  //  @Override
  //  protected Object onGetDefaultValue(TypedArray a, int index) {
  //      return Long.parseLong(a.getString(index));
  //  }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedString("pkey"));
        } else {
            setValue(((String) defaultValue));
        }
    }

    void setValue(String value) {
        persistString(value);

        notifyDependencyChange(false);
        this.pKey = value;
    }

    String getValue() {
        return pKey;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        String pKey = getValue();

        if (positiveResult) {
            if (callChangeListener(pKey)) {
                setValue(pKey);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        String value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}


