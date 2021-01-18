package com.itrustmachines.verification.util;

import java.util.Arrays;

import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ClearanceRecordVerifyUtil {
  
  public boolean isRootHashEqual(@NonNull ClearanceRecord record, @NonNull byte[] rootHash) {
    log.debug("isRootHashEqual() CR={}, rootHash={}", record, HashUtils.byte2hex(rootHash));
    boolean result = Arrays.equals(HashUtils.hex2byte(record.getRootHash()), rootHash);
    log.debug("isRootHashEqual() result={}", result);
    return result;
  }
  
}
