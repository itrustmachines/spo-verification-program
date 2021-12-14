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
    TIMESTAMP,
    
    // query by clearanceOrder and sn
    CLEARANCE_ORDER_AND_SN,
    
    // error
    ERROR
  }
  
  QueryType type;
  String indexValueKey;
  long fromCO;
  long fromSN;
  long toCO;
  long toSN;
  long fromTS;
  long toTS;
  
}
