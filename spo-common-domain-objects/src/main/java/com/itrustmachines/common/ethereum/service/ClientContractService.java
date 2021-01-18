package com.itrustmachines.common.ethereum.service;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple5;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.contract.LedgerBooster;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class ClientContractService {
  
  @Getter(AccessLevel.PACKAGE)
  private final EvmEnv evmEnv;
  
  private final LedgerBooster contract;
  private final Cache<Long, ClearanceRecord> clearanceRecordCache;
  private final ReentrantLock lock;
  private final int retryDelaySec;
  private final int maxRetryTimes;
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final double gasPriceMultiply, final int retryDelaySec, final int maxRetryTimes) {
    this(contractAddress, privateKey, EvmEnv.getInstance(nodeUrl, gasPriceMultiply, false, "", ""), retryDelaySec,
        maxRetryTimes);
  }
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final double gasPriceMultiply, final int retryDelaySec) {
    this(contractAddress, privateKey, nodeUrl, gasPriceMultiply, false, "", "", retryDelaySec);
  }
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final double gasPriceMultiply, final Boolean nodeNeedAuth,
      final String nodeUserName, final String nodePassword, final int retryDelaySec) {
    this(contractAddress, privateKey,
        EvmEnv.getInstance(nodeUrl, gasPriceMultiply, nodeNeedAuth, nodeUserName, nodePassword), retryDelaySec, 5);
  }
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final EvmEnv evmEnv, final int retryDelaySec, final int maxRetryTimes) {
    this.evmEnv = evmEnv;
    this.contract = LedgerBooster.load(contractAddress, evmEnv.getWeb3j(), Credentials.create(privateKey),
        evmEnv.getContractGasProvider());
    this.clearanceRecordCache = CacheBuilder.newBuilder()
                                            .maximumSize(10)
                                            .build();
    this.lock = new ReentrantLock();
    this.retryDelaySec = retryDelaySec;
    this.maxRetryTimes = maxRetryTimes;
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
    for (int retryCount = 0; retryCount <= maxRetryTimes; retryCount++) {
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
        if (retryCount == maxRetryTimes) {
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
  
}
