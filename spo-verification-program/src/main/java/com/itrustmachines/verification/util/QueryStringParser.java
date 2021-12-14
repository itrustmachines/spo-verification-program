package com.itrustmachines.verification.util;

import org.apache.commons.lang3.math.NumberUtils;

import com.itrustmachines.verification.vo.Query;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryStringParser {
  
  /**
   * 1. Locator List: <code>Locators=(5:XXX_R0),(5:XXX_R1),(5:XXX_R2)</code><br>
   * 2. TS: <code>IV_Key=XXX,FromTS=xxxxx,ToTS=xxxxx,FromCO=xx,ToCO=xx</code><br>
   * 3. CO: <code>IV_Key=XXX,FromCO=xx,ToCO=xx</code>
   */
  public static Query parse(@NonNull final String queryString) {
    final Query.QueryBuilder queryBuilder = Query.builder();
    
    if (queryString.startsWith("Locators")) {
      queryBuilder.type(Query.QueryType.LOCATOR);
    } else {
      if (queryString.contains(",FromTS=") && queryString.contains(",ToTS=")) {
        queryBuilder.type(Query.QueryType.TIMESTAMP);
      } else if (queryString.contains(",FromSN=") && queryString.contains(",ToSN=")) {
        queryBuilder.type(Query.QueryType.CLEARANCE_ORDER_AND_SN);
      } else {
        queryBuilder.type(Query.QueryType.CLEARANCE_ORDER);
      }
      
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
        } else if ("FromSN".equals(split[0])) {
          queryBuilder.fromSN(NumberUtils.toLong(split[1]));
        } else if ("ToSN".equals(split[0])) {
          queryBuilder.toSN(NumberUtils.toLong(split[1]));
        }
      }
    }
    final Query result = queryBuilder.build();
    log.debug("parse={} result={}", queryString, result);
    return result;
  }
  
}
