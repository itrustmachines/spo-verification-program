package com.itrustmachines.verification.util;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.tpm.Slice;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class SliceValidationUtil {
  
  public final char SLICE_DELIMITER = '.';
  
  public boolean isLeafNode(final String sliceString, final List<PBPair.PBPairValue> pbPair, final byte[] txHash) {
    final Slice slice = Slice.fromString(sliceString);
    final int index = slice.getIndex();
    
    final String leafNode = getLeafNode(slice);
    
    final List<byte[]> valueList = pbPair.stream()
                                         .map(PBPair.PBPairValue::getValue)
                                         .map(HashUtils::hex2byte)
                                         .collect(Collectors.toList());
    boolean txHashOk = false;
    for (byte[] digest : valueList) {
      txHashOk = Arrays.equals(digest, txHash);
      if (txHashOk)
        break;
    }
    return txHashOk && leafNode.equalsIgnoreCase(HashUtils.byte2hex(HashUtils.sha256(valueList)));
  }
  
  public String getLeafNode(final Slice slice) {
    final int sliceListSize = slice.getInterNodes()
                                   .size();
    final int index = slice.getIndex();
    String leafNode;
    if (index % 2 == 0 || sliceListSize == 1) {
      leafNode = slice.getInterNodes()
                      .get(0);
    } else {
      leafNode = slice.getInterNodes()
                      .get(1);
    }
    return leafNode;
  }
  
  public boolean evalRootHashFromSlice(String sliceString) {
    final String[] tokens = sliceString.split("\\" + SLICE_DELIMITER);
    int index = Integer.parseInt(tokens[0]);
    int parentIndex;
    byte[] parentDigest = null;
    for (int i = 1; index > 1; i += 2, index /= 2) {
      parentIndex = i + 2 + (index / 2 == 1 ? 0 : index / 2) % 2;
      parentDigest = HashUtils.sha256(HashUtils.hex2byte(tokens[i]), HashUtils.hex2byte(tokens[i + 1]));
      if (!HashUtils.byte2hex(parentDigest)
                    .equals(tokens[parentIndex])) {
        return false;
      }
    }
    return true;
  }
  
  public byte[] getRootHash(String sliceString) throws NoSuchElementException {
    final String[] tokens = sliceString.split(String.valueOf("\\" + SLICE_DELIMITER));
    int index = Integer.parseInt(tokens[0]);
    int parentIndex = 1;
    byte[] parentDigest = HashUtils.hex2byte(tokens[1]);
    for (int i = 1; index > 1; i += 2, index /= 2) {
      if (i == parentIndex) {
        parentDigest = HashUtils.sha256(parentDigest, HashUtils.hex2byte(tokens[i + 1]));
      } else {
        parentDigest = HashUtils.sha256(HashUtils.hex2byte(tokens[i]), parentDigest);
      }
      parentIndex = i + 2 + (index / 2 == 1 ? 0 : index / 2) % 2;
      
    }
    return parentDigest;
  }
  
}
