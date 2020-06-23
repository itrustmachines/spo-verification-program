package com.itrustmachines.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptLocator {
  
  private Long clearanceOrder;
  private String indexValue;
  
}
