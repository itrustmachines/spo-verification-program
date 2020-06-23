package com.itrustmachines.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClearanceRecord {

  private Long id;
  private Long clearanceOrder;
  private String rootHash;
  private String chainHash;
  private String description;
  private Long createTime;

  // for build blockchain browser link
  private String txHash;
  
}