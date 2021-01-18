package com.itrustmachines.verification.service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyClearanceRecordService {
  
  Map<Long, ClearanceRecord> buildClearanceRecordMap(@NonNull final List<ClearanceRecord> clearanceRecordList,
      @NonNull final ClearanceRecord latestCR) {
    log.debug("buildClearanceRecordMap() start, clearanceRecordListSize={}", clearanceRecordList.size());
    final Map<Long, ClearanceRecord> clearanceRecordMap = clearanceRecordList.stream()
                                                                             .filter(
                                                                                 clearanceRecord -> clearanceRecord.getClearanceOrder()
                                                                                                                   .compareTo(
                                                                                                                       latestCR.getClearanceOrder()) <= 0)
                                                                             .collect(Collectors.toMap(
                                                                                 ClearanceRecord::getClearanceOrder,
                                                                                 clearanceRecord -> clearanceRecord));
    log.debug("buildClearanceRecordMap() end, clearanceRecordMapKeySet={}", clearanceRecordMap.keySet());
    return clearanceRecordMap;
  }
  
  // key: clearanceOrder, value: VerifiedClearanceRecordInfo
  Map<Long, VerifiedClearanceRecordInfo> buildVerifiedClearanceRecordInfoMap(
      @NonNull final List<ClearanceRecord> crList, final @NonNull ClearanceRecord latestCR) {
    log.debug("buildVerifiedClearanceRecordMap() crList={}, latestCR={}", crList, latestCR);
    Map<Long, VerifiedClearanceRecordInfo> verifiedClearanceRecordInfoMap = new HashMap<>();
    Map<Long, ClearanceRecord> clearanceRecordMap = buildClearanceRecordMap(crList, latestCR);
    // verify latest cr
    boolean pass = false;
    Long currentCo = latestCR.getClearanceOrder();
    ClearanceRecord currentRecord = clearanceRecordMap.get(currentCo);
    if (verifyLatestClearanceRecord(currentRecord, latestCR)) {
      pass = true;
    } else {
      log.debug("buildVerifiedClearanceRecordMap() lastCRInClearanceRecordList error");
      currentRecord = latestCR;
    }
    clearanceRecordMap.remove(currentCo);
    verifiedClearanceRecordInfoMap.put(currentCo, VerifiedClearanceRecordInfo.builder()
                                                                             .pass(pass)
                                                                             .clearanceRecord(currentRecord)
                                                                             .build());
    
    if (crList.size() > 1) {
      currentCo--;
      while (!clearanceRecordMap.isEmpty()) {
        currentRecord = clearanceRecordMap.get(currentCo);
        final ClearanceRecord previousRecord = clearanceRecordMap.get(currentCo - 1);
        if (Objects.nonNull(currentRecord) && Objects.nonNull(previousRecord)) {
          // have previous cr
          pass = verifyChainHash(currentRecord, previousRecord);
        } else {
          // if currentRecord exist && previous not exist: latest
          pass = Objects.nonNull(currentRecord);
        }
        if (Objects.nonNull(currentRecord)) {
          clearanceRecordMap.remove(currentCo);
        }
        verifiedClearanceRecordInfoMap.put(currentCo, VerifiedClearanceRecordInfo.builder()
                                                                                 .pass(pass)
                                                                                 .clearanceRecord(currentRecord)
                                                                                 .build());
        currentCo--;
      }
    }
    
    log.debug("buildPassClearanceRecord() verifiedClearanceRecordInfoMap={}", verifiedClearanceRecordInfoMap);
    return verifiedClearanceRecordInfoMap;
  }
  
  boolean verifyLatestClearanceRecord(@NonNull final ClearanceRecord proofRecord,
      @NonNull final ClearanceRecord contractRecord) {
    log.debug("verifyLatestClearanceRecord() start, proofRecord={}, contractRecord={}", proofRecord, contractRecord);
    boolean result = false;
    if (Objects.nonNull(proofRecord) && proofRecord.getRootHash()
                                                   .equals(contractRecord.getRootHash())
        && proofRecord.getClearanceOrder()
                      .equals(contractRecord.getClearanceOrder())
        && proofRecord.getChainHash()
                      .equals(contractRecord.getChainHash())) {
      result = true;
    }
    log.debug("verifyLatestClearanceRecord() end, result={}", result);
    return result;
  }
  
  boolean verifyChainHash(@NonNull final ClearanceRecord currentRecord, @NonNull final ClearanceRecord previousRecord) {
    log.debug("verifyChainHash() start, currentRecord={}, previousRecord={}", currentRecord, previousRecord);
    
    /**
     * TODO: in 3.0.0.SNAPSHOT chainHash method = Chi = H(Ri-1|COi-1|CHi-1) RH 0 =
     * Hash.sha3("")
     */
    byte[] concatByteArray = concatByteArray(HashUtils.hex2byte(currentRecord.getRootHash()),
        Numeric.toBytesPadded(BigInteger.valueOf(currentRecord.getClearanceOrder()), 32));
    
    concatByteArray = concatByteArray(concatByteArray, HashUtils.hex2byte(previousRecord.getChainHash()));
    final byte[] clearanceRecordHash = Hash.sha3(concatByteArray);
    final boolean result = Arrays.equals(clearanceRecordHash, HashUtils.hex2byte(currentRecord.getChainHash()));
    log.debug("verifyChainHash() end, result={}", result);
    
    return result;
  }
  
  byte[] concatByteArray(@NonNull final byte[] previousByte, final @NonNull byte[] currentByte) {
    byte[] result = new byte[previousByte.length + currentByte.length];
    System.arraycopy(previousByte, 0, result, 0, previousByte.length);
    System.arraycopy(currentByte, 0, result, previousByte.length, currentByte.length);
    return result;
  }
}
