package com.itrustmachines.common.vo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.web3j.crypto.Hash;

import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.PBPairUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerkleProof implements Serializable {
  
  private String slice;
  
  private List<PBPair.PBPairValue> pbPair;
  
  private Long clearanceOrder;
  
  private SpoSignature sigServer;
  
  public String toSignData() {
    return new StringBuilder().append(slice)
                              .append(pbPair != null ? PBPairUtil.toPbPairString(pbPair) : pbPair)
                              .append(clearanceOrder)
                              .toString();
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
}
