package com.itrustmachines.common.ethereum.service;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple5;

import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.contract.LedgerBooster;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class ClientContractService {
  
  private static final int MAX_RETRY_TIMES = 5;
  
  @Getter(AccessLevel.PACKAGE)
  private final EvmEnv evmEnv;
  
  private final LedgerBooster contract;
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl) {
    this(contractAddress, privateKey, nodeUrl, false, "", "");
  }
  
  public ClientContractService(@NonNull final String contractAddress, @NonNull final String privateKey,
      @NonNull final String nodeUrl, final Boolean nodeNeedAuth, final String nodeUserName, final String nodePassword) {
    this.evmEnv = EvmEnv.getInstance(nodeUrl, nodeNeedAuth, nodeUserName, nodePassword);
    this.contract = LedgerBooster.load(contractAddress, evmEnv.getWeb3j(), Credentials.create(privateKey),
        evmEnv.getContractGasProvider());
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public long obtainContractClearanceOrder() {
    return contract.clearanceOrder()
                   .send()
                   .longValue();
  }
  
  @SneakyThrows
  public ClearanceRecord obtainClearanceRecord(long clearanceOrder) {
    log.debug("obtainClearanceRecord() start clearanceOrder={}", clearanceOrder);
    ClearanceRecord result = getClearanceRecordFromContract(clearanceOrder);
    log.debug("obtainClearanceRecord() clearanceOrder={}, result={}", clearanceOrder, result);
    return result;
  }
  
  @SneakyThrows
  private ClearanceRecord getClearanceRecordFromContract(long clearanceOrder) {
    ClearanceRecord result = null;
    for (int retryCount = 1; retryCount <= MAX_RETRY_TIMES; retryCount++) {
      log.debug("getClearanceRecordFromContract() retryCount={}", retryCount);
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
          log.error("getClearanceRecordFromContract() fail", e);
          throw e;
        }
      }
    }
    log.debug("getClearanceRecordFromContract() result={}", result);
    return result;
  }
  
}
