package com.itrustmachines.common.ethereum.contract;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.6.4.
 */
@SuppressWarnings("rawtypes")
public class LedgerBooster extends Contract {
    public static final String BINARY = "60c0604052600d60808190526c322e312e302e52454c4541534560981b60a090815262000030916000919062000195565b503480156200003e57600080fd5b50604051620016d2380380620016d2833981810160405260608110156200006457600080fd5b508051602080830151604093840151600180546001600160a01b038087166001600160a01b03199283161783556002805491861691909216179055600355600080805260048085526103e842027f17ef568e3e12ab5b9c7254a8d58478811de00f9e6eb34345acd53bf8fd09d3ee5586516524aa26afa62160d11b818701528751808203600601815260268201808a528151918801919091207f17ef568e3e12ab5b9c7254a8d58478811de00f9e6eb34345acd53bf8fd09d3ef5560668201909852601288527124aa26afa622a223a2a92fa127a7a9aa22a960711b6046909101908152918052909352935192939092909162000184917f17ef568e3e12ab5b9c7254a8d58478811de00f9e6eb34345acd53bf8fd09d3f0919062000195565b50600655505060006007556200023a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001d857805160ff191683800117855562000208565b8280016001018555821562000208579182015b8281111562000208578251825591602001919060010190620001eb565b50620002169291506200021a565b5090565b6200023791905b8082111562000216576000815560010162000221565b90565b611488806200024a6000396000f3fe608060405234801561001057600080fd5b50600436106100cf5760003560e01c806354fd4d501161008c5780636b30ad23116100665780636b30ad2314610b13578063d1e5b9e514610bc4578063ddcb538f14610bcc578063e3bd995014610bf2576100cf565b806354fd4d501461095e578063624d3d12146109db57806362e0645914610b0b576100cf565b806317a04078146100d45780631a706f8e146100f8578063211c22be1461053f57806328e8e220146105595780633c54068714610610578063449dd88f14610618575b600080fd5b6100dc610c0f565b604080516001600160a01b039092168252519081900360200190f35b61053d600480360361010081101561010f57600080fd5b810190602081018135600160201b81111561012957600080fd5b82018360208201111561013b57600080fd5b803590602001918460018302840111600160201b8311171561015c57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b8111156101ae57600080fd5b8201836020820111156101c057600080fd5b803590602001918460018302840111600160201b831117156101e157600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b81111561023357600080fd5b82018360208201111561024557600080fd5b803590602001918460018302840111600160201b8311171561026657600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b8111156102b857600080fd5b8201836020820111156102ca57600080fd5b803590602001918460018302840111600160201b831117156102eb57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b81111561033d57600080fd5b82018360208201111561034f57600080fd5b803590602001918460018302840111600160201b8311171561037057600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b8111156103c257600080fd5b8201836020820111156103d457600080fd5b803590602001918460018302840111600160201b831117156103f557600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b81111561044757600080fd5b82018360208201111561045957600080fd5b803590602001918460018302840111600160201b8311171561047a57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295949360208101935035915050600160201b8111156104cc57600080fd5b8201836020820111156104de57600080fd5b803590602001918460208302840111600160201b831117156104ff57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600092019190915250929550610c1e945050505050565b005b610547610c28565b60408051918252519081900360200190f35b61053d6004803603608081101561056f57600080fd5b81359160208101359160408201359190810190608081016060820135600160201b81111561059c57600080fd5b8201836020820111156105ae57600080fd5b803590602001918460018302840111600160201b831117156105cf57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250929550610c2e945050505050565b610547610f5b565b61094a600480360360e081101561062e57600080fd5b81359190810190604081016020820135600160201b81111561064f57600080fd5b82018360208201111561066157600080fd5b803590602001918460208302840111600160201b8311171561068257600080fd5b9190808060200260200160405190810160405280939291908181526020018383602002808284376000920191909152509295949360208101935035915050600160201b8111156106d157600080fd5b8201836020820111156106e357600080fd5b803590602001918460208302840111600160201b8311171561070457600080fd5b9190808060200260200160405190810160405280939291908181526020018383602002808284376000920191909152509295949360208101935035915050600160201b81111561075357600080fd5b82018360208201111561076557600080fd5b803590602001918460208302840111600160201b8311171561078657600080fd5b9190808060200260200160405190810160405280939291908181526020018383602002808284376000920191909152509295949360208101935035915050600160201b8111156107d557600080fd5b8201836020820111156107e757600080fd5b803590602001918460208302840111600160201b8311171561080857600080fd5b9190808060200260200160405190810160405280939291908181526020018383602002808284376000920191909152509295949360208101935035915050600160201b81111561085757600080fd5b82018360208201111561086957600080fd5b803590602001918460208302840111600160201b8311171561088a57600080fd5b9190808060200260200160405190810160405280939291908181526020018383602002808284376000920191909152509295949360208101935035915050600160201b8111156108d957600080fd5b8201836020820111156108eb57600080fd5b803590602001918460208302840111600160201b8311171561090c57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600092019190915250929550610f61945050505050565b604080519115158252519081900360200190f35b610966610f6e565b6040805160208082528351818301528351919283929083019185019080838360005b838110156109a0578181015183820152602001610988565b50505050905090810190601f1680156109cd5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6109f8600480360360208110156109f157600080fd5b5035610ffc565b60405180866001600160a01b03166001600160a01b031681526020018581526020018060200180602001846006811115610a2e57fe5b60ff168152602001838103835286818151815260200191508051906020019080838360005b83811015610a6b578181015183820152602001610a53565b50505050905090810190601f168015610a985780820380516001836020036101000a031916815260200191505b50838103825285518152855160209182019187019080838360005b83811015610acb578181015183820152602001610ab3565b50505050905090810190601f168015610af85780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b61054761114b565b610b3060048036036020811015610b2957600080fd5b5035611151565b6040518086815260200185815260200184815260200183815260200180602001828103825283818151815260200191508051906020019080838360005b83811015610b85578181015183820152602001610b6d565b50505050905090810190601f168015610bb25780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390f35b6100dc61121f565b61053d60048036036020811015610be257600080fd5b50356001600160a01b031661122e565b61053d60048036036020811015610c0857600080fd5b50356112aa565b6002546001600160a01b031681565b5050505050505050565b60065481565b6002546001600160a01b03163314610c8d576040805162461bcd60e51b815260206004820152601e60248201527f73706f53657276657257616c6c65744164647265737320696e76616c69640000604482015290519081900360640190fd5b6003548414610ce3576040805162461bcd60e51b815260206004820152601760248201527f5f636c656172616e63654f7264657220696e76616c6964000000000000000000604482015290519081900360640190fd5b60008211610d2b576040805162461bcd60e51b815260206004820152601060248201526f17dd1e10dbdd5b9d081a5b9d985b1a5960821b604482015290519081900360640190fd5b8051610d75576040805162461bcd60e51b815260206004820152601460248201527317d9195cd8dc9a5c1d1a5bdb881a5b9d985b1a5960621b604482015290519081900360640190fd5b610d8160075483611357565b60078190556006541015610dd1576040805162461bcd60e51b81526020600482015260126024820152711b585e151e10dbdd5b9d081a5b9d985b1a5960721b604482015290519081900360640190fd5b600380546000198101600090815260046020818152604080842086015481518084018b905280830187905260608082019290925282518082039092018252608081018084528251928501929092206101208201845287835260a082018c81526103e8420260c0840181815260e085018481526101009095018d81529a8a528888529590982084518155905160018201559351600285015590519783019790975594518051949695949193610e8b93908501929101906113b8565b509050507f58859c73c7901b793059ede08d0791a182675f86d81dcc63dbae70bebfa2adf0600354868484876040518086815260200185815260200184815260200183815260200180602001828103825283818151815260200191508051906020019080838360005b83811015610f0c578181015183820152602001610ef4565b50505050905090810190601f168015610f395780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390a1505060038054600101905550505050565b60075481565b6000979650505050505050565b6000805460408051602060026001851615610100026000190190941693909304601f81018490048402820184019092528181529291830182828015610ff45780601f10610fc957610100808354040283529160200191610ff4565b820191906000526020600020905b815481529060010190602001808311610fd757829003601f168201915b505050505081565b60056020908152600091825260409182902080546001808301546002808501805488516101009582161595909502600019011691909104601f81018790048702840187019097528683526001600160a01b039093169590949192918301828280156110a85780601f1061107d576101008083540402835291602001916110a8565b820191906000526020600020905b81548152906001019060200180831161108b57829003601f168201915b5050505060038301805460408051602060026001851615610100026000190190941693909304601f81018490048402820184019092528181529495949350908301828280156111385780601f1061110d57610100808354040283529160200191611138565b820191906000526020600020905b81548152906001019060200180831161111b57829003601f168201915b5050506004909301549192505060ff1685565b60035481565b6004602052806000526040600020600091509050806000015490806001015490806002015490806003015490806004018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156112155780601f106111ea57610100808354040283529160200191611215565b820191906000526020600020905b8154815290600101906020018083116111f857829003601f168201915b5050505050905085565b6001546001600160a01b031681565b6001546001600160a01b03163314611288576040805162461bcd60e51b81526020600482015260186024820152771a5d1b55d85b1b195d1059191c995cdcc81a5b9d985b1a5960421b604482015290519081900360640190fd5b600280546001600160a01b0319166001600160a01b0392909216919091179055565b6001546001600160a01b03163314611304576040805162461bcd60e51b81526020600482015260186024820152771a5d1b55d85b1b195d1059191c995cdcc81a5b9d985b1a5960421b604482015290519081900360640190fd5b61131060065482611357565b600681905560075460408051918252602082019290925281517f96697f8280ef45ceeb3b8ce37058b4f2956f5b2ce25f7fbcbaa1d5aebd9eab92929181900390910190a150565b6000828201838110156113b1576040805162461bcd60e51b815260206004820152601b60248201527f536166654d6174683a206164646974696f6e206f766572666c6f770000000000604482015290519081900360640190fd5b9392505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106113f957805160ff1916838001178555611426565b82800160010185558215611426579182015b8281111561142657825182559160200191906001019061140b565b50611432929150611436565b5090565b61145091905b80821115611432576000815560010161143c565b9056fea265627a7a72315820f55896ed2bedc122eb0cd54d9cb31285b74cf53914741b0d106f1af5c02220ee64736f6c63430005100032";

