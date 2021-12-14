package com.itrustmachines.verification.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.verification.vo.VerifyReport;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class VerifyClearanceRecordService {
  
  // key: clearanceOrder, value: VerifiedClearanceRecordInfo
  protected abstract Map<Long, VerifiedClearanceRecordInfo> buildVerifiedClearanceRecordInfoMap(
      @NonNull final List<ClearanceRecord> crList, final @NonNull ClearanceRecord latestCR,
      @NonNull VerifyReport report);
  
  protected Map<Long, ClearanceRecord> buildClearanceRecordMapFilterByLatestCO(
      @NonNull final List<ClearanceRecord> clearanceRecordList, @NonNull final ClearanceRecord latestCR) {
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
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  protected static class VerifyChainHashResult {
    
    private boolean pass;
    
    private String report;
    
  }
  
  protected abstract VerifyChainHashResult verifyChainHash(@NonNull final ClearanceRecord currentRecord,
      @NonNull final ClearanceRecord previousRecord);
}
