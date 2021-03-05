package uk.co.xrpdevs.flarenetmessenger;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import net.lingala.zip4j.exception.ZipException;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class PinCodeDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    PinCodeDialogFragment mThis = this;
    String prompt;

    public interface OnResultListener {
        void onResult(String pinCode) throws ZipException;
    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */
    public PinCodeDialogFragment newInstance(final OnResultListener listener, String prompt) {
        mThis.prompt = prompt;
        final PinCodeDialogFragment fragment = new PinCodeDialogFragment();
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;
    private EditText editPinCode;

    /**
     * Sets the ConfigSetListener to receive callback when the PIN code is set.
     */
   private void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.pin_code_entry_dialog, null);

        editPinCode = (EditText) content.findViewById(R.id.editPinCode);
        editPinCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFieldValid(s.toString())) {
                    editPinCode.setError(null);
                } else {
                    editPinCode.setError(getString(R.string.empty_field));
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mThis.prompt)
                .setView(content)
                // Button clicks are handled by the DialogFragment!
                .setPositiveButton(R.string.connect, null);

        setCancelable(true);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String pinCode = editPinCode.getText().toString();
                    myLog("PIN", pinCode);
                    if (isFieldValid(pinCode)) {
                        myLog("PIN", "ifv: "+isFieldValid(pinCode));
                        if (onResultListener != null) {
                            myLog("PIN", "oRL: "+(onResultListener != null));
                            try {
                                onResultListener.onResult(pinCode);
                            } catch (ZipException e) {
                                e.printStackTrace();
                            }
                            //dialog.dismiss();
                        }
                    } else {
                        editPinCode.setError(getString(R.string.empty_field));
                    }
                }
            });
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
