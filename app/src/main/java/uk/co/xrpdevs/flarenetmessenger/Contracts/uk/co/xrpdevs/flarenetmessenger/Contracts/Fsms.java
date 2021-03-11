package uk.co.xrpdevs.flarenetmessenger.Contracts;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.0.1.
 */
public class Fsms extends Contract {
    private static final String BINARY = "6080604052600060075534801561001557600080fd5b5061001e610035565b600c80546001600160a01b031916331790556100c6565b3360009081526001602052604090205460ff166100af576100546100b1565b3360008181526001602081905260408220805460ff191682179055600d805491820181559091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb50180546001600160a01b03191690911790555b565b33600090815260208190526040812060010155565b610cd9806100d56000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c80636f6fc077116100665780636f6fc077146100de578063857cdbb8146100f157806389d45b9114610111578063a544f67f14610133578063de6f24bb1461014957610093565b8063056e7e39146100985780634d3820eb146100a25780636252194d146100aa5780636e9a215e146100c8575b600080fd5b6100a061015c565b005b6100a0610171565b6100b26101ed565b6040516100bf9190610c77565b60405180910390f35b6100d0610203565b6040516100bf929190610ba0565b6100a06100ec366004610a8d565b61027c565b6101046100ff3660046109c4565b61029b565b6040516100bf9190610c82565b61012461011f366004610afa565b610345565b6040516100bf93929190610bcc565b61013b6105c3565b6040516100bf929190610c95565b6100a06101573660046109e5565b6105de565b33600090815260208190526040812060010155565b3360009081526001602052604090205460ff166101eb5761019061015c565b3360008181526001602081905260408220805460ff191682179055600d805491820181559091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb50180546001600160a01b03191690911790555b565b3360009081526001602052604090205460ff1690565b600c54600d8054604080516020808402820181019092528281526000946060946001600160a01b0390911693909283919083018282801561026d57602002820191906000526020600020905b81546001600160a01b0316815260019091019060200180831161024f575b50505050509050915091509091565b3360009081526002602052604090206102969083836107d9565b505050565b6001600160a01b038116600090815260026020818152604092839020805484516001821615610100026000190190911693909304601f810183900483028401830190945283835260609390918301828280156103385780601f1061030d57610100808354040283529160200191610338565b820191906000526020600020905b81548152906001019060200180831161031b57829003601f168201915b505050505090505b919050565b33600090815260208190526040812060609182918291806103646105c3565b9150915060608167ffffffffffffffff8111801561038157600080fd5b506040519080825280602002602001820160405280156103b557816020015b60608152602001906001900390816103a05790505b50905060608267ffffffffffffffff811180156103d157600080fd5b506040519080825280602002602001820160405280156103fb578160200160208202803683370190505b50905060608367ffffffffffffffff8111801561041757600080fd5b50604051908082528060200260200182016040528015610441578160200160208202803683370190505b50905060005b848110156105b357610457610865565b6000828152600389016020908152604091829020825160808101845281546001600160a01b03168152600180830180548651600261010094831615949094026000190190911692909204601f81018690048602830186019096528582529194929385810193919291908301828280156105115780601f106104e657610100808354040283529160200191610511565b820191906000526020600020905b8154815290600101906020018083116104f457829003601f168201915b50505091835250506002820154602082015260039091015460ff166040918201528101519091508c10156105aa57806020015185838151811061055057fe5b6020026020010181905250806000015184838151811061056c57fe5b60200260200101906001600160a01b031690816001600160a01b031681525050806040015183838151811061059d57fe5b6020026020010181815250505b50600101610447565b5099909850909650945050505050565b33600090815260208190526040902080546001909101549091565b80516105f1906009906020840190610899565b5042600a5560088054336001600160a01b03199182168117808455600091825260208281526040808420805485526002818101909352932080546001600160a01b0390931692909416919091178355600980549294939261066992600185810193926101009181161591909102600019011604610915565b50600282810154908201556003918201549101805460ff191660ff9283161790558154600190810183556001600160a01b03851660009081526020919091526040902054166106fe57600d80546001810182556000919091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb50180546001600160a01b0319166001600160a01b0385161790555b6001600160a01b0380841660009081526020818152604080832060018082015485526003820190935292206008805482546001600160a01b031916951694909417815560098054939493919261076d928482019291600260001991831615610100029190910190911604610915565b50600282810154908201556003918201549101805460ff191660ff9092169190911790556001818101805490910190556040516001600160a01b0385169033907f81eec519e5b8469dc32a9de8b2d9a1fa6b85fe3686c290dbfa5c6c8c25d2127590600090a350505050565b828054600181600116156101000203166002900490600052602060002090601f01602090048101928261080f5760008555610855565b82601f106108285782800160ff19823516178555610855565b82800160010185558215610855579182015b8281111561085557823582559160200191906001019061083a565b50610861929150610998565b5090565b604051806080016040528060006001600160a01b031681526020016060815260200160008152602001600060ff1681525090565b828054600181600116156101000203166002900490600052602060002090601f0160209004810192826108cf5760008555610855565b82601f106108e857805160ff1916838001178555610855565b82800160010185558215610855579182015b828111156108555782518255916020019190600101906108fa565b828054600181600116156101000203166002900490600052602060002090601f01602090048101928261094b5760008555610855565b82601f1061095c5780548555610855565b8280016001018555821561085557600052602060002091601f016020900482015b8281111561085557825482559160010191906001019061097d565b5b808211156108615760008155600101610999565b80356001600160a01b038116811461034057600080fd5b6000602082840312156109d5578081fd5b6109de826109ad565b9392505050565b600080604083850312156109f7578081fd5b610a00836109ad565b915060208084013567ffffffffffffffff80821115610a1d578384fd5b818601915086601f830112610a30578384fd5b813581811115610a3c57fe5b604051601f8201601f1916810185018381118282101715610a5957fe5b6040528181528382018501891015610a6f578586fd5b81858501868301378585838301015280955050505050509250929050565b60008060208385031215610a9f578182fd5b823567ffffffffffffffff80821115610ab6578384fd5b818501915085601f830112610ac9578384fd5b813581811115610ad7578485fd5b866020828501011115610ae8578485fd5b60209290920196919550909350505050565b600060208284031215610b0b578081fd5b5035919050565b6000815180845260208085019450808401835b83811015610b4a5781516001600160a01b031687529582019590820190600101610b25565b509495945050505050565b60008151808452815b81811015610b7a57602081850181015186830182015201610b5e565b81811115610b8b5782602083870101525b50601f01601f19169290920160200192915050565b6001600160a01b0383168152604060208201819052600090610bc490830184610b12565b949350505050565b606080825284519082018190526000906020906080840190828801845b82811015610c0557815184529284019290840190600101610be9565b50505083810382850152610c198187610b12565b848103604086015285518082529091508282019083810283018401848801865b83811015610c6757601f19868403018552610c55838351610b55565b94870194925090860190600101610c39565b50909a9950505050505050505050565b901515815260200190565b6000602082526109de6020830184610b55565b91825260208201526040019056fea2646970667358221220f7c05eded3991f7c67da5a2e6b76998e5d7413d9cd2570fb7a91e34396add8b564736f6c63430007040033";

