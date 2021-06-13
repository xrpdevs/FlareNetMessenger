package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.R;

import static uk.co.xrpdevs.flarenetmessenger.Utils.getAvailChains;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class SelectBlockChainDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    SelectBlockChainDialogFragment mThis = this;
    public String titletxt = "No title";
    public String prompt = "Enter Pin Code";
    public Boolean cancelable = true;
    public Boolean isEncReqd = false;
    public SimpleAdapter bcAdapter;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<>();

    ListView lv;
    Resources res;
    String packageName = "uk.co.xrpdevs.flarenetmessenger";


    public interface OnResultListener {
        void onResult(HashMap<String, String> data) throws GeneralSecurityException, IOException;

    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */
    public SelectBlockChainDialogFragment newInstance(final OnResultListener listener, String title, String prompt, Boolean cancelable) {
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
    private EditText editMessage;
    private SwitchCompat encSwitch;


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
/*        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        getDialog().getWindow().setAttributes(p);*/
        //   editMessage = content.findViewById(R.id.editMessage);
        //  int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        //editMessage.setInputType(inputType);
        //  SwitchCompat encryptionSwitch = content.findViewById(R.id.switch1);

        //   encryptionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //        @Override
        //         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //            isEncReqd = isChecked;
        //        }
        //   });

        res = getActivity().getApplication().getApplicationContext().getResources();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        // Button clicks are handled by the DialogFragment!
        //    .setPositiveButton(R.string.connect, null);

        TextView title = content.findViewById(R.id.replyTitle);
        TextView rText = content.findViewById(R.id.theirMessage);
        ImageButton img = content.findViewById(R.id.imageButton);
        lv = content.findViewById(R.id.blockchains_list);
        populateBlockChains();
        //lv.setAdapter(bcAdapter);
        rText.setText(prompt);
// You Can Customise your Title here
        title.setText(titletxt);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        //  builder.setCustomTitle(title);

        builder.setCancelable(true);
        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.x = 0;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        //dialog.BUTTON_POSITIVE = img;
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        myLog("PIN", "oRL: " + (onResultListener != null));
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {


        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }

    public int getDrawableId(String resname) {
        int resourceId = res.getIdentifier(
                resname, "mipmap", packageName);
        return resourceId;
    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        myLog("Lines", lines.toString());


        SimpleAdapter simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_blockchains, new String[]{"Name", "NativeCurrency", "type", "lastval"}, new int[]{R.id.bcListChainName, R.id.bcListTokenName, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                TextView cType = view.findViewById(R.id.inboxType);
                ImageView iv = view.findViewById(R.id.bcListIcon);

                //   cType.setText("Coston");
                //      String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);

                iv.setImageResource(getDrawableId(item.get("Icon")));
                //iv.setImageResource();
                int unread = lines.size();
                myLog("TEST", "Number of contaxts: " + unread);

                return view;
            }
        };

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                SharedPreferences sp = getActivity().getSharedPreferences("fnm", 0);
                SharedPreferences.Editor se = sp.edit();
                HashMap<String, String> data = lines.get(position);
                data.put("id", String.valueOf(position));
                se.putInt("csbc_id", position);
                se.putString("csbc_name", data.get("Name"));
                se.putString("csbc_rpc", data.get("RPC"));
                se.putString("csbc_cid", data.get("ChainID"));
                se.apply();
                getActionBar().setDisplayUseLogoEnabled(true);
                getActionBar().setLogo(getDrawableId(data.get("Icon")));
                if (onResultListener != null) {
                    myLog("PIN", "oRL: " + (onResultListener != null));
                    try {
                        onResultListener.onResult(data);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //dialog.dismiss();
                }
            }

        });
//                Intent i = new Intent(mThis.getActivity(),
//                        MainActivity.class);
//                HashMap<String, String> theItem = lines.get(position);
//                String pooo = theItem.get("num");
//                myLog("smscseeker", "name:" + theItem.toString());
        //        pEdit.putInt("currentWallet", (position +1 ));
        //        pEdit.commit();

        //Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
        //fragmentTransaction.remove(currentFragment);
        //           HomeFragment f = new HomeFragment();
        //           Bundle args = new Bundle();
        //           args.putInt("ltype", 2000);
        //           args.putString("selectFragment", "home");
        //           f.setArguments(args);
        //          Intent myService = new Intent(mAct, MyService.class);
        //          mAct.stopService(myService);
        //          mAct.startService(myService);

//                fragmentTransaction.replace(R.id.nav_host_fragment, f);
        //              fragmentTransaction.addToBackStack("wallets").commit();

        // startActivity(i);

        //          }
        //      });

        return simpleAdapter;

    }

    public void populateBlockChains() {
        feedList.clear();

        ArrayList<HashMap<String, String>> maplist = getAvailChains();

        String dtemp;
        for (int j = 0; j < maplist.size(); j++) {
            HashMap<String, String> poo = maplist.get(j);
            // dtemp = getDate(Long.parseLong(poo.get("ts")));
            myLog("BLOCKCHAINS", poo.toString());

            //   poo.put("date", dtemp);
            // TODO: Local database of names associated with Coston addresses.
            //      poo.put("cnam", dbHelper.getContactName(this, poo.get("num")));


            feedList.add(poo);

        }

        mThis.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // mContactList.setAdapter(cursorAdapter);

                bcAdapter = fillListView(feedList);
                lv.setAdapter(bcAdapter);
                myLog("TEST", "Running UI thread");


            }
        });
        //   myLog("feedList", feedList.toString());
//        Collections.reverse(feedList);
        // WalletsAdaptor = fillListView(feedList);
    }
}
