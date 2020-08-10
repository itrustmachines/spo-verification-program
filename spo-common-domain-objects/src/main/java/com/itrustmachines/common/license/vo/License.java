package com.itrustmachines.common.license.vo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.web3j.crypto.Hash;

import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.common.web3j.Web3jSignature;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class License implements Serializable {
  
  // TODO use mac address or not
  // private String macAddress;
  
  private String spoWalletAddress;
  private String itmWalletAddress;
  private Long startTime;
  private List<ActivationHistory> activationHistoryList;
  
  private SpoSignature signature;
  
  public String toSignData() {
    return new StringBuilder().append(spoWalletAddress)
                              .append(itmWalletAddress)
                              .append(startTime)
                              .append(activationHistoryList.toString())
                              .toString();
  }
  
  public byte[] toSignDataSha3() {
    return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
  }
  
  public License sign(@NonNull final String privateKey) {
    try {
      final Web3jSignature web3jSignature = SignatureUtil.signData(privateKey, toSignData());
      this.signature = SpoSignature.fromByte(web3jSignature);
    } catch (Exception e) {
      final String errMsg = String.format("sign() error, License=%s", this);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    }
    return this;
  }
  
}