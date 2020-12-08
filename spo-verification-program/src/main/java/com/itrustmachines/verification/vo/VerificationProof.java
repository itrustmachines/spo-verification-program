package com.itrustmachines.verification.vo;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.web3j.crypto.Hash;

import com.itrustmachines.common.ethereum.EthereumEnv;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.common.web3j.Web3jSignature;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class VerificationProof {
  
  // SPO Server Version
  private String version;
  
  /**
   * 1. Locator List: <code>Locators=(5:XXX_R0),(5:XXX_R1),(5:XXX_R2)</code>
   *
   * 2. TS: <code>IV_Key=XXX,FromTS=xxxxx,ToTS=xxxxx,FromCO=xx,ToCO=xx</code>
   *
   * 3. CO: <code>IV_Key=XXX,FromCO=xx,ToCO=xx</code>
   */
  private String query;
  
  private Long timestamp;
  
  private String contractAddress;
  private String serverWalletAddress;
  
  private EthereumEnv env;
  
  // for custom blockchain
  private String nodeConnectionString;
  
  private List<ExistenceProof> existenceProofs;
  
  private List<ClearanceRecord> clearanceRecords;
  
  private SpoSignature sigServer;
  
  public String toSignData() {
    return new StringBuilder().append(version)
                              .append(query)
                              .append(timestamp)
                              .append(contractAddress)
                              .append(serverWalletAddress)
                              .append(env)
                              .append(nodeConnectionString)
                              .append(existenceProofs)
                              .append(clearanceRecords)
                              .toString();
  }
  
  public VerificationProof sign(@NonNull final String privateKey) {
    try {
      final Web3jSignature web3jSignature = SignatureUtil.signData(privateKey, toSignData());
      this.setSigServer(SpoSignature.fromByte(web3jSignature));
    } catch (Exception e) {
      final String errMsg = String.format("sign() error, input=%s", this);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    }
    return this;
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
  public String obtainPublicKey() {
    String result = null;
    try {
      result = SignatureUtil.getPublicKey(serverWalletAddress, SignatureUtil.transferToECDSASignature(sigServer),
          toSignDataSha3())
                            .toString(16);
    } catch (Exception ex) {
      log.error("obtainPublicKey() error, input={}", this, ex);
    }
    return result;
  }
  
}
