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
  
  private String spoWalletAddress;
  private String contractAddress;
  private String itmWalletAddress;
  private String productName;
  private String company;
  private Long startTime;
  private List<ActivationHistory> activationHistoryList;
  
  private SpoSignature signature;
  
  public String toSignData() {
    return new StringBuilder().append(spoWalletAddress)
                              .append(contractAddress)
                              .append(itmWalletAddress)
                              .append(productName)
                              .append(company)
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