    public static final String FUNC_ADDMAXTXCOUNT = "addMaxTxCount";

    public static final String FUNC_CHANGESPOSERVERWALLETADDRESS = "changeSpoServerWalletAddress";

    public static final String FUNC_CLEARANCEORDER = "clearanceOrder";

    public static final String FUNC_CLEARANCERECORDS = "clearanceRecords";

    public static final String FUNC_ITMWALLETADDRESS = "itmWalletAddress";

    public static final String FUNC_MAXTXCOUNT = "maxTxCount";

    public static final String FUNC_OBJECTIONMERKLEPROOF = "objectionMerkleProof";

    public static final String FUNC_OBJECTIONRECEIPT = "objectionReceipt";

    public static final String FUNC_OBJECTIONRECORDS = "objectionRecords";

    public static final String FUNC_SPOSERVERWALLETADDRESS = "spoServerWalletAddress";

    public static final String FUNC_TXCOUNT = "txCount";

    public static final String FUNC_VERSION = "version";

    public static final String FUNC_WRITECLEARANCERECORD = "writeClearanceRecord";

    public static final Event ADDMAXTXCOUNTEVENT_EVENT = new Event("addMaxTxCountEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WRITECLEARANCERECORDEVENT_EVENT = new Event("writeClearanceRecordEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}));
    ;

    @Deprecated
    protected LedgerBooster(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected LedgerBooster(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected LedgerBooster(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected LedgerBooster(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<AddMaxTxCountEventEventResponse> getAddMaxTxCountEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ADDMAXTXCOUNTEVENT_EVENT, transactionReceipt);
        ArrayList<AddMaxTxCountEventEventResponse> responses = new ArrayList<AddMaxTxCountEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AddMaxTxCountEventEventResponse typedResponse = new AddMaxTxCountEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.txCount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.maxTxCount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<AddMaxTxCountEventEventResponse> addMaxTxCountEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, AddMaxTxCountEventEventResponse>() {
            @Override
            public AddMaxTxCountEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ADDMAXTXCOUNTEVENT_EVENT, log);
                AddMaxTxCountEventEventResponse typedResponse = new AddMaxTxCountEventEventResponse();
                typedResponse.log = log;
                typedResponse.txCount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.maxTxCount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<AddMaxTxCountEventEventResponse> addMaxTxCountEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ADDMAXTXCOUNTEVENT_EVENT));
        return addMaxTxCountEventEventFlowable(filter);
    }

    public List<WriteClearanceRecordEventEventResponse> getWriteClearanceRecordEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WRITECLEARANCERECORDEVENT_EVENT, transactionReceipt);
        ArrayList<WriteClearanceRecordEventEventResponse> responses = new ArrayList<WriteClearanceRecordEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WriteClearanceRecordEventEventResponse typedResponse = new WriteClearanceRecordEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.clearanceOrder = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.rootHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.createTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.chainHash = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.description = (String) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<WriteClearanceRecordEventEventResponse> writeClearanceRecordEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, WriteClearanceRecordEventEventResponse>() {
            @Override
            public WriteClearanceRecordEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WRITECLEARANCERECORDEVENT_EVENT, log);
                WriteClearanceRecordEventEventResponse typedResponse = new WriteClearanceRecordEventEventResponse();
                typedResponse.log = log;
                typedResponse.clearanceOrder = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.rootHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.createTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.chainHash = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.description = (String) eventValues.getNonIndexedValues().get(4).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<WriteClearanceRecordEventEventResponse> writeClearanceRecordEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WRITECLEARANCERECORDEVENT_EVENT));
        return writeClearanceRecordEventEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> addMaxTxCount(BigInteger _maxTxcount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDMAXTXCOUNT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_maxTxcount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> changeSpoServerWalletAddress(String _spoServerWalletAddress) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CHANGESPOSERVERWALLETADDRESS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _spoServerWalletAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> clearanceOrder() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CLEARANCEORDER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple5<BigInteger, byte[], BigInteger, byte[], String>> clearanceRecords(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CLEARANCERECORDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple5<BigInteger, byte[], BigInteger, byte[], String>>(function,
                new Callable<Tuple5<BigInteger, byte[], BigInteger, byte[], String>>() {
                    @Override
                    public Tuple5<BigInteger, byte[], BigInteger, byte[], String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<BigInteger, byte[], BigInteger, byte[], String>(
                                (BigInteger) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (byte[]) results.get(3).getValue(), 
                                (String) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> itmWalletAddress() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ITMWALLETADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> maxTxCount() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_MAXTXCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> objectionMerkleProof(byte[] receiptHash, List<byte[]> indexAndClearnaceOrder, List<byte[]> _slice, List<byte[]> _pbPairIndex, List<byte[]> _pbPbpairKey, List<byte[]> _pbpairValue, List<byte[]> signature) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_OBJECTIONMERKLEPROOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(receiptHash), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(indexAndClearnaceOrder, org.web3j.abi.datatypes.generated.Bytes32.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(_slice, org.web3j.abi.datatypes.generated.Bytes32.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes8>(
                        org.web3j.abi.datatypes.generated.Bytes8.class,
                        org.web3j.abi.Utils.typeMap(_pbPairIndex, org.web3j.abi.datatypes.generated.Bytes8.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(_pbPbpairKey, org.web3j.abi.datatypes.generated.Bytes32.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(_pbpairValue, org.web3j.abi.datatypes.generated.Bytes32.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(signature, org.web3j.abi.datatypes.generated.Bytes32.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> objectionReceipt(String beforeIndexValue, String indexValue, String metaData, String co, String timestampSPO, String result, String afterResult, List<byte[]> signature) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_OBJECTIONRECEIPT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(beforeIndexValue), 
                new org.web3j.abi.datatypes.Utf8String(indexValue), 
                new org.web3j.abi.datatypes.Utf8String(metaData), 
                new org.web3j.abi.datatypes.Utf8String(co), 
                new org.web3j.abi.datatypes.Utf8String(timestampSPO), 
                new org.web3j.abi.datatypes.Utf8String(result), 
                new org.web3j.abi.datatypes.Utf8String(afterResult), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.datatypes.generated.Bytes32.class,
                        org.web3j.abi.Utils.typeMap(signature, org.web3j.abi.datatypes.generated.Bytes32.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple5<String, byte[], String, String, BigInteger>> objectionRecords(byte[] param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OBJECTIONRECORDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint8>() {}));
        return new RemoteFunctionCall<Tuple5<String, byte[], String, String, BigInteger>>(function,
                new Callable<Tuple5<String, byte[], String, String, BigInteger>>() {
                    @Override
                    public Tuple5<String, byte[], String, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, byte[], String, String, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> spoServerWalletAddress() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SPOSERVERWALLETADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> txCount() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TXCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> version() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_VERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> writeClearanceRecord(BigInteger _clearanceOrder, byte[] _rootHash, BigInteger _txCount, String _description) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WRITECLEARANCERECORD, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_clearanceOrder), 
                new org.web3j.abi.datatypes.generated.Bytes32(_rootHash), 
                new org.web3j.abi.datatypes.generated.Uint256(_txCount), 
                new org.web3j.abi.datatypes.Utf8String(_description)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static LedgerBooster load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new LedgerBooster(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static LedgerBooster load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new LedgerBooster(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static LedgerBooster load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new LedgerBooster(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static LedgerBooster load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new LedgerBooster(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<LedgerBooster> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _itmWalletAddress, String _spoServerWalletAddress, BigInteger _maxTxCount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _itmWalletAddress), 
                new org.web3j.abi.datatypes.Address(160, _spoServerWalletAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxTxCount)));
        return deployRemoteCall(LedgerBooster.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<LedgerBooster> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _itmWalletAddress, String _spoServerWalletAddress, BigInteger _maxTxCount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _itmWalletAddress), 
                new org.web3j.abi.datatypes.Address(160, _spoServerWalletAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxTxCount)));
        return deployRemoteCall(LedgerBooster.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<LedgerBooster> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _itmWalletAddress, String _spoServerWalletAddress, BigInteger _maxTxCount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _itmWalletAddress), 
                new org.web3j.abi.datatypes.Address(160, _spoServerWalletAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxTxCount)));
        return deployRemoteCall(LedgerBooster.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<LedgerBooster> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _itmWalletAddress, String _spoServerWalletAddress, BigInteger _maxTxCount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _itmWalletAddress), 
                new org.web3j.abi.datatypes.Address(160, _spoServerWalletAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxTxCount)));
        return deployRemoteCall(LedgerBooster.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class AddMaxTxCountEventEventResponse extends BaseEventResponse {
        public BigInteger txCount;

        public BigInteger maxTxCount;
    }

    public static class WriteClearanceRecordEventEventResponse extends BaseEventResponse {
        public BigInteger clearanceOrder;

        public byte[] rootHash;

        public BigInteger createTime;

        public byte[] chainHash;

        public String description;
    }
}
