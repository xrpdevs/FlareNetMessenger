package uk.co.xrpdevs.flarenetmessenger.contracts;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.4.
 */
@SuppressWarnings("rawtypes")
public class Fsms extends Contract {
    public static final String BINARY = "6080604052600060075534801561001557600080fd5b5061002764010000000061003e810204565b600c8054600160a060020a031916331790556100d8565b3360009081526001602052604090205460ff166100c1576100666401000000006100c3810204565b3360008181526001602081905260408220805460ff191682179055600d805491820181559091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb5018054600160a060020a03191690911790555b565b33600090815260208190526040812060010155565b610d26806100e76000396000f3fe608060405234801561001057600080fd5b50600436106100b0576000357c0100000000000000000000000000000000000000000000000000000000900480636f6fc077116100835780636f6fc077146100fb578063857cdbb81461010e57806389d45b911461012e578063a544f67f14610150578063de6f24bb14610166576100b0565b8063056e7e39146100b55780634d3820eb146100bf5780636252194d146100c75780636e9a215e146100e5575b600080fd5b6100bd610179565b005b6100bd61018e565b6100cf610217565b6040516100dc9190610cc4565b60405180910390f35b6100ed61022d565b6040516100dc929190610bef565b6100bd610109366004610adc565b6102a6565b61012161011c366004610a13565b6102c5565b6040516100dc9190610ccf565b61014161013c366004610b49565b61036f565b6040516100dc93929190610c19565b6101586105ed565b6040516100dc929190610ce2565b6100bd610174366004610a34565b610608565b33600090815260208190526040812060010155565b3360009081526001602052604090205460ff16610215576101ad610179565b3360008181526001602081905260408220805460ff191682179055600d805491820181559091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb501805473ffffffffffffffffffffffffffffffffffffffff191690911790555b565b3360009081526001602052604090205460ff1690565b600c54600d805460408051602080840282018101909252828152600094606094600160a060020a0390911693909283919083018282801561029757602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311610279575b50505050509050915091509091565b3360009081526002602052604090206102c0908383610828565b505050565b600160a060020a038116600090815260026020818152604092839020805484516001821615610100026000190190911693909304601f810183900483028401830190945283835260609390918301828280156103625780601f1061033757610100808354040283529160200191610362565b820191906000526020600020905b81548152906001019060200180831161034557829003601f168201915b505050505090505b919050565b336000908152602081905260408120606091829182918061038e6105ed565b9150915060608167ffffffffffffffff811180156103ab57600080fd5b506040519080825280602002602001820160405280156103df57816020015b60608152602001906001900390816103ca5790505b50905060608267ffffffffffffffff811180156103fb57600080fd5b50604051908082528060200260200182016040528015610425578160200160208202803683370190505b50905060608367ffffffffffffffff8111801561044157600080fd5b5060405190808252806020026020018201604052801561046b578160200160208202803683370190505b50905060005b848110156105dd576104816108b4565b600082815260038901602090815260409182902082516080810184528154600160a060020a03168152600180830180548651600261010094831615949094026000190190911692909204601f810186900486028301860190965285825291949293858101939192919083018282801561053b5780601f106105105761010080835404028352916020019161053b565b820191906000526020600020905b81548152906001019060200180831161051e57829003601f168201915b50505091835250506002820154602082015260039091015460ff166040918201528101519091508c10156105d457806020015185838151811061057a57fe5b6020026020010181905250806000015184838151811061059657fe5b6020026020010190600160a060020a03169081600160a060020a03168152505080604001518383815181106105c757fe5b6020026020010181815250505b50600101610471565b5099909850909650945050505050565b33600090815260208190526040902080546001909101549091565b805161061b9060099060208401906108e8565b5042600a55600880543373ffffffffffffffffffffffffffffffffffffffff19918216811780845560009182526020828152604080842080548552600281810190935293208054600160a060020a039093169290941691909117835560098054929493926106a092600185810193926101009181161591909102600019011604610964565b50600282810154908201556003918201549101805460ff191660ff928316179055815460019081018355600160a060020a038516600090815260209190915260409020541661074257600d80546001810182556000919091527fd7b6990105719101dabeb77144f2a3385c8033acd3af97e9423a695e81ad1eb501805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0385161790555b600160a060020a03808416600090815260208181526040808320600180820154855260038201909352922060088054825473ffffffffffffffffffffffffffffffffffffffff191695169490941781556009805493949391926107be928482019291600260001991831615610100029190910190911604610964565b50600282810154908201556003918201549101805460ff191660ff909216919091179055600181810180549091019055604051600160a060020a038516907f58b159f47e4df18cf759ba6efa13f041e2419f668953f879b7a116ed151ae4e890600090a250505050565b828054600181600116156101000203166002900490600052602060002090601f01602090048101928261085e57600085556108a4565b82601f106108775782800160ff198235161785556108a4565b828001600101855582156108a4579182015b828111156108a4578235825591602001919060010190610889565b506108b09291506109e7565b5090565b60405180608001604052806000600160a060020a031681526020016060815260200160008152602001600060ff1681525090565b828054600181600116156101000203166002900490600052602060002090601f01602090048101928261091e57600085556108a4565b82601f1061093757805160ff19168380011785556108a4565b828001600101855582156108a4579182015b828111156108a4578251825591602001919060010190610949565b828054600181600116156101000203166002900490600052602060002090601f01602090048101928261099a57600085556108a4565b82601f106109ab57805485556108a4565b828001600101855582156108a457600052602060002091601f016020900482015b828111156108a45782548255916001019190600101906109cc565b5b808211156108b057600081556001016109e8565b8035600160a060020a038116811461036a57600080fd5b600060208284031215610a24578081fd5b610a2d826109fc565b9392505050565b60008060408385031215610a46578081fd5b610a4f836109fc565b915060208084013567ffffffffffffffff80821115610a6c578384fd5b818601915086601f830112610a7f578384fd5b813581811115610a8b57fe5b604051601f8201601f1916810185018381118282101715610aa857fe5b6040528181528382018501891015610abe578586fd5b81858501868301378585838301015280955050505050509250929050565b60008060208385031215610aee578182fd5b823567ffffffffffffffff80821115610b05578384fd5b818501915085601f830112610b18578384fd5b813581811115610b26578485fd5b866020828501011115610b37578485fd5b60209290920196919550909350505050565b600060208284031215610b5a578081fd5b5035919050565b6000815180845260208085019450808401835b83811015610b99578151600160a060020a031687529582019590820190600101610b74565b509495945050505050565b60008151808452815b81811015610bc957602081850181015186830182015201610bad565b81811115610bda5782602083870101525b50601f01601f19169290920160200192915050565b6000600160a060020a038416825260406020830152610c116040830184610b61565b949350505050565b606080825284519082018190526000906020906080840190828801845b82811015610c5257815184529284019290840190600101610c36565b50505083810382850152610c668187610b61565b848103604086015285518082529091508282019083810283018401848801865b83811015610cb457601f19868403018552610ca2838351610ba4565b94870194925090860190600101610c86565b50909a9950505050505050505050565b901515815260200190565b600060208252610a2d6020830184610ba4565b91825260208201526040019056fea2646970667358221220a630d0235e22c77212d5ca397c266307d069ba5d6863bfa7bdfa566d896f462664736f6c63430007050033";

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
            Arrays.asList(new TypeReference<Address>(true) {
            }));

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
            typedResponse._to = (String) eventValues.getIndexedValues().get(0).getValue();
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
                typedResponse._to = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<MessageNotificationEventResponse> messageNotificationEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MESSAGENOTIFICATION_EVENT));
        return messageNotificationEventFlowable(filter);
    }

    public RemoteFunctionCall<Boolean> checkUserRegistration() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CHECKUSERREGISTRATION,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> clearInbox() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CLEARINBOX,
                Arrays.asList(),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<String, List<String>>> getContractProperties() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETCONTRACTPROPERTIES,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }));
        return new RemoteFunctionCall<Tuple2<String, List<String>>>(function,
                new Callable<Tuple2<String, List<String>>>() {
                    @Override
                    public Tuple2<String, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<String, List<String>>(
                                (String) results.get(0).getValue(),
                                convertToNative((List<Address>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, BigInteger>> getMyInboxSize() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETMYINBOXSIZE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }));
        return new RemoteFunctionCall<Tuple2<BigInteger, BigInteger>>(function,
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> getPublicKey(String _address) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETPUBLICKEY,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, _address)),
                Arrays.asList(new TypeReference<Utf8String>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple3<List<BigInteger>, List<String>, List<String>>> receiveMessages(BigInteger _fromTimeStamp) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_RECEIVEMESSAGES,
                Arrays.asList(new org.web3j.abi.datatypes.generated.Uint256(_fromTimeStamp)),
                Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {
                }, new TypeReference<DynamicArray<Address>>() {
                }, new TypeReference<DynamicArray<Utf8String>>() {
                }));
        return new RemoteFunctionCall<Tuple3<List<BigInteger>, List<String>, List<String>>>(function,
                new Callable<Tuple3<List<BigInteger>, List<String>, List<String>>>() {
                    @Override
                    public Tuple3<List<BigInteger>, List<String>, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<List<BigInteger>, List<String>, List<String>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()),
                                convertToNative((List<Address>) results.get(1).getValue()),
                                convertToNative((List<Utf8String>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> registerUser() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REGISTERUSER,
                Arrays.asList(),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> sendMessage(String _receiver, String _content) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SENDMESSAGE,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, _receiver),
                        new org.web3j.abi.datatypes.Utf8String(_content)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPublicKey(String _pubkey) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETPUBLICKEY,
                Arrays.asList(new org.web3j.abi.datatypes.Utf8String(_pubkey)),
                Collections.emptyList());
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

    public static class MessageNotificationEventResponse extends BaseEventResponse {
        public String _to;
    }
}