    public static final String FUNC_CHECKUSERREGISTRATION = "checkUserRegistration";

    public static final String FUNC_CLEARINBOX = "clearInbox";

    public static final String FUNC_GETCONTRACTPROPERTIES = "getContractProperties";

    public static final String FUNC_GETMYINBOXSIZE = "getMyInboxSize";

    public static final String FUNC_GETPUBLICKEY = "getPublicKey";

    public static final String FUNC_RECEIVEMESSAGES = "receiveMessages";

    public static final String FUNC_REGISTERUSER = "registerUser";

    public static final String FUNC_SENDMESSAGE = "sendMessage";

    public static final String FUNC_SETPUBLICKEY = "setPublicKey";

    public static final Event MESSAGENOTIFICATION_EVENT = new Event("messageNotification",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected Fsms(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Fsms(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Fsms(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Fsms(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<MessageNotificationEventResponse> getMessageNotificationEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MESSAGENOTIFICATION_EVENT, transactionReceipt);
        ArrayList<MessageNotificationEventResponse> responses = new ArrayList<MessageNotificationEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MessageNotificationEventResponse typedResponse = new MessageNotificationEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<MessageNotificationEventResponse> messageNotificationEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, MessageNotificationEventResponse>() {
            @Override
            public MessageNotificationEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MESSAGENOTIFICATION_EVENT, log);
                MessageNotificationEventResponse typedResponse = new MessageNotificationEventResponse();
                typedResponse.log = log;
                typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<MessageNotificationEventResponse> messageNotificationEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MESSAGENOTIFICATION_EVENT));
        return messageNotificationEventFlowable(filter);
    }

    public RemoteCall<TransactionReceipt> checkUserRegistration() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CHECKUSERREGISTRATION,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> clearInbox() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CLEARINBOX,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> getContractProperties() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETCONTRACTPROPERTIES,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> getMyInboxSize() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETMYINBOXSIZE,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> getPublicKey(String _address) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETPUBLICKEY,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_address)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> receiveMessages(BigInteger _fromTimeStamp) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RECEIVEMESSAGES,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_fromTimeStamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> registerUser() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REGISTERUSER,
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> sendMessage(String _receiver, String _content) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SENDMESSAGE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver),
                        new org.web3j.abi.datatypes.Utf8String(_content)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setPublicKey(String _pubkey) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETPUBLICKEY,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_pubkey)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Fsms load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Fsms(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Fsms load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Fsms(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Fsms load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Fsms(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Fsms load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Fsms(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Fsms> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Fsms.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Fsms> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Fsms.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Fsms> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Fsms.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Fsms> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Fsms.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class MessageNotificationEventResponse {
        public Log log;

        public String _from;

        public String _to;
    }
}
