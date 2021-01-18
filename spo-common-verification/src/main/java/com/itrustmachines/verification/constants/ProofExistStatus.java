package com.itrustmachines.verification.constants;

public enum ProofExistStatus {
  
  // verify pass
  PASS,
  
  // verify fail
  MODIFIED,
  
  // indexValue is missing in proof
  REMOVED,
  
  // indexValue is redundant in proof
  ADDED,
  
  // clearanceRecord error
  CLEARANCE_RECORD_ERROR

}
