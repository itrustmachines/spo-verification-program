package com.itrustmachines.verification.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Query {
  
  public enum QueryType {
    
    // query by locator
    LOCATOR,
    
    // query by clearanceOrder
    CLEARANCE_ORDER,
    
    // query by timestamp
    TIMESTAMP
  
  }
  
  QueryType type;
  String indexValueKey;
  long fromCO;
  long toCO;
  long fromTS;
  long toTS;
  
}
