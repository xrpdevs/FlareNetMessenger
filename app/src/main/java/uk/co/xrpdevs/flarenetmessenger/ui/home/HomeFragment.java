package uk.co.xrpdevs.flarenetmessenger.ui.home;

import static android.content.Context.CLIPBOARD_SERVICE;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import uk.co.xrpdevs.flarenetmessenger.BuildConfig;
import uk.co.xrpdevs.flarenetmessenger.FirstRun;
import uk.co.xrpdevs.flarenetmessenger.FlareNetMessenger;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.dbHelper;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.SelectBlockChainDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.token.TokensFragment;

//import android.net.Credentials;

public class HomeFragment extends Fragment implements SelectBlockChainDialogFragment.OnResultListener, SwipeRefreshLayout.OnRefreshListener {

    PleaseWaitDialog notify;
    SelectBlockChainDialogFragment sbcdf;
    private NfcAdapter adapter = null;
    HomeFragment mThis = this;
    View root;
    String XRPAddress;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    BottomNavigationView navView;
    TextView walletName, balanceView;
    Resources res;
    ImageView walletIcon;
    Activity mAct;
    dbHelper dbH = FlareNetMessenger.dbH;
    Button hShare, hCopy, hAssets;
    private PendingIntent pendingIntent = null;
    Credentials c;
    Web3j fc = MyService.initWeb3j();
    Thread qrThread;// = new QR_Thread();
    String packageName = "uk.co.xrpdevs.flarenetmessenger";
    SwipeRefreshLayout mSwipeRefreshLayout;
    public HomeFragment() throws IOException {
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        Service bob = new MyService();
        int cw = prefs.getInt("currentWallet", 0);
        //int wt = FlareNetMessenger.dbH.walletCount();

        //cw=3;
        Cursor c = dbH.getWalletDetails(String.valueOf(cw));
        mAct = mThis.getActivity();
        if(c!=null && c.getCount()!=0) {
            deets = dbHelper.cursorToHashMapArray(c).get(0);
            FlareNetMessenger.deets = deets;
            res = getActivity().getApplication().getApplicationContext().getResources();
            navView = mThis.getActivity().findViewById(R.id.nav_view);
            navView.getMenu().findItem(R.id.navigation_home).setChecked(true);
            Bundle args = getArguments();
            myLog("FRAG", "onStart");
            adapter = NfcAdapter.getDefaultAdapter(mAct);

            if (args != null) {

                myLog("FRAG", args.toString());
            }
            if (args != null) {
                if (args.containsKey("updatewallet")) {
                    Cursor cur = dbH.getWalletDetails(String.valueOf(prefs.getInt("currentWallet", 0)));
                    deets = dbHelper.cursorToHashMapArray(cur).get(0);
                    FlareNetMessenger.deets = deets;
                }
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

            XRPAddress = deets.get("ADDRESS");
            // c = Credentials.create(deets.get("walletPrvKey"));
            Log.d("abcdef", deets.get("ICON"));
            walletName.setText(deets.getOrDefault("NAME", "Wallet " + prefs.getInt("currentWallet", 0)));

            walletIcon.setImageResource(getDrawableId(deets.get("ICON")));
            GetBalance gb = new GetBalance();
            gb.refreshnow = false;
            gb.start();
            qrThread = new QR_Thread();
            qrThread.start();
        }
    }

    public int getDrawableId(String resname) {
        return res.getIdentifier(
                resname, "mipmap", packageName);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getActivity(mAct, 0,
                    new Intent(mAct, mAct.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            //currentTagView.setText("Scan a tag");
        }


        if (adapter != null) adapter.enableForegroundDispatch(mAct, pendingIntent, null, null);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        myLog("FRAG", "HomeFragment");
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        hCopy = root.findViewById(R.id.homeQR_copy);
        hShare = root.findViewById(R.id.homeQR_share);
        hAssets = root.findViewById(R.id.hAssets);
        walletName = root.findViewById(R.id.textView7);
        balanceView = root.findViewById(R.id.textView6);
        walletIcon = root.findViewById(R.id.imageView4);
        //walletIcon.setImageResource(.getDrawableId(item.get("ICON")));
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        //   BottomNavigationView navView = root.getParent().findViewById(R.id.nav_view);
        navView = mThis.getActivity().findViewById(R.id.nav_view);

        prefs = mThis.getActivity().getSharedPreferences("fnm", 0);

        //webview.loadUrl("https://xrpdevs.co.uk/");

        hCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", deets.get("ADDRESS"));
                clipboard.setPrimaryClip(clip);
            }
        });
        hShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Add some information about what blockchain wallet is for to avoid confusion.

