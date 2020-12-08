package com.itrustmachines.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;

import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.common.web3j.Web3jSignature;

public class SignatureUtilTest {
  
  public static final String PRIVATE_KEY = "43cbbbf7643cd3f8bdf54d70014cd5fcc313b243aadec7081d16c1ad04ee4b8f";
  public static final String ADDRESS = "0xa2be5cc6a7683ea3e3b0405e3169111db7dac31a";
  public static final String PUBLIC_KEY = "9065b050df9b711e7e1783a6042029a7e41dacd0fd48ab9ae1fe816e4c1d564d72adb37c461577776b2d2af4a076e15ce08ecccba594f0d8883cfb92d1d5711d";
  public static final String DATA = "test1123456";
  
  public static final byte[] EXPECTED_R = { //
      (byte) 0x3, (byte) 0xDD, (byte) 0x9C, (byte) 0x1E, (byte) 0x9D, (byte) 0xEB, (byte) 0xEB, (byte) 0xE5, //
      (byte) 0xA6, (byte) 0x2D, (byte) 0x9A, (byte) 0xB6, (byte) 0x35, (byte) 0x5, (byte) 0x47, (byte) 0x80, //
      (byte) 0x2, (byte) 0xCF, (byte) 0xEA, (byte) 0x77, (byte) 0x5, (byte) 0xE0, (byte) 0x28, (byte) 0xF8, //
      (byte) 0xA0, (byte) 0xAB, (byte) 0xE6, (byte) 0x6B, (byte) 0xFA, (byte) 0x1, (byte) 0xAF, (byte) 0xD9 };
  public static final byte[] EXPECTED_S = { //
      (byte) 0x44, (byte) 0x3A, (byte) 0xA4, (byte) 0x3D, (byte) 0x39, (byte) 0xA4, (byte) 0x12, (byte) 0x35, //
      (byte) 0xA3, (byte) 0xD0, (byte) 0x16, (byte) 0x6E, (byte) 0xB3, (byte) 0xDC, (byte) 0x34, (byte) 0x4C, //
      (byte) 0xED, (byte) 0x91, (byte) 0xF4, (byte) 0x23, (byte) 0x3D, (byte) 0x63, (byte) 0x16, (byte) 0x85, //
      (byte) 0x6E, (byte) 0x6A, (byte) 0x2E, (byte) 0xF4, (byte) 0x5E, (byte) 0xF5, (byte) 0x30, (byte) 0x69 };
  public static final byte[] EXPECTED_V = { (byte) 0x1B };
  
  @Test
  public void test_signData() {
    // when
    Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    
    // then
    assertThat(signature).isNotNull();
    assertThat(signature.getContent()).isNotEmpty()
                                      .hasSize(32);
    assertThat(signature.getR()).isNotEmpty()
                                .isEqualTo(EXPECTED_R);
    assertThat(signature.getS()).isNotEmpty()
                                .isEqualTo(EXPECTED_S);
    assertThat(signature.getV()).hasSize(1)
                                .isEqualTo(EXPECTED_V);
  }
  
  @Test
  public void test_signData_privateKey_empty() {
    assertThatThrownBy(() -> SignatureUtil.signData("", DATA)).isInstanceOf(Exception.class);
  }
  
  @Test
  public void test_signData_privateKey_null() {
    assertThatThrownBy(() -> SignatureUtil.signData(null, DATA)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_signData_data_empty() {
    // when
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, "");
    
    // then
    assertThat(signature).isNotNull();
  }
  
  @Test
  public void test_signData_data_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> SignatureUtil.signData(PRIVATE_KEY, null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_verifySignature() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // when
    final boolean result = SignatureUtil.verifySignature(ADDRESS, signature, message);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifySignature_address_empty() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // when
    final boolean result = SignatureUtil.verifySignature("", signature, message);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifySignature_address_incorrect() {
    // given
    final String address = "0x0000000000000000000000000000000000000000";
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // when
    final boolean result = SignatureUtil.verifySignature(address, signature, message);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifySignature_address_null() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // then
    assertThatThrownBy(() -> SignatureUtil.verifySignature(null, signature, message)).isInstanceOf(
        NullPointerException.class);
  }
  
  @Test
  public void test_verifySignature_signature_incorrect() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(
        SignatureUtil.signData("1234567812345678123456781234567812345678123456781234567812345678", DATA));
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // when
    final boolean result = SignatureUtil.verifySignature(ADDRESS, signature, message);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifySignature_signature_null() {
    // given
    final byte[] message = Hash.sha3(DATA.getBytes(StandardCharsets.UTF_8));
    
    // then
    assertThatThrownBy(() -> SignatureUtil.verifySignature(ADDRESS, null, message)).isInstanceOf(
        NullPointerException.class);
  }
  
  @Test
  public void test_verifySignature_message_incorrect() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    final byte[] message = Hash.sha3("000000000".getBytes(StandardCharsets.UTF_8));
    
    // when
    final boolean result = SignatureUtil.verifySignature(ADDRESS, signature, message);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifySignature_message_null() {
    // given
    final SpoSignature signature = SpoSignature.fromByte(SignatureUtil.signData(PRIVATE_KEY, DATA));
    
    // then
    assertThatThrownBy(() -> SignatureUtil.verifySignature(ADDRESS, signature, null)).isInstanceOf(
        NullPointerException.class);
  }
  
  @Test
  public void test_getPublicKey() {
    // given
    final BigInteger publicKey = new BigInteger(PUBLIC_KEY, 16);
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.fromByte(signature));
    final byte[] message = signature.getContent();
    
    final BigInteger result = SignatureUtil.getPublicKey(ADDRESS, ecdsaSignature, message);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(publicKey);
  }
  
  @Test
  public void test_getPublicKey_address_incorrect() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.fromByte(signature));
    final byte[] message = signature.getContent();
    
