package com.itrustmachines.verification.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyProofCount {
  
  private long totalCount;
  private long successCount;
  private long modifiedCount;
  private long removedCount;
  private long addedCount;
  
  public VerifyProofCount add(VerifyProofCount count) {
    totalCount += count.getTotalCount();
    successCount += count.getSuccessCount();
    modifiedCount += count.getModifiedCount();
    removedCount += count.getRemovedCount();
    addedCount += count.getAddedCount();
    return this;
  }

}