                ImageView imageView = root.findViewById(R.id.imageView2);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                String bitmapPath = MediaStore.Images.Media.insertImage(mAct.getContentResolver(), bitmap, "Wallet", deets.get("ADDRESS"));
                Uri bitmapUri = Uri.parse(bitmapPath);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                startActivity(Intent.createChooser(intent, "Share"));

            }
        });
        hAssets.setOnClickListener(v -> {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            //fragmentTransaction.remove(currentFragment);
            Fragment f = new TokensFragment();
            Bundle args = new Bundle();
            args.putInt("ltype", 2000);
            args.putString("wBcid", deets.get("BCID"));
            args.putString("selectFragment", "tokens");
            f.setArguments(args);
            fragmentTransaction.replace(R.id.nav_host_fragment, f);
            fragmentTransaction.addToBackStack("home").commit();
        });
        mSwipeRefreshLayout = root.findViewById(R.id.swipe_container_home);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);

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

                showDialog("About this App", "Version: " + BuildConfig.VERSION_NAME + "\n\nBuild: " + BuildConfig.VERSION_CODE + "\n\n" +
                        "Built: \n" + new Date(Long.parseLong(BuildConfig.BUILD_TIME)).toString() +
                        "\n\nCurrent blockchain:\n" + deets.get("BCID") + ": " + deets.get("BCNAME") +
                        "\nRPC URL:\n" + deets.get("RPC"), true);
                return true;
            case R.id.ma1:
                Intent aa = new Intent(this.getActivity(), MainActivity.class);
                startActivity(aa);
                return true;
            case R.id.pks:
                showDialog("Your Public Key", "PubKey:\n\n" + deets.get("PUBKEY"), true);
                return true;
            case R.id.show_prices:
                String prompt = "";
                for (Map.Entry<String, String> entry : FlareNetMessenger.prices.get().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    prompt += key + ": " + value + "\n";
                }


                showDialog("Current Prices", prompt, true);
                return true;
            case R.id.select_blockchain:
                bcDialog("Select Blockchain", true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //    if(mSwipeRefreshLayout != null) {
                //      mSwipeRefreshLayout.setRefreshing(true);
                //   }
                try {
                    GetBalance gb = new GetBalance();
                    gb.refreshnow = true;
                    gb.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //  Log.d("REFRESH", "SSSSS");

    }

    class GetBalance extends Thread {
        String balance = "Balance: unknown";
        boolean refreshnow = false;

        @Override
        public void run() {
            try {
                if (refreshnow) FlareNetMessenger.prices.updateNow(deets.get("SYMBOL"), "USDT");
                if (deets.get("TYPE").equals("XRPL")) {
                    BigDecimal first = Utils.getMyXRPBalance(deets.get("ADDRESS"), deets.get("RPC")).first;
                    balance = first.setScale(2, RoundingMode.FLOOR).toPlainString();

                    if (balance.equals("-1")) {
                        balance = "Not active (send 20 XRP)";
                    }
                } else {
                    balance = Utils.getMyBalance(deets.get("ADDRESS")).first.setScale(2, RoundingMode.FLOOR).toPlainString();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            mAct.runOnUiThread(() -> {
                String usval = null;
                try {
                    usval = FlareNetMessenger.prices.get(deets.get("SYMBOL") + "USDT");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d("abcdef", usval);
                try {
                    usval = new BigDecimal(usval).multiply(new BigDecimal(balance)).setScale(2, RoundingMode.FLOOR).toPlainString();
                } catch (Exception e) {
                }
                balanceView.setText("Balance: " + balance + " ($" + usval + ")");
            });
        }
    }

    class QR_Thread extends Thread {
        Bitmap bmp;

        @Override
        public void run() {
            QRCodeWriter writer = new QRCodeWriter();
            // Map<EncodeHintType, ?> hints = new
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 0);
            if (XRPAddress != null) {
                try {
                    BitMatrix bitMatrix = writer.encode(XRPAddress, BarcodeFormat.QR_CODE, 512, 512, hints);
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

            mAct.runOnUiThread(() -> {
                ((ImageView) mAct.findViewById(R.id.imageView2)).setImageBitmap(bmp);

            });
        }
    }

    private boolean showDialog(String title, String prompt, Boolean cancelable) {
        FragmentManager manager = getParentFragmentManager();

        notify = new PleaseWaitDialog();
        notify.titleText = title;
        notify.prompt = prompt;
        notify.cancelable = cancelable;

        notify.show(getActivity().getFragmentManager(), "aaa");
        return true;
    }

    private boolean bcDialog(String title, Boolean cancelable) {
        android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();

        sbcdf = new SelectBlockChainDialogFragment().newInstance(this, title, title, true);
        //sbcdf.titleText = "About this App";
        sbcdf.prompt = title;
        sbcdf.cancelable = cancelable;

        sbcdf.show(manager, "aaa");
        return true;
    }

    @Override
    public void onResult(HashMap<String, ?> data) throws GeneralSecurityException, IOException {
        sbcdf.dismiss();
        String RPC = (String) data.get("RPC");
        int CID = Integer.decode((String) data.get("CHAINID"));
        MyService.fCoinLink = MyService.initConnection(RPC, CID); // maybe just keep this to FLR
        MyService.rpc = RPC;
        MyService.tmpCID = CID;
        MyService.isXRPL = true;
        // todo: check here that we have the destination address' public key!

    }

}