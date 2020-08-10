package com.itrustmachines.common.tpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.util.HashUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PBPairTest {
  
  @Test
  public void test_getKeyHashs() {
    PBPair pbPair = new PBPair();
    pbPair.addPbPairValue(PBPair.PBPairValue.builder()
                                            .keyHash("1")
                                            .value(HashUtils.byte2hex(HashUtils.sha256("1".getBytes())))
                                            .index(1)
                                            .build());
    
    // when
    List<String> keyHashsList = pbPair.getKeyHashs();
    
    // then
    assertThat(keyHashsList).contains("1");
  }
  
  @Test
  public void test_getValues() {
    PBPair pbPair = new PBPair();
    pbPair.addPbPairValue(PBPair.PBPairValue.builder()
                                            .keyHash("1")
                                            .value(HashUtils.byte2hex(HashUtils.sha256("1".getBytes())))
                                            .index(1)
                                            .build());
    
    // when
    List<String> values = pbPair.getValues();
    final List<byte[]> valueList = values.stream()
                                         .map(HashUtils::hex2byte)
                                         .collect(Collectors.toList());
    
    // then
    assertThat(valueList).contains(HashUtils.sha256("1".getBytes()));
  }
  
}