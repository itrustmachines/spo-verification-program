package com.itrustmachines.common.license.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivationHistory implements Serializable {
  
  private Long fromTimestamp;
  private Long toTimestamp;
  private Long txCount;
  private Long maxRegisterCount;
  
}
