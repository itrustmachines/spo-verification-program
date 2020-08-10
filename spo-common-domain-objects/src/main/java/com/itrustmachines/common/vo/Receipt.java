package com.itrustmachines.common.vo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;
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
public class Receipt implements Serializable, Cloneable {
  
  // used in spo server
  private Long id;
  
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
    // IV + CO
    final String firstPart = new StringBuilder().append(this.getIndexValue())
                                                .append(this.getClearanceOrder())
                                                .toString();
    final String hashedSecondPart = getSecondPart();
    
    return firstPart + hashedSecondPart;
  }
  
  /**
   * For Objection
   * <li>First part: CO,IV</li>
   * <li>Second part: 除了CO,IV的ledger input取SHA256</li>
   * <li>Third part: Sig server的 R, S, V</li>
   */
  public String getSecondPart() {
    final String secondPart = new StringBuilder().append(this.getCallerAddress())
                                                 .append(this.getTimestamp())
                                                 .append(this.getCmd())
                                                 .append(this.getMetadata())
                                                 .append(this.getTimestampSPO())
                                                 .append(this.getResult())
                                                 .append(this.getSigClient()
                                                             .getR())
                                                 .append(this.getSigClient()
                                                             .getS())
                                                 .append(this.getSigClient()
                                                             .getV())
                                                 .toString();
    return HashUtils.byte2hex(HashUtils.sha256(secondPart.getBytes(StandardCharsets.UTF_8)));
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
  public byte[] toDigestValue() {
    final String toDigestContent = new StringBuilder().append(toSignData())
                                                      .append(this.getSigServer()
                                                                  .getR())
                                                      .append(this.getSigServer()
                                                                  .getS())
                                                      .append(this.getSigServer()
                                                                  .getV())
                                                      .toString();
    return HashUtils.sha256(toDigestContent.getBytes(StandardCharsets.UTF_8));
  }
  
}
