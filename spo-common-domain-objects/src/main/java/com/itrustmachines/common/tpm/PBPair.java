package com.itrustmachines.common.tpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Builder
@Slf4j
public class PBPair implements Serializable {

  private static final long serialVersionUID = 1L;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PBPairValue {

    private Integer index;
    private String keyHash;
    private String value;
    
  }

  private List<PBPairValue> pbPairValueList;
  
  public PBPair() {
    pbPairValueList = new ArrayList<>();
  }
  
  public boolean addPbPairValue(PBPairValue pbPairValue) {
    return pbPairValueList.add(pbPairValue);
  }
  
  public List<String> getKeyHashs() {
    return pbPairValueList.stream()
                          .map(PBPairValue::getKeyHash)
                          .collect(Collectors.toList());
  }
  
  public List<String> getValues() {
    return pbPairValueList.stream()
                          .map(PBPairValue::getValue)
                          .collect(Collectors.toList());
  }
  
}
