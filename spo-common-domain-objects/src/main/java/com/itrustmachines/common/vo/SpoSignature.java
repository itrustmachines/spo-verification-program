package com.itrustmachines.common.vo;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.web3j.Web3jSignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class SpoSignature implements Serializable, Cloneable {
  
  private String r;
  private String s;
  private String v;
  
  public SpoSignature clone() {
    return SerializationUtils.clone(this);
  }
  
  public static SpoSignature fromByte(final Web3jSignature web3jSignature) {
    if (web3jSignature == null) {
      log.warn("fromByte() web3jSignature is null, return default SpoSignature object");
      return SpoSignature.builder()
                         .build();
    }
    final String r = HashUtils.byte2hex(web3jSignature.getR());
    final String s = HashUtils.byte2hex(web3jSignature.getS());
    final String v = HashUtils.byte2hex(web3jSignature.getV());
    return SpoSignature.builder()
                       .r(r)
                       .s(s)
                       .v(v)
                       .build();
  }
  
  public String toSignData() {
    return new StringBuilder().append(this.getR())
                              .append(this.getS())
                              .append(this.getV())
                              .toString();
  }
}
