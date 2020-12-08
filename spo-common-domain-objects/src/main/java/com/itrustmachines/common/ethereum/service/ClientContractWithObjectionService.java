package com.itrustmachines.common.ethereum.service;

import static com.itrustmachines.common.util.PBPairUtil.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.utils.Numeric;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.contract.LedgerBoosterWithObjection;
import com.itrustmachines.common.tpm.Slice;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class ClientContractWithObjectionService {
  
  private static final int MAX_RETRY_TIMES = 5;
  
  @Getter(AccessLevel.PACKAGE)
  private final EvmEnv evmEnv;
  
  private final LedgerBoosterWithObjection contract;
  private final Cache<Long, ClearanceRecord> clearanceRecordCache;
  private final ReentrantLock lock;
  private final int retryDelaySec;
  
  public ClientContractWithObjectionService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final double gasPriceMultiply, final int retryDelaySec) {
    this(contractAddress, privateKey, nodeUrl, gasPriceMultiply, false, "", "", retryDelaySec);
  }
  
  public ClientContractWithObjectionService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final double gasPriceMultiply, final Boolean nodeNeedAuth,
      final String nodeUserName, final String nodePassword, final int retryDelaySec) {
    this(contractAddress, privateKey,
        EvmEnv.getInstance(nodeUrl, gasPriceMultiply, nodeNeedAuth, nodeUserName, nodePassword), retryDelaySec);
  }
  
  public ClientContractWithObjectionService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final EvmEnv evmEnv, final int retryDelaySec) {
    this.evmEnv = evmEnv;
    this.contract = LedgerBoosterWithObjection.load(contractAddress, evmEnv.getWeb3j(), Credentials.create(privateKey),
        evmEnv.getContractGasProvider());
    this.clearanceRecordCache = CacheBuilder.newBuilder()
                                            .maximumSize(10)
                                            .build();
    this.lock = new ReentrantLock();
    this.retryDelaySec = retryDelaySec;
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public String getContractVersion() {
    return contract.version()
                   .send();
  }
  
  @SneakyThrows
  public long obtainContractClearanceOrder() {
    return contract.clearanceOrder()
                   .send()
                   .longValue();
  }
  
  @SneakyThrows
  public ClearanceRecord obtainClearanceRecord(final long clearanceOrder) {
    lock.lock();
    try {
      log.debug("obtainClearanceRecord() start, CO={}", clearanceOrder);
      ClearanceRecord result = clearanceRecordCache.getIfPresent(clearanceOrder);
      if (result == null) {
        result = getClearanceRecordFromContract(clearanceOrder);
      }
      log.debug("obtainClearanceRecord() CO={}, result={}", clearanceOrder, result);
      return result;
    } finally {
      lock.unlock();
    }
  }
  
  @SneakyThrows
  private ClearanceRecord getClearanceRecordFromContract(final long clearanceOrder) {
    ClearanceRecord result = null;
    for (int retryCount = 1; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("getClearanceRecordFromContract() CO={}, retryCount={}", clearanceOrder, retryCount);
      try {
        final Tuple5<BigInteger, byte[], BigInteger, byte[], String> crTuple5 = contract.clearanceRecords(
            BigInteger.valueOf(clearanceOrder))
                                                                                        .send();
        // TODO add txHash
        final String description = crTuple5.component5();
        if (StringUtils.isNotBlank(description)) {
          result = ClearanceRecord.builder()
                                  .clearanceOrder(crTuple5.component1()
                                                          .longValue())
                                  .rootHash(HashUtils.byte2hex(crTuple5.component2()))
                                  .createTime(crTuple5.component3()
                                                      .longValue())
                                  .chainHash(HashUtils.byte2hex(crTuple5.component4()))
                                  .description(description)
                                  .build();
          break;
        }
      } catch (Exception e) {
        if (retryCount == MAX_RETRY_TIMES) {
          log.error("getClearanceRecordFromContract() fail, CO={}", clearanceOrder, e);
          throw e;
        }
        TimeUnit.SECONDS.sleep(retryDelaySec);
      }
    }
    if (result != null) {
      clearanceRecordCache.put(clearanceOrder, result);
    }
    log.debug("getClearanceRecordFromContract() CO={}, result={}", clearanceOrder, result);
    return result;
  }
  
  public TransactionReceipt objection(@NonNull final Receipt receipt, @NonNull final MerkleProof merkleProof) {
    final List<byte[]> receiptSignature = toBytesSignature(receipt.getSigServer());
    
    final List<byte[]> merkleProofSignature = toBytesSignature(merkleProof.getSigServer());
    final Slice slice = Slice.fromString(merkleProof.getSlice());
    final List<byte[]> sliceList = slice.getInterNodes()
                                        .stream()
                                        .map(HashUtils::hex2byte)
                                        .collect(Collectors.toList());
    
    final List<byte[]> indexAndClearanceOrderList = toIndexAndClearanceOrderList(slice.getIndex(),
        merkleProof.getClearanceOrder());
    
    final List<byte[]> pbPairIndexList = getPbPairIndex(merkleProof.getPbPair());
    final List<byte[]> pbPairKeyList = getPBPairKey(merkleProof.getPbPair());
    final List<byte[]> pbPairValueList = getPBPairValue(merkleProof.getPbPair());
    
    TransactionReceipt transactionReceipt = null;
    
    try {
      transactionReceipt = contract.objection(receipt.getIndexValue(), "" + receipt.getClearanceOrder(),
          receipt.getSecondPart(), receiptSignature, indexAndClearanceOrderList, sliceList, pbPairIndexList,
          pbPairKeyList, pbPairValueList, merkleProofSignature)
                                   .send();
    } catch (Exception e) {
      log.error("objection() error", e);
    }
    
    return transactionReceipt;
    
  }
  
  List<byte[]> toBytesSignature(SpoSignature spoSignature) {
    log.debug("toBytesSignature() spoSignature={}", spoSignature);
    final List<byte[]> result = new ArrayList<>();
    result.add(Numeric.toBytesPadded(new BigInteger(HashUtils.hex2byte(spoSignature.getV())), 32));
    result.add(HashUtils.hex2byte(spoSignature.getR()));
    result.add(HashUtils.hex2byte(spoSignature.getS()));
    log.debug("toBytesSignature() result={}", result);
    return result;
  }
  
  List<byte[]> toIndexAndClearanceOrderList(int index, Long clearanceOrder) {
    log.debug("toIndexAndClearanceOrderList() index={}, clearanceOrder={}", index, clearanceOrder);
    List<byte[]> result = new ArrayList<>();
    
    result.add(Numeric.toBytesPadded(BigInteger.valueOf(index), 32));
    result.add(Numeric.toBytesPadded(BigInteger.valueOf(clearanceOrder), 32));
    log.debug("toIndexAndClearanceOrderList() result[0]={}, result[1]={}", ByteArrayUtil.toHexString(result.get(0)),
        ByteArrayUtil.toHexString(result.get(1)));
    return result;
  }
}
