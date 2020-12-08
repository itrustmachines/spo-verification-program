package com.itrustmachines.common.ethereum.util;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.encoders.Hex;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ClearanceRecordTransactionParser {
  
  private final String DESC_BEGIN = "5b49544d2c5248";
  private static final String DESC_END = "5d";
  
  @SneakyThrows
  public static String parseDescription(@NonNull final String txInputData) {
    log.debug("parseDescription() txInputData={}", txInputData);
    
    final int beginIndex = txInputData.lastIndexOf(DESC_BEGIN);
    final int endIndex = txInputData.lastIndexOf(DESC_END);
    final String descHexString = txInputData.substring(beginIndex, endIndex + 2);
    log.debug("descHexString={}", descHexString);
    
    byte[] bytes = Hex.decode(descHexString);
    final String result = new String(bytes, StandardCharsets.UTF_8);
    log.debug("parseDescription() result={}", result);
    return result;
  }
  
}
