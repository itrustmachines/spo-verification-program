package com.itrustmachines.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class IndexValueProperties {
  
  private String indexValueKey;
  private Long sn;
  
  public String toIndexValue() {
    return this.indexValueKey + "_R" + this.sn;
  }
  
  public String toIndexValueAddOneSn() {
    return this.indexValueKey + "_R" + (this.sn + 1);
  }
  
  private static Pattern pattern = Pattern.compile("(.+)_R(\\d+)$");
  
  public static IndexValueProperties of(String indexValue) {
    Matcher m = pattern.matcher(indexValue);
    IndexValueProperties indexValueProperties = null;
    if (m.find()) {
      final String indexValueKey = m.group(1);
      final Long serialNumber = Long.parseLong(m.group(2));
      indexValueProperties = IndexValueProperties.builder()
                                                 .indexValueKey(indexValueKey)
                                                 .sn(serialNumber)
                                                 .build();
    } else {
      log.error("parse IndexValueProperties error: indexValue={}", indexValue);
    }
    return indexValueProperties;
  }
  
}
