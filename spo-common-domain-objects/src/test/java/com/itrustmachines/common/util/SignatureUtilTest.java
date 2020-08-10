package com.itrustmachines.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;

import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.common.web3j.Web3jSignature;

public class SignatureUtilTest {
  
  @Test
  public void testSignData() throws Exception {
    // given
    String privateKey = "43cbbbf7643cd3f8bdf54d70014cd5fcc313b243aadec7081d16c1ad04ee4b8f";
    String data = "test1123456";
    
    // when
    Web3jSignature signature = SignatureUtil.signData(privateKey, data);
    
    // then
    assertThat(signature.getContent()).isNotEmpty()
                                      .hasSize(32);
    assertThat(signature.getR()).isNotEmpty()
                                .hasSize(32);
    assertThat(signature.getS()).isNotEmpty()
                                .hasSize(32);
    assertThat(signature.getV()).hasSize(1)
                                .isNotNull();
  }
  
  @Test
  public void test_signData_null() throws Exception {
    // given
    String privateKey = "43cbbbf7643cd3f8bdf54d70014cd5fcc313b243aadec7081d16c1ad04ee4b8f";
    String data = null;
    
    // then
    assertThatThrownBy(() -> {
      // when
      SignatureUtil.signData(null, null);
    
    }).isInstanceOf(NullPointerException.class)
      .hasMessage("privateKey is marked non-null but is null");
  }
  
  @Test
  public void test_getPublicKey() throws Exception {
    // given
    String privateKey = "43cbbbf7643cd3f8bdf54d70014cd5fcc313b243aadec7081d16c1ad04ee4b8f";
    String data = "test1123456";
    String address = "0xA2Be5Cc6a7683EA3E3b0405E3169111db7DaC31A";
    
    // when
    Credentials credentials = Credentials.create(privateKey);
    BigInteger publicKey = credentials.getEcKeyPair()
                                      .getPublicKey();
    Web3jSignature signature = SignatureUtil.signData(privateKey, data);
    final BigInteger r = new BigInteger(1, signature.getR());
    final BigInteger s = new BigInteger(1, signature.getS());
    ECDSASignature ecdsaSignature = new ECDSASignature(r, s);
    BigInteger pk = SignatureUtil.getPublicKey(address, ecdsaSignature, signature.getContent());
    
    // then
    assertThat(pk).isEqualTo(publicKey);
  }
  
  @Test
  public void test_transferToECDSASignature_Notnull() throws Exception {
    // given
    SpoSignature sig = SpoSignature.builder()
                                   .r("string")
                                   .s("string")
                                   .v("string")
                                   .build();
    
    // then
    ECDSASignature signature = SignatureUtil.transferToECDSASignature(sig);
    
    // then
    assertThat(signature).isNotNull();
  }
  
  @Test
  public void test_transferToECDSASignature_null() throws Exception {
    // given
    SpoSignature sig = null;
    
    // then
    assertThatThrownBy(() -> {
      // when
      SignatureUtil.transferToECDSASignature(sig);
    
    }).isInstanceOf(NullPointerException.class)
      .hasMessageContaining("sig is marked non-null but is null");
    
  }
  
  @Test
  public void test_getPublicKey_wrong() throws Exception {
    // given
    String privateKey = "43cbbbf7643cd3f8bdf54d70014cd5fcc313b243aadec7081d16c1ad04ee4b8f";
    String data = "test1123456";
    String address = "";
    
    // when
    Credentials credentials = Credentials.create(privateKey);
    BigInteger publicKey = credentials.getEcKeyPair()
                                      .getPublicKey();
    Web3jSignature signature = SignatureUtil.signData(privateKey, data);
    final BigInteger r = new BigInteger(1, signature.getR());
    final BigInteger s = new BigInteger(1, signature.getS());
    ECDSASignature ecdsaSignature = new ECDSASignature(r, s);
    BigInteger pk = SignatureUtil.getPublicKey(address, ecdsaSignature, signature.getContent());
    
    // then
    assertThat(pk).isNotEqualTo(publicKey)
                  .isNull();
  }
  
}
