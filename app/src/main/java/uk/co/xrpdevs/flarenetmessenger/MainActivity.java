package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    public Web3j FlareConnection;
    private Object TextView;
    TextView myTV;
    EthBlockNumber bob;
    Button refresh;
    TextView myBalance;
    public String walletAddress;
    public String contractAddress;
    public String walletPrivateKey;
    Credentials c;
    ContractGasProvider cgp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FlareConnection = MyService.initWeb3j();
        bob = getBlockNumber();
        Log.d("TEST", bob.getBlockNumber().toString());

        myBalance = findViewById(R.id.balance);
        myTV = findViewById(R.id.text1);

        walletAddress    = "0x24423475227b49376d72E863bB6c5b6cB4E60Cea";
        walletPrivateKey = "0xc6d66f9d9cd4c607e742d27c5fb9a9140465226c19578c02a13931f1fd0c8ef2";
        contractAddress  = "0xa49D5f1f6e63406E9dd6BF6BbAC5A9ac085527e7";
        String account = null;
        try {
            account = FlareConnection.ethAccounts().send().getAccounts().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c = Credentials.create(account);
     //   cgp = FlareConnection.ethGasPrice();

        refresh = findViewById(R.id.button);
        refresh.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           update();
                                       }
                                   }
        );
        myTV.setText("Current Coston\nBlock Number:\n"+bob.getBlockNumber().toString());
        myBalance.setText("FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());

        Log.d("TEST", "FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());
     //   FlareConnection.

    }

    public void update() {
        myTV.setText("Current Coston\nBlock Number:\n" + bob.getBlockNumber().toString());
        myBalance.setText("FXRP Balance of Flare Testnet Address " + walletAddress + " = " + getMyBalance(walletAddress).toString());
        ContractBindings contract = ContractBindings.load(contractAddress, FlareConnection, c, cgp);
                // //load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider

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