    final BigInteger result = SignatureUtil.getPublicKey("0x0000000000000000000000000000000000000000", ecdsaSignature,
        message);
    
    // then
    assertThat(result).isNull();
  }
  
  @Test
  public void test_getPublicKey_address_null() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.fromByte(signature));
    final byte[] message = signature.getContent();
    
    // then
    // noinspection CodeBlock2Expr
    assertThatThrownBy(() -> {
      SignatureUtil.getPublicKey(null, ecdsaSignature, message);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_getPublicKey_ecdsaSignature_incorrect() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.builder()
                                                                                             .r("1234567812345678123456781234567812345678123456781234567812345678")
                                                                                             .s("1234567812345678123456781234567812345678123456781234567812345678")
                                                                                             .v("1b")
                                                                                             .build());
    final byte[] message = signature.getContent();
    
    final BigInteger result = SignatureUtil.getPublicKey(ADDRESS, ecdsaSignature, message);
    
    // then
    assertThat(result).isNull();
  }
  
  @Test
  public void test_getPublicKey_ecdsaSignature_null() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final byte[] message = signature.getContent();
    
    // then
    // noinspection CodeBlock2Expr
    assertThatThrownBy(() -> {
      SignatureUtil.getPublicKey(ADDRESS, null, message);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_getPublicKey_message_incorrect() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.fromByte(signature));
    final byte[] message = Hash.sha3("00000000".getBytes(StandardCharsets.UTF_8));
    
    final BigInteger result = SignatureUtil.getPublicKey(ADDRESS, ecdsaSignature, message);
    
    // then
    assertThat(result).isNull();
  }
  
  @Test
  public void test_getPublicKey_message_null() {
    // given
    final Web3jSignature signature = SignatureUtil.signData(PRIVATE_KEY, DATA);
    final ECDSASignature ecdsaSignature = SignatureUtil.transferToECDSASignature(SpoSignature.fromByte(signature));
    
    // then
    // noinspection CodeBlock2Expr
    assertThatThrownBy(() -> {
      SignatureUtil.getPublicKey(ADDRESS, ecdsaSignature, null);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_transferToECDSASignature() {
    // given
    final SpoSignature sig = SpoSignature.builder()
                                         .r("1111111111111111111111111111111111111111111111111111111111111111")
                                         .s("2222222222222222222222222222222222222222222222222222222222222222")
                                         .v("1b")
                                         .build();
    final BigInteger r = new BigInteger(sig.getR(), 16);
    final BigInteger s = new BigInteger(sig.getS(), 16);
    
    // when
    final ECDSASignature result = SignatureUtil.transferToECDSASignature(sig);
    
    // then
    assertThat(result).isNotNull();
    assertThat(result.r).isEqualTo(r);
    assertThat(result.s).isEqualTo(s);
  }
  
  @Test
  public void test_transferToECDSASignature_sig_r_null() {
    // given
    final SpoSignature sig = SpoSignature.builder()
                                         .r(null)
                                         .s("2222222222222222222222222222222222222222222222222222222222222222")
                                         .v("1b")
                                         .build();
    
    // when
    final ECDSASignature result = SignatureUtil.transferToECDSASignature(sig);
    
    // then
    assertThat(result).isNull();
  }
  
  @Test
  public void test_transferToECDSASignature_sig_s_null() {
    // given
    final SpoSignature sig = SpoSignature.builder()
                                         .r("1111111111111111111111111111111111111111111111111111111111111111")
                                         .s(null)
                                         .v("1b")
                                         .build();
    
    // when
    final ECDSASignature result = SignatureUtil.transferToECDSASignature(sig);
    
    // then
    assertThat(result).isNull();
  }
  
  @Test
  public void test_transferToECDSASignature_sig_v_null() {
    // given
    final SpoSignature sig = SpoSignature.builder()
                                         .r("1111111111111111111111111111111111111111111111111111111111111111")
                                         .s("2222222222222222222222222222222222222222222222222222222222222222")
                                         .v(null)
                                         .build();
    final BigInteger r = new BigInteger(sig.getR(), 16);
    final BigInteger s = new BigInteger(sig.getS(), 16);
    
    // when
    final ECDSASignature result = SignatureUtil.transferToECDSASignature(sig);
    
    // then
    assertThat(result).isNotNull();
    assertThat(result.r).isEqualTo(r);
    assertThat(result.s).isEqualTo(s);
  }
  
  @Test
  public void test_transferToECDSASignature_sig_null() {
    assertThatThrownBy(() -> SignatureUtil.transferToECDSASignature(null)).isInstanceOf(NullPointerException.class);
  }
  
}
