package com.itrustmachines.common.util;

import org.web3j.crypto.Credentials;

import com.itrustmachines.common.vo.KeyInfo;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class KeyInfoUtil {
  
  public KeyInfo buildKeyInfo(final @NonNull String privateKey) {
    log.debug("buildKeyInfo() begin");
    final Credentials credentials = Credentials.create(privateKey);
    final KeyInfo keyInfo = KeyInfo.builder()
                                   .address(credentials.getAddress())
                                   .privateKey(privateKey)
                                   .publicKey(credentials.getEcKeyPair()
                                                         .getPublicKey()
                                                         .toString(16))
                                   .build();
    log.debug("buildKeyInfo() end, keyInfo={}", keyInfo);
    return keyInfo;
  }
  
}
