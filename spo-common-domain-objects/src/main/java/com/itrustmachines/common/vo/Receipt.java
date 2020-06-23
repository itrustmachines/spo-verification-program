package com.itrustmachines.common.vo;

import java.nio.charset.StandardCharsets;

import org.web3j.crypto.Hash;

import com.itrustmachines.common.util.HashUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Receipt {
  
  private String callerAddress;
  private Long timestamp;
  private String cmd;
  private String indexValue;
  private String metadata;
  private Long clearanceOrder;
  private SpoSignature sigClient;
  private Long timestampSPO;
  private String result;
  private SpoSignature sigServer;
  
  public String toSignData() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getCallerAddress())
      .append(this.getTimestamp())
      .append(this.getCmd())
      .append(this.getIndexValue())
      .append(this.getMetadata())
      .append(this.getClearanceOrder())
      .append(this.getTimestampSPO())
      .append(this.getResult())
      .append(this.getSigClient()
                  .getR())
      .append(this.getSigClient()
                  .getS())
      .append(this.getSigClient()
                  .getV());
    return sb.toString();
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
  public byte[] toDigestValue() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getCallerAddress())
      .append(this.getTimestamp())
      .append(this.getCmd())
      .append(this.getIndexValue())
      .append(this.getMetadata())
      .append(this.getClearanceOrder())
      .append(this.getTimestampSPO())
      .append(this.getResult())
      .append(this.getSigClient()
                  .getR())
      .append(this.getSigClient()
                  .getS())
      .append(this.getSigClient()
                  .getV())
      .append(this.getSigServer()
                  .getR())
      .append(this.getSigServer()
                  .getS())
      .append(this.getSigServer()
                  .getV());
    
    return HashUtils.sha256(sb.toString()
                              .getBytes(StandardCharsets.UTF_8));
  }
  
}
