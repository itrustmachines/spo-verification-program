package com.itrustmachines.verification.service.adapter;

import java.math.BigInteger;
import java.util.*;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import com.itrustmachines.common.util.ByteUtils;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.verification.service.VerifiedClearanceRecordInfo;
import com.itrustmachines.verification.service.VerifyClearanceRecordService;
import com.itrustmachines.verification.util.VerifyReportUtil;
import com.itrustmachines.verification.vo.VerifyReport;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyClearanceRecordServiceAdapter extends VerifyClearanceRecordService {
  
  @Override
  protected Map<Long, VerifiedClearanceRecordInfo> buildVerifiedClearanceRecordInfoMap(
      @NonNull final List<ClearanceRecord> crList, @NonNull final ClearanceRecord latestCR,
      @NonNull VerifyReport report) {
    log.debug("buildVerifiedClearanceRecordMap() crList={}, latestCR={}", crList, latestCR);
    Map<Long, VerifiedClearanceRecordInfo> verifiedClearanceRecordInfoMap = new HashMap<>();
    Map<Long, ClearanceRecord> clearanceRecordMap = buildClearanceRecordMapFilterByLatestCO(crList, latestCR);
    // verify latest cr
    boolean pass = false;
    Long currentCo = latestCR.getClearanceOrder();
    ClearanceRecord currentRecord = clearanceRecordMap.get(currentCo);
    if (currentRecord.isEqualTo(latestCR)) {
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
    report.setVerifyLastClearanceRecordReport(
        VerifyReportUtil.buildVerifyLastClearanceRecordReport(currentRecord, pass));
    if (crList.size() > 1) {
      final List<String> reportList = new ArrayList<>();
      currentCo--;
      while (!clearanceRecordMap.isEmpty()) {
        currentRecord = clearanceRecordMap.get(currentCo);
        final ClearanceRecord previousRecord = clearanceRecordMap.get(currentCo - 1);
        if (Objects.nonNull(currentRecord) && Objects.nonNull(previousRecord)) {
          // have previous cr
          final VerifyChainHashResult verifyChainHashResult = verifyChainHash(currentRecord, previousRecord);
          reportList.add(verifyChainHashResult.getReport());
          pass = verifyChainHashResult.isPass();
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
      report.setVerifyClearanceRecordReportList(reportList);
    }
    
    log.debug("buildPassClearanceRecord() verifiedClearanceRecordInfoMap={}", verifiedClearanceRecordInfoMap);
    return verifiedClearanceRecordInfoMap;
  }
  
  @Override
  protected VerifyChainHashResult verifyChainHash(@NonNull final ClearanceRecord currentRecord,
      @NonNull final ClearanceRecord previousRecord) {
    log.debug("verifyChainHash() start, currentRecord={}, previousRecord={}", currentRecord, previousRecord);
    byte[] concatByteArray = ByteUtils.concatByteArray(HashUtils.hex2byte(currentRecord.getRootHash()),
        Numeric.toBytesPadded(BigInteger.valueOf(currentRecord.getClearanceOrder()), 32));
    
    concatByteArray = ByteUtils.concatByteArray(concatByteArray, HashUtils.hex2byte(previousRecord.getChainHash()));
    final byte[] chainHash = Hash.sha3(concatByteArray);
    final boolean pass = Arrays.equals(chainHash, HashUtils.hex2byte(currentRecord.getChainHash()));
    final String report = VerifyReportUtil.buildVerifyClearanceRecordReport(currentRecord, concatByteArray, chainHash,
        pass);
    final VerifyChainHashResult result = VerifyChainHashResult.builder()
                                                              .pass(pass)
                                                              .report(report)
                                                              .build();
    log.debug("verifyChainHash() end, result={}", result);
    
    return result;
  }
}
