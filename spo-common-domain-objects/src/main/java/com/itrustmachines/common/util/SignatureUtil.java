package com.itrustmachines.common.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.web3j.crypto.*;

import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.common.web3j.Web3jSignature;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class SignatureUtil {
  
  /**
   * @param privateKey
   *          the private key to derive the public key from
   * @param data
   *          sign this data
   * @return signature class
   */
  public Web3jSignature signData(@NonNull final String privateKey, @NonNull final String data) {
    final Credentials credentials = Credentials.create(privateKey);
    final Sign.SignatureData signature = Sign.signMessage(data.getBytes(StandardCharsets.UTF_8),
        credentials.getEcKeyPair());
    final byte[] content = Hash.sha3(data.getBytes(StandardCharsets.UTF_8));
    
    return Web3jSignature.builder()
                         .content(content)
                         .r(signature.getR())
                         .s(signature.getS())
                         .v(signature.getV())
                         .build();
  }
  
  /**
   * @param serverWalletAddress
   *          ethereum address
   * @param sig
   *          signature.r signature.s
   * @param message
   *          sign message
   * @return boolean
   */
  public boolean verifySignature(@NonNull final String serverWalletAddress, @NonNull final SpoSignature sig,
      @NonNull final byte[] message) {
    log.debug("verifySignature() serverWalletAddress={}, sig={}", serverWalletAddress, sig);
    final ECDSASignature ecdsaSignature = transferToECDSASignature(sig);
    boolean match = false;
    String addressRecovered = null;
    for (int i = 0; i < 4; i++) {
      BigInteger publicKey = null;
      try {
        final byte[] v = HashUtils.hex2byte(sig.getV());
        publicKey = Sign.recoverFromSignature(v[0] - 27, ecdsaSignature, message);
      } catch (Exception e) {
        log.error("verifySignature error, serverWalletAddress={}, sig={}", serverWalletAddress, sig, e);
      }
      if (publicKey != null) {
        addressRecovered = "0x" + Keys.getAddress(publicKey);
        if (serverWalletAddress.equalsIgnoreCase(addressRecovered)) {
          match = true;
          break;
        }
      }
    }
    log.debug("verifySignature() result={}", match);
    return match;
  }
  
  // TODO check usage
  public BigInteger getPublicKey(@NonNull final String address, @NonNull final ECDSASignature sig,
      @NonNull final byte[] message) {
    log.debug("getPublicKey() address={}, sig={}", address, sig);
    BigInteger publicKey = null;
    for (int i = 0; i < 4; i++) {
      try {
        publicKey = Sign.recoverFromSignature((byte) i, sig, message);
      } catch (Exception e) {
        log.error("getPublicKey() error, address={}, sig={}", address, sig, e);
      }
      log.debug("pk={}", publicKey);
      if (publicKey != null) {
        final String addressRecovered = "0x" + Keys.getAddress(publicKey);
        if (address.equalsIgnoreCase(addressRecovered)) {
          return publicKey;
        }
      }
    }
    return publicKey;
  }
  
  public ECDSASignature transferToECDSASignature(@NonNull final SpoSignature sig) {
    log.debug("transferToECDSASignature() sig={}", sig);
    ECDSASignature ecdsaSig = null;
    try {
      final BigInteger r = new BigInteger(1, HashUtils.hex2byte(sig.getR()));
      final BigInteger s = new BigInteger(1, HashUtils.hex2byte(sig.getS()));
      ecdsaSig = new ECDSASignature(r, s);
    } catch (Exception e) {
      log.error("transferToECDSASignature() error, sig={}", sig, e);
      throw e;
    }
    log.debug("transferToECDSASignature() ecdsaSig={}", ecdsaSig);
    return ecdsaSig;
  }
  
}
