package com.itrustmachines.common.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.web3j.crypto.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ref:
 * https://ethereum.stackexchange.com/questions/41072/generate-private-key-and-address-using-web3j
 * https://github.com/web3j/web3j/tree/64d99e4fb33992f5e71b7f1c3bd8d0ada48553c0/core/src/main/java/org/web3j/crypto
 */
@Slf4j
public class KeyGeneratorUtil {
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class KeyInfo {
    
    String address;
    String privateKey;
    String publicKey;
    
    public String toSignData() {
      return address + publicKey;
    }
    
    public byte[] toSignDataSha3() {
      return Hash.sha3(toSignData().getBytes(StandardCharsets.UTF_8));
    }
    
  }
  
  public static KeyInfo generateKeyWithPassword(String password) {
    KeyInfo.KeyInfoBuilder builder = KeyInfo.builder();
    
    // sAddress length: 40
    // sPrivatekeyInHex length: 64
    // sPublicKeyInHex length: 128
    try {
      String sAddress;
      String sPublicKeyInHex;
      String sPrivatekeyInHex;
      do {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom());
        BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
        BigInteger publicKeyInDec = ecKeyPair.getPublicKey();
        
        sPublicKeyInHex = publicKeyInDec.toString(16);
        sPrivatekeyInHex = privateKeyInDec.toString(16);
        WalletFile aWallet = Wallet.createLight(password, ecKeyPair);
        sAddress = aWallet.getAddress();
      } while (sAddress.length() != 40 || sPrivatekeyInHex.length() != 64 || sPublicKeyInHex.length() != 128);
      
      builder = builder.address("0x" + sAddress)
                       .privateKey(sPrivatekeyInHex)
                       .publicKey(sPublicKeyInHex);
    } catch (Exception e) {
      log.error("generateKeyWithPassword() error, password={}", password, e);
    }
    return builder.build();
  }
  
}
