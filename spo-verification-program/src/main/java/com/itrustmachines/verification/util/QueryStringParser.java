package com.itrustmachines.verification.util;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryStringParser {
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Query {
    
    String indexValueKey;
    long fromCO;
    long toCO;
    long fromTS;
    long toTS;
    
  }
  
  /**
   * 1. Locator List: <code>Locators=(5:XXX_R0),(5:XXX_R1),(5:XXX_R2)</code> 2.
   * TS: <code>IV_Key=XXX,FromTS=xxxxx,ToTS=xxxxx,FromCO=xx,ToCO=xx</code> 3. CO:
   * <code>IV_Key=XXX,FromCO=xx,ToCO=xx</code>
   */
  public static Query parse(final @NonNull String queryString) {
    final Query.QueryBuilder queryBuilder = Query.builder();
    
    final String[] keyValuePairs = queryString.split(",");
    for (String pair : keyValuePairs) {
      final String[] split = pair.split("=");
      if ("IV_Key".equals(split[0])) {
        queryBuilder.indexValueKey(split[1]);
      } else if ("FromTS".equals(split[0])) {
        queryBuilder.fromTS(NumberUtils.toLong(split[1]));
      } else if ("ToTS".equals(split[0])) {
        queryBuilder.toTS(NumberUtils.toLong(split[1]));
      } else if ("FromCO".equals(split[0])) {
        queryBuilder.fromCO(NumberUtils.toLong(split[1]));
      } else if ("ToCO".equals(split[0])) {
        queryBuilder.toCO(NumberUtils.toLong(split[1]));
      }
    }
    final Query result = queryBuilder.build();
    log.debug("parse={} result={}", queryString, result);
    return result;
  }
  
}
