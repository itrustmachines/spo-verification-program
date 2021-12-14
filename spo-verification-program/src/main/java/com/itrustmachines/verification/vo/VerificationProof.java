package com.itrustmachines.verification.vo;

import java.util.List;
import java.util.Objects;

import com.itrustmachines.common.ethereum.EthereumEnv;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.SpoSignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@ToString(exclude = { "existenceProofs", "clearanceRecords" })
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
  
  public String toSignDataWithLombok() {
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
  
  public String toSignData() {
    final StringBuilder stringBuilder = new StringBuilder().append(version)
                                                           .append(query)
                                                           .append(timestamp)
                                                           .append(contractAddress)
                                                           .append(serverWalletAddress)
                                                           .append(env)
                                                           .append(nodeConnectionString);
    this.getExistenceProofs()
        .forEach(existenceProof -> stringBuilder.append(existenceProof.toSignData()));
    this.getClearanceRecords()
        .forEach(clearanceRecord -> stringBuilder.append(clearanceRecord.toSignData()));
    return stringBuilder.toString();
  }
  
  public VerificationProof sign(@NonNull final String privateKey) {
    try {
      this.setSigServer(SignatureUtil.signEthereumMessage(privateKey, toSignData()));
    } catch (Exception e) {
      final String errMsg = String.format("sign() error, input=%s", this);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    }
    return this;
  }
  
  public String obtainPublicKey() {
    String result = null;
    try {
      result = SignatureUtil.recoverEthereumPublicKeyString(serverWalletAddress, sigServer, toSignData());
    } catch (Exception ex) {
      log.error("obtainPublicKey() error, input={}", this, ex);
    }
    if (Objects.isNull(result)) {
      try {
        result = SignatureUtil.recoverEthereumPublicKeyString(serverWalletAddress, sigServer, toSignDataWithLombok());
      } catch (Exception ex) {
        log.error("obtainPublicKey() error, input={}", this, ex);
      }
    }
    if (Objects.isNull(result)) {
      try {
        result = SignatureUtil.recoverPublicKeyString(serverWalletAddress, sigServer, toSignData());
      } catch (Exception ex) {
        log.error("obtainPublicKey() error, input={}", this, ex);
      }
    }
    if (Objects.isNull(result)) {
      try {
        result = SignatureUtil.recoverPublicKeyString(serverWalletAddress, sigServer, toSignDataWithLombok());
      } catch (Exception ex) {
        log.error("obtainPublicKey() error, input={}", this, ex);
      }
    }
    return result;
  }
  
}
