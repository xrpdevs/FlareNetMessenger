package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.os.AsyncTask;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

//import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;

public class aRawTransaction {

    private final Credentials mCredentials;
    private final Web3j mWeb3j;
    private final String fromAddress;
    private final String mValueGasPrice;
    private final String mValueGasLimit;
    private final long mChainID;

    //   private CBSendingEther cbSendingEther;

    public aRawTransaction(Web3j web3j, Credentials credentials, String valueGasPrice, String valueGasLimit, long chainID) {
        mWeb3j = web3j;
        mCredentials = credentials;
        fromAddress = credentials.getAddress();
        mValueGasPrice = valueGasPrice;
        mValueGasLimit = "30";//valueGasLimit;
        mChainID = chainID;
    }

    private BigInteger getNetworkGasPrice() {
        BigInteger gasPrice = BigInteger.ONE;
        try {
            Request<?, EthGasPrice> rs = mWeb3j.ethGasPrice();
            EthGasPrice eGasPrice = rs.sendAsync().get();
            gasPrice = eGasPrice.getGasPrice();
        } catch (Exception e) {
            System.out.println("" + e);
        }
        return gasPrice;
    }

    private BigInteger getNonce() throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = mWeb3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }

    private BigInteger getGasPrice() {
        //return BigInteger.valueOf(Long.valueOf(mValueGasPrice));
        return getNetworkGasPrice();
    }

    private BigInteger getGasLimit() {
        return BigInteger.valueOf(Long.valueOf(mValueGasLimit));
    }

    public void Send(String toAddress, String valueAmmount) {
        new SendEthereum().execute(toAddress, valueAmmount);
    }

    private class SendEthereum extends AsyncTask<String, Void, EthSendTransaction> {

        @Override
        protected EthSendTransaction doInBackground(String... values) {
            BigInteger ammount = Convert.toWei(values[1], Convert.Unit.ETHER).toBigInteger();
            try {

                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(getNonce(), getGasPrice(), getGasLimit(), values[0], ammount);

                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, mChainID, mCredentials);
                String hexValue = "0x" + Hex.toHexString(signedMessage);
                myLog("ETHMSG", hexValue);
                myLog("ETHMSG", "Transaction hash" + mWeb3j.ethSendRawTransaction(hexValue).send().getTransactionHash());
                return null;//mWeb3j.ethSendRawTransaction(hexValue.toString()).sendAsync().get();

            } catch (ExecutionException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(EthSendTransaction result) {
            super.onPostExecute(result);
            //cbSendingEther.backSendEthereum(result);
        }
    }

    //  public void registerCallBack(CBSendingEther cbSendingEther){
    //      this.cbSendingEther = cbSendingEther;
    //  }

}