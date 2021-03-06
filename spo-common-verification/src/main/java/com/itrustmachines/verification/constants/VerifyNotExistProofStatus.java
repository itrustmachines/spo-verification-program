package com.itrustmachines.verification.constants;

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
  ERROR_SIGNATURE,
  
  // clearance record error
  CLEARANCE_RECORD_ERROR

}
