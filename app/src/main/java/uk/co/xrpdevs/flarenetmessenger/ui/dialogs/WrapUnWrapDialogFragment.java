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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import uk.co.xrpdevs.flarenetmessenger.Convert;
import uk.co.xrpdevs.flarenetmessenger.R;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class WrapUnWrapDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    public boolean confirm = false;
    private final WrapUnWrapDialogFragment mThis = this;
    private String prompt = "Enter Amount To Wrap";
    private String tag;
    public BigInteger balance;
    boolean wrapping = true;
    private OnResultListener onResultListener;
    private EditText amount;
    private TextView tv_percent;
    public String _balstring;
    private String _humanAmount;
    private String _percent;
    private BigInteger _inWei;

    private String cunt;


    public interface OnResultListener {
        void onResult(String humanAmount, BigInteger inWei, String percentage, String tag) throws IOException, JSONException;
    }

    public WrapUnWrapDialogFragment newInstance(
            final OnResultListener listener, String prompt, String tag, String mybalance, boolean _wrap) {
//        myLog("BAL", balance.toString());
        mThis.prompt = prompt;
        this.prompt = prompt;
        mThis.tag = tag;
        mThis.confirm = confirm;
        mThis.wrapping = _wrap;
        WrapUnWrapDialogFragment fragment = new WrapUnWrapDialogFragment();
        fragment.prompt = prompt;
        fragment.tag = tag;
        fragment.cunt = mybalance;
        fragment.balance = Convert.toWei(mybalance, Convert.Unit.ETHER).toBigIntegerExact();
        fragment._balstring = mybalance;
        fragment.wrapping = _wrap;
        mThis._balstring = mybalance;
        fragment.setOnResultListener(listener);
        return fragment;
    }


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


        //  if(wrapping){balance.subtract(new BigInteger("80000000")); }
        //     myLog("BAL", mThis.balance.toString());
        myLog("BAL", _balstring);

        //  balance = Convert.toWei(mThis._balstring, Convert.Unit.ETHER).toBigIntegerExact();
        myLog("BALF", balance.toString());

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

                BigInteger _units = new BigInteger("0");

                myLog("_balance", balance.toString());

                _units = balance.divide(new BigInteger("100"));

                _inWei = _units.multiply(new BigInteger(String.valueOf(paramInt)));

                BigDecimal _amount = new BigDecimal(_inWei, 18);

                String disp = _amount.setScale(6, RoundingMode.FLOOR).stripTrailingZeros().toPlainString();

                _humanAmount = disp;

                _percent = String.valueOf(paramInt);

                amount.setText(disp);

            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFieldValid(s.toString())) {
                    amount.setError(null);
                } else {
                    amount.setError(getString(R.string.empty_field));
                }
            }
        });

        // only validate the second field if we have asked for initial confirmation

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (wrapping) {
            builder.setView(content)
                    .setPositiveButton("Wrap", null);
        } else {
            builder.setView(content)
                    .setPositiveButton("Unwrap", null);
        }
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
            //     builder.setCancelable(false);
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
                            try {
                                onResultListener.onResult(_humanAmount, _inWei, _percent, tag);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            myLog("PIN", "ORL is NULL");
                        }
                    }
            );
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
