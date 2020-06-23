package com.itrustmachines.common.vo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.itrustmachines.common.tpm.PBPair;
import lombok.*;
import org.web3j.crypto.Hash;

import com.itrustmachines.common.util.PBPairUtil;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerkleProof implements Serializable {
  private static final long serialVersionUID = 1L;

  private String slice;

  private List<PBPair.PBPairValue> pbPair;

  private Long clearanceOrder;

  private SpoSignature sigServer;
  
  public String toSignData() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getSlice())
      .append(PBPairUtil.toPbPairString(getPbPair()))
      .append(getClearanceOrder());

    return sb.toString();
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
}
