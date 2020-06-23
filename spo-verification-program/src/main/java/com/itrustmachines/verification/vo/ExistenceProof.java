package com.itrustmachines.verification.vo;

import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExistenceProof {
  
  private Long clearanceOrder;
  private String indexValue;
  private boolean exist;

  private MerkleProof merkleProof;
  private Receipt receipt;
  
}
