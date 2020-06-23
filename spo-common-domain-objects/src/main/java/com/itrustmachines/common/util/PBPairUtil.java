package com.itrustmachines.common.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.utils.Numeric;

import com.itrustmachines.common.tpm.PBPair;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class PBPairUtil {
  
  public String toPbPairString(final @NonNull PBPair pbPair) {
    return toPbPairString(pbPair.getPbPairValueList());
  }
  
  public String toPbPairString(final @NonNull List<PBPair.PBPairValue> pbPairList) {
    final StringBuilder sb = new StringBuilder();
    for (PBPair.PBPairValue pbPairValue : pbPairList) {
      sb.append(pbPairValue.getIndex())
        .append(pbPairValue.getKeyHash())
        .append(pbPairValue.getValue());
    }
    final String pbPairString = sb.toString();
    log.debug("pbPairString={}", pbPairString);
    return pbPairString;
  }
  
  public List<byte[]> getPbPairIndex(final @NonNull List<PBPair.PBPairValue> pbPairValueList) {
    log.debug("getPbPairIndex() pbPairValueList={}", pbPairValueList);
    final List<byte[]> result = new ArrayList<>();
    for (PBPair.PBPairValue pbPairValue : pbPairValueList) {
      result.add(Numeric.toBytesPadded(BigInteger.valueOf(pbPairValue.getIndex()), 8));
    }
    log.debug("getPbPairIndex result={}", result);
    return result;
  }
  
  public List<byte[]> getPBPairKey(final @NonNull List<PBPair.PBPairValue> pbPairValueList) {
    log.debug("getPBPairKey() pbPairValueList={}", pbPairValueList);
    final List<byte[]> result = new ArrayList<>();
    for (PBPair.PBPairValue pbPairValue : pbPairValueList) {
      result.add(HashUtils.hex2byte(pbPairValue.getKeyHash()));
    }
    log.debug("getPBPairKey() result={}", pbPairValueList);
    return result;
  }
  
  public List<byte[]> getPBPairValue(List<PBPair.PBPairValue> pbPairValueList) {
    log.debug("getPBPairValue() pbPairValueList={}", pbPairValueList);
    final List<byte[]> result = new ArrayList<>();
    for (PBPair.PBPairValue pbPairValue : pbPairValueList) {
      result.add(HashUtils.hex2byte(pbPairValue.getValue()));
    }
    log.debug("getPBPairValue() result={}", pbPairValueList);
    return result;
  }
  
}
