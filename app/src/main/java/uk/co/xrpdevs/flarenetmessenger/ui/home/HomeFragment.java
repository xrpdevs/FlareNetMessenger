package uk.co.xrpdevs.flarenetmessenger.ui.home;

import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.BuildConfig;
import uk.co.xrpdevs.flarenetmessenger.FirstRun;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.ui.token.TokensFragment;

import static android.content.Context.CLIPBOARD_SERVICE;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

//import android.net.Credentials;

public class HomeFragment extends Fragment {

    PleaseWaitDialog notify;

    HomeFragment mThis = this;
    View root;
    String XRPAddress;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    BottomNavigationView navView;
    TextView walletName;
    Activity mAct;
    Button hShare, hCopy, hAssets;

    Credentials c;
    Web3j fc = MyService.initWeb3j();
    Thread qrThread;// = new QR_Thread();

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_home, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public void onStart() {
        super.onStart();
        Service bob = new MyService();
        try {
            deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        navView = mThis.getActivity().findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.navigation_home).setChecked(true);
        Bundle args = getArguments();
        myLog("FRAG", "onStart");
        mAct = mThis.getActivity();

        if (args != null) {

            myLog("FRAG", args.toString());
        }
        if(args != null) {
            if (args.containsKey("selectFragment")) {
                if (args.getString("selectFragment", "").equals("home")) {
                    myLog("FRAG", "Has selectFragment = home");

                    navView.setSelectedItemId(R.id.navigation_home);
                }
                if (args.getString("selectFragment", "").equals("tokens")) {
                    myLog("FRAG", "Has selectFragment = tokens");

                    navView.setSelectedItemId(R.id.navigation_wallets);
                }
            }
        }
        if(prefs.contains("currentWallet")) {
            c = org.web3j.crypto.Credentials.create(deets.get("walletPrvKey"));
            try {
                deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            XRPAddress = deets.get("walletAddress");
            c = Credentials.create(deets.get("walletPrvKey"));
            walletName.setText(deets.getOrDefault("walletName", "Wallet "+prefs.getInt("currentWallet", 0)));
            qrThread = new QR_Thread();
            qrThread.start();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        Log.d("FRAG", "HomeFragment");
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        hCopy = root.findViewById(R.id.homeQR_copy); hShare = root.findViewById(R.id.homeQR_share); hAssets = root.findViewById(R.id.hAssets);
        walletName = root.findViewById(R.id.textView7);
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
     //   BottomNavigationView navView = root.getParent().findViewById(R.id.nav_view);
        navView = mThis.getActivity().findViewById(R.id.nav_view);

        prefs = mThis.getActivity().getSharedPreferences("fnm", 0);
        try {
            deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        //webview.loadUrl("https://xrpdevs.co.uk/");

        hCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", deets.get("walletAddress"));
                clipboard.setPrimaryClip(clip);
            }
        });
        hShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Add some information about what blockchain wallet is for to avoid confusion.

                ImageView imageView = root.findViewById(R.id.imageView2);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                String bitmapPath = MediaStore.Images.Media.insertImage(mAct.getContentResolver(), bitmap, "Wallet", deets.get("walletAddress"));
                Uri bitmapUri = Uri.parse(bitmapPath);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                startActivity(Intent.createChooser(intent, "Share"));

            }
        });
        hAssets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                //fragmentTransaction.remove(currentFragment);
                Fragment f = new TokensFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 2000);
                args.putString("selectFragment", "tokens");
                f.setArguments(args);
                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("home").commit();
            }
        });
        return root;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.fr:
                myLog("FR", "FirstRun Selected");
                Intent intent2 = new Intent(mThis.getContext(), FirstRun.class);
                startActivity(intent2);
                return true;
            case R.id.version:
                BigInteger balance = new BigInteger("0");
                //try {
            //        balance = fcoin.balanceOf(deets.get("walletAddress")).send();
            //    } catch (Exception e) {
           //         e.printStackTrace();
           //     }
                showDialog("Version: "+ BuildConfig.VERSION_NAME+"\n\nBuild: "+BuildConfig.VERSION_CODE+"\n\n"+
                        "FCoin Balance: "+balance, true);
                return true;
            case R.id.ma1:
                Intent aa = new Intent(this.getActivity(), MainActivity.class); startActivity(aa);
                return true;
                case R.id.pks:
                showDialog("PubKey:\n\n"+c.getEcKeyPair().getPublicKey().toString(16), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class QR_Thread extends Thread {
        Bitmap bmp;
        @Override
        public void run() {
            QRCodeWriter writer = new QRCodeWriter();
            if (XRPAddress != null) {
                try {
                    BitMatrix bitMatrix = writer.encode(XRPAddress, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ImageView) mAct.findViewById(R.id.imageView2)).setImageBitmap(bmp);
                }
            });
        }
    }

    private boolean showDialog(String prompt, Boolean cancelable) {
        FragmentManager manager = getParentFragmentManager();

        notify = new PleaseWaitDialog();
        notify.titleText = "About this App";
        notify.prompt = prompt;
        notify.cancelable = cancelable;

        notify.show(getActivity().getFragmentManager(), "aaa");
        return true;
    }
}