package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import uk.co.xrpdevs.flarenetmessenger.R;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class WrapUnWrapDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    public boolean confirm = false;
    WrapUnWrapDialogFragment mThis = this;
    String prompt = "Enter Amount To Wrap";
    String tag;
    BigInteger balance;


    public interface OnResultListener {
        void onResult(String pinCode, String tag) throws IOException, JSONException;
    }

    public WrapUnWrapDialogFragment newInstance(final OnResultListener listener, String prompt, String tag, BigInteger balance) {
        mThis.prompt = prompt;
        this.prompt = prompt;
        mThis.tag = tag;
        //boolean confirm;
        mThis.confirm = confirm;
        mThis.balance = balance;
        setCancelable(false);
        WrapUnWrapDialogFragment fragment = new WrapUnWrapDialogFragment();
        fragment.prompt = prompt;
        fragment.tag = tag;
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;
    private EditText amount;
    private TextView tv_percent;

    /**
     * Sets the ConfigSetListener to receive callback when the PIN code is set.
     */
    private void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.wrapunwrap_dialog, null);
        amount = content.findViewById(R.id.editTextNumberDecimal2);
        tv_percent = content.findViewById(R.id.tv_percent);
        SeekBar seekbar = content.findViewById(R.id.seekbar);

        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
//        editPinCode.setInputType(inputType);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar bar) {
                int value = bar.getProgress(); // the value of the seekBar progress
            }

            public void onStartTrackingTouch(SeekBar bar) {

            }

            public void onProgressChanged(SeekBar bar,
                                          int paramInt, boolean paramBoolean) {
                tv_percent.setText("" + paramInt + "%"); // here in textView the percent will be shown
                BigInteger _units = new BigInteger(String.valueOf(balance.divide(new BigInteger("100"))));
                BigDecimal _amount = new BigDecimal(_units.multiply(new BigInteger(String.valueOf(paramInt))), 18);
                String disp = _amount.setScale(6, RoundingMode.FLOOR).stripTrailingZeros().toPlainString();
                amount.setText(disp);

            }
        });

       /* editPinCode.addTextChangedListener(new TextWatcher() {
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
        });*/
        if (confirm) {
            //confirmPinCode.setInputType(inputType);

            setCancelable(false);
            mThis.setCancelable(false);
            /*confirmPinCode.addTextChangedListener(new TextWatcher() {
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
            });*/
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
        //   if (!confirm) confirmPinCode.setVisibility(View.GONE);
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                        // final String pinCode = editPinCode.getText().toString();
                        //myLog("PIN", pinCode);
                        // if (isFieldValid(pinCode)) {
                        //     myLog("PIN", "ifv: " + isFieldValid(pinCode));
                        if (onResultListener != null) {
                            myLog("PIN", "oRL: " + (onResultListener != null));
                            //        try {
                            //            onResultListener.onResult(pinCode, tag);
                            //       } catch (IOException e) {
                            //            e.printStackTrace();
                            //       } catch (JSONException e) {
                            //            e.printStackTrace();
                            //        }
                            //dialog.dismiss();
                        }
                    }// else {
                    //    editPinCode.setError(getString(R.string.empty_field));
                    //  }
                    //   }
            );
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
