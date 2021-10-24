package uk.co.xrpdevs.flarenetmessenger.ui.wallets;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.FlareNetMessenger;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.PKeyScanner;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.Zipper;
import uk.co.xrpdevs.flarenetmessenger.dbHelper;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.home.HomeFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.token.TokensFragment;

public class WalletsFragment extends Fragment implements PinCodeDialogFragment.OnResultListener {

    /* TODO:
        - Context menu: Remove wallet, rename wallet, view tokens, transfer, export private key (QR code)
        - Import wallet(s) from ZIP file created by app - check format of outputted JSON in export wallets, too.
     */

    public SimpleAdapter WalletsAdaptor;

    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<>();

    WalletsFragment mThis = this;
    ListView lv;
    PinCodeDialogFragment pinDialog;

    Activity mAct;
    Uri walletsImportURI;

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_wallets, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myLog("FRAG", "WalletsFragment");
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_wallets, container, false);
        lv = root.findViewById(R.id.wallets_list);
       /// lv.setAdapter(WalletsAdaptor);
        registerForContextMenu(lv);
        prefs = this.requireActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        mAct = this.getActivity();
        if (prefs.getInt("walletCount", 0) > 0) {
            myLog("FRAG", "Wallet count is non zero");

            getWalletList();
        }

        FloatingActionButton fab = root.findViewById(R.id.fab);
        // TODO: change intent methods to Fragment context switches
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(mThis.getActivity(), PKeyScanner.class);
            startActivity(intent);
        });

        return root;
    }

    public SimpleAdapter fillListView(Cursor c) {
        return null;
    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        myLog("Lines", lines.toString());
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                mThis.getActivity(), lines, R.layout.listitem_wallets,
                new String[]{"NAME", "ADDRESS", "BCNAME", "LEDGER_BALANCE"},
                new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                return view;
            }
        };


        lv.setOnItemClickListener((parent, v, position, id) -> {
            HashMap<String, String> theItem = lines.get(position);
            //String pooo = theItem.get("num");
            //myLog("smscseeker", "name:" + theItem.toString());
            pEdit.putInt("currentWallet", Integer.parseInt(lines.get(position).get("ID")));
            pEdit.commit();

            assert getFragmentManager() != null;
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            HomeFragment f = null;
            try {
                f = new HomeFragment();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bundle args = new Bundle();
            args.putInt("ltype", 2000);
            args.putBoolean("updatewallet", true);
            args.putString("selectFragment", "home");
            if (f != null) {
                f.setArguments(args);
                Intent myService = new Intent(mAct, MyService.class);
                mAct.stopService(myService);
                mAct.startService(myService);

                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("wallets").commit();
            }

        });

        return simpleAdapter;

    }


    public void getWalletList() {
        feedList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor c = FlareNetMessenger.dbH.getWallets();
                feedList = dbHelper.cursorToHashMapArray(c);
                lv.setAdapter(fillListView(feedList));
            }
        }).start();
        //mThis.requireActivity().runOnUiThread(() -> {
        //   //  WalletsAdaptor = fillListView(feedList);

        // });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.export_wallets:
                exportWallets();
                return true;
            case R.id.import_wallets:
                importWallets();
                return true;
            case R.id.tokens:
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                Fragment f = new TokensFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 2000);
                args.putString("selectFragment", "home");
                f.setArguments(args);


                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("wallets").commit();
                return true;
     /*       case R.id.theNav:
                Intent intent2 = new Intent(mThis.getContext(), FirstRun.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent2);
            case R.id.ma2:
                Intent intent3 = new Intent(mThis.getContext(), MainActivity.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent3);
*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void importWallets() {
        //File downloadedfile = new File(Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + "/myapp") + "/" + "downloadedfile.zip");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(null, "*/*");
        startActivityForResult(intent, 7959);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7959) {
            walletsImportURI = data.getData(); //The uri with the location of the file
            //  Toast.makeText(mThis.getActivity(), selectedfile.toString(), Toast.LENGTH_LONG ).show();
            // todo: ask for PIN code (for the archive)
            FragmentManager manager = mThis.requireActivity().getFragmentManager();
            pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter Archive PIN:", "import");
            pinDialog.show(manager, "1");
        }
    }

    public void exportWallets() {
        FragmentManager manager = mThis.requireActivity().getFragmentManager();
        pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter PIN:", "export");
        pinDialog.show(manager, "1");
    }

    @SuppressLint("SdCardPath")
    @Override
    public void onResult(String pinCode, String tag) throws IOException, JSONException {
        if (tag.equals("import")) {
            Zipper zipDecode = new Zipper(pinCode, mThis.requireContext());
            JSONArray wallets;
            if ((wallets = zipDecode.extractWithZipInputStream(walletsImportURI, pinCode)) != null) {
                // todo: do something with our JSONobject
                Log.d("IMPORT", wallets.toString());
            } else {
                // todo: tell user they entered an incorrect PIN.
            }
        }
        if (tag.equals("export")) {
            myLog("Motherfucker", prefs.getString("pinCode", "ffnf"));
            if (Utils.pinHash(pinCode).equals(prefs.getString("pin", "asas"))) {
                pinDialog.dismiss();
                Zipper zipArchive = new Zipper(pinCode, mThis.requireContext());
                zipArchive.pack("/sdcard/Downloads/wallets.zip");

                // TODO: Save as a passworded ZIP file in default location on phone.
            }
        }
    }

}
//                     Web3j sendObj = MyService.initConnection(prefs.getString("csbc_rpc", ""), Integer.decode(prefs.getString("csbc_cid", "0")));