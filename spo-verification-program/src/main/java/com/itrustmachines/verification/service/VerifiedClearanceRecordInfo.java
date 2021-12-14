package com.itrustmachines.verification.service;

import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifiedClearanceRecordInfo {
  
  private boolean pass;
  
  private ClearanceRecord clearanceRecord;
  
}
