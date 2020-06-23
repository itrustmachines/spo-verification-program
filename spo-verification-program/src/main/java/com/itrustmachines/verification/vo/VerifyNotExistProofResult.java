package com.itrustmachines.verification.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyNotExistProofResult {

  private Long clearanceOrder;
  private String indexValue;
  private boolean signatureOk;
  
  public enum VerifyNotExistProofStatus {
    
    // indexValue is not exist in pbpair
    OK_INDEX_VALUE_NOT_FOUND,
    
    // timestamp is not in from time to to time bound
    OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME,
    
    // indexValue in pbpair, must be a error
    ERROR_INDEX_VALUE_IN_PAIR,
    
    // calculate rootHash error
    ERROR_ROOT_HASH_ERROR,
    
    // calculate slice error
    ERROR_SLICE_ERROR,
    
    // signature error
    ERROR_SIGNATURE
  
  }
  
  // 3 results: not_exist, not_between_time, error_indexValue_in_Pair
  private VerifyNotExistProofStatus result;
  
}
