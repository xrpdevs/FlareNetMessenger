package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import uk.co.xrpdevs.flarenetmessenger.FlareNetMessenger;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.PKeyScanner;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.dbHelper;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
@SuppressWarnings("deprecation")
public class SelectBlockChainDialogFragment extends android.app.DialogFragment {

    SelectBlockChainDialogFragment mThis = this;
    public String titletxt = "No title";
    public String prompt = "Enter Pin Code";
    public Boolean cancelable = true;
    public SimpleAdapter bcAdapter;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<>();
    String bcid;
    ListView lv;
    Resources res;
    String packageName = "uk.co.xrpdevs.flarenetmessenger";


    public interface OnResultListener {
        void onResult(HashMap<String, ?> data) throws GeneralSecurityException, IOException;

    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */

    public SelectBlockChainDialogFragment newInstance(final OnResultListener listener, String title, String prompt, Boolean cancelable, String _bcid) {
        bcid = _bcid;
        return newInstance(listener, title, prompt, cancelable);
    }


    public SelectBlockChainDialogFragment newInstance(final OnResultListener listener, String title, String prompt, Boolean cancelable) {
        // todo: modify this class to handle tokens
        mThis.prompt = prompt;
        this.prompt = prompt;
        SelectBlockChainDialogFragment fragment = new SelectBlockChainDialogFragment();
        fragment.prompt = prompt;
        fragment.cancelable = cancelable;
        fragment.titletxt = title;
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;

    /**
     * Sets the ConfigSetListener to receive callback when the PIN code is set.
     */
    private void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_blockchain, null);
        res = getActivity().getApplication().getApplicationContext().getResources();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        TextView title = content.findViewById(R.id.replyTitle);
        TextView rText = content.findViewById(R.id.theirMessage);
        lv = content.findViewById(R.id.blockchains_list);
        populateBlockChains();
        rText.setText(prompt);
        title.setText(titletxt);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        builder.setCancelable(true);
        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.x = 0;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private ActionBar getActionBar() {
        if (this.getActivity().getLocalClassName().equals("PKeyScanner")) {
            return ((PKeyScanner) getActivity()).getSupportActionBar();
        } else {
            return ((MainActivity) getActivity()).getSupportActionBar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public int getDrawableId(String resname) {
        return res.getIdentifier(
                resname, "mipmap", packageName);
    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_blockchains,
                new String[]{"NAME", "TOKNAME", "TYPE", "INTID"},
                new int[]{R.id.bcListChainName, R.id.bcListTokenName, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView iv = view.findViewById(R.id.bcListIcon);
                HashMap<String, String> item;
                item = (HashMap<String, String>) getItem(position);
                iv.setImageResource(getDrawableId(item.get("ICON")));
                return view;
            }
        };

        lv.setOnItemClickListener((parent, v, position, id) -> {
            SharedPreferences sp = getActivity().getSharedPreferences("fnm", 0);
            SharedPreferences.Editor se = sp.edit();
            HashMap<String, String> data = lines.get(position);
            data.put("id", String.valueOf(position));
            se.putInt("csbc_id", Integer.parseInt(Objects.requireNonNull(data.get("INTID"))));
            se.putString("csbc_name", data.get("NAME"));
            se.putString("csbc_rpc", data.get("RPC"));
            se.putString("csbc_cid", data.get("CHAINID"));
            se.putString("csbc_type", data.get("TYPE"));
            se.apply();
            getActionBar().setDisplayUseLogoEnabled(true);
            getActionBar().setLogo(getDrawableId(data.get("ICON")));
            if (onResultListener != null) {
                try {
                    onResultListener.onResult(data);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return simpleAdapter;

    }

    public void populateBlockChains() {
        feedList.clear();
        Cursor c = FlareNetMessenger.dbH.getBlockChains();
        feedList = dbHelper.cursorToHashMapArray(c);
        mThis.getActivity().runOnUiThread(() -> {
            bcAdapter = fillListView(feedList);
            lv.setAdapter(bcAdapter);
        });
    }
}
