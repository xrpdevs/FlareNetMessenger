package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;

import java.io.IOException;

import uk.co.xrpdevs.flarenetmessenger.R;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class PinCodeDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    public boolean confirm = false;
    PinCodeDialogFragment mThis = this;
    String prompt = "Enter Pin Code";
    String tag;


    public interface OnResultListener {
        void onResult(String pinCode, String tag) throws IOException, JSONException;
    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */
   /* public PinCodeDialogFragment newInstance(final OnResultListener listener, String prompt, String tag) {
        mThis.prompt = prompt;
        this.prompt = prompt;
        mThis.tag = tag;
        mThis.confirm = false;
        PinCodeDialogFragment fragment = new PinCodeDialogFragment();
        fragment.prompt = prompt;
        fragment.tag = tag;
        fragment.setOnResultListener(listener);
        return fragment;
    }*/
    public PinCodeDialogFragment newInstance(final OnResultListener listener, String prompt, String tag) {
        mThis.prompt = prompt;
        this.prompt = prompt;
        mThis.tag = tag;
        //boolean confirm;
        mThis.confirm = confirm;
        setCancelable(false);
        PinCodeDialogFragment fragment = new PinCodeDialogFragment();
        fragment.prompt = prompt;
        fragment.tag = tag;
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;
    private EditText editPinCode;
    private EditText confirmPinCode;
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
        editPinCode = content.findViewById(R.id.editPinCode);
        confirmPinCode = content.findViewById(R.id.editPinCode2);

        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        editPinCode.setInputType(inputType);
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
        if (confirm) {
            confirmPinCode.setInputType(inputType);

            setCancelable(false);
            mThis.setCancelable(false);
            confirmPinCode.addTextChangedListener(new TextWatcher() {
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
                    if (!editPinCode.getText().toString().equals(confirmPinCode.getText().toString())) {
                        confirmPinCode.setError("Codes do not match!");
                    } else {
                        confirmPinCode.setError(null);
                        if (!editPinCode.getText().toString().equals("")) {
                            setCancelable(true);
                        }
                    }
                }
            });
        } // only validate the second field if we have asked for initial confirmation

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content)
                // Button clicks are handled by the DialogFragment!
                .setPositiveButton(R.string.connect, null);

        TextView title = new TextView(builder.getContext());
// You Can Customise your Title here
        title.setText(prompt);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        if (confirm) {
            builder.setCancelable(false);
        }
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (!confirm) confirmPinCode.setVisibility(View.GONE);
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                final String pinCode = editPinCode.getText().toString();
                myLog("PIN", pinCode);
                if (isFieldValid(pinCode)) {
                    myLog("PIN", "ifv: " + isFieldValid(pinCode));
                    if (onResultListener != null) {
                        myLog("PIN", "oRL: " + (onResultListener != null));
                        try {
                            onResultListener.onResult(pinCode, tag);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //dialog.dismiss();
                    }
                } else {
                    editPinCode.setError(getString(R.string.empty_field));
                }
            });
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
