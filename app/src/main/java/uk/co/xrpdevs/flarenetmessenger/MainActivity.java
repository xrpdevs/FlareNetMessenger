package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    public Web3j FlareConnection;
    private Object TextView;

    public String walletAddress = "0x24423475227b49376d72E863bB6c5b6cB4E60Cea";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FlareConnection = MyService.initWeb3j();
        EthBlockNumber bob = getBlockNumber();
        Log.d("TEST", bob.getBlockNumber().toString());


        TextView myTV = findViewById(R.id.text1);

        TextView myBalance = findViewById(R.id.balance);
        myTV.setText("Current Coston\nBlock Number:\n"+bob.getBlockNumber().toString());
        myBalance.setText("FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());

        Log.d("TEST", "FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());
     //   FlareConnection.

    }
    public BigDecimal getMyBalance(String walletAddress) {

        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = FlareConnection
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        BigDecimal wei = new BigDecimal(ethGetBalance.getBalance());

        BigDecimal FXRP;
        FXRP = Convert.fromWei(wei, Convert.Unit.ETHER);

        return FXRP;
    }
    public EthBlockNumber getBlockNumber() {
        EthBlockNumber result = new EthBlockNumber();

        try {
            result = FlareConnection.ethBlockNumber()
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.d("TEST", result.getBlockNumber().toString());
        return result;
    }
}