package com.itrustmachines.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class HashUtilsTest {
  
  public static final String SAMPLE_HEX_UPPER_AND_LOWER_CASE = "F0e1D2C3b4a5968778695A4B3c2d1E0f";
  public static final String SAMPLE_HEX_UPPER_CASE = SAMPLE_HEX_UPPER_AND_LOWER_CASE.toUpperCase();
  public static final String SAMPLE_HEX_LOWER_CASE = SAMPLE_HEX_UPPER_AND_LOWER_CASE.toLowerCase();
  public static final String SAMPLE_HEX_EMPTY = "";
  public static final String SAMPLE_HEX_BLANK = "    ";
  public static final String SAMPLE_NON_HEX_STRING = "!\"#$%&'()*+,-./0@A[\\]^_`a{|}~";
  public static final String SAMPLE_NON_HEX_STRING_HEX = "2122232425262728292a2b2c2d2e2f3040415b5c5d5e5f60617b7c7d7e";
  public static final byte[] SAMPLE_BYTES = { (byte) 0xf0, (byte) 0xe1, (byte) 0xd2, (byte) 0xc3, (byte) 0xb4,
      (byte) 0xa5, (byte) 0x96, (byte) 0x87, (byte) 0x78, (byte) 0x69, (byte) 0x5a, (byte) 0x4b, (byte) 0x3c,
      (byte) 0x2d, (byte) 0x1e, (byte) 0x0f };
  public static final byte[] SAMPLE_BYTES_EMPTY = {};
  public static final byte[] SAMPLE_NON_HEX_STRING_BYTES = { (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24,
      (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c,
      (byte) 0x2d, (byte) 0x2e, (byte) 0x2f, (byte) 0x30, (byte) 0x40, (byte) 0x41, (byte) 0x5b, (byte) 0x5c,
      (byte) 0x5d, (byte) 0x5e, (byte) 0x5f, (byte) 0x60, (byte) 0x61, (byte) 0x7b, (byte) 0x7c, (byte) 0x7d,
      (byte) 0x7e };
  
  public static final String SAMPLE_TO_SHA256_STRING = SAMPLE_NON_HEX_STRING;
  public static final byte[] SAMPLE_TO_SHA256_BYTES = SAMPLE_NON_HEX_STRING_BYTES;
  public static final byte[] SAMPLE_TO_SHA256_BYTES_PART1 = { (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24,
      (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28 };
  public static final byte[] SAMPLE_TO_SHA256_BYTES_PART2 = { (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c,
      (byte) 0x2d, (byte) 0x2e, (byte) 0x2f, (byte) 0x30 };
  public static final byte[] SAMPLE_TO_SHA256_BYTES_PART3 = { (byte) 0x40, (byte) 0x41, (byte) 0x5b, (byte) 0x5c,
      (byte) 0x5d, (byte) 0x5e, (byte) 0x5f, (byte) 0x60 };
  public static final byte[] SAMPLE_TO_SHA256_BYTES_PART4 = { (byte) 0x61, (byte) 0x7b, (byte) 0x7c, (byte) 0x7d,
      (byte) 0x7e };
  public static final String SAMPLE_HASH_STRING = "ACCA71BB9EED46060D384F426590D386D6AB3532311EB049E1748C1D37CC4759";
  public static final byte[] SAMPLE_HASH_BYTES = { //
      (byte) 0xAC, (byte) 0xCA, (byte) 0x71, (byte) 0xBB, (byte) 0x9E, (byte) 0xED, (byte) 0x46, (byte) 0x06, //
      (byte) 0x0D, (byte) 0x38, (byte) 0x4F, (byte) 0x42, (byte) 0x65, (byte) 0x90, (byte) 0xD3, (byte) 0x86, //
      (byte) 0xD6, (byte) 0xAB, (byte) 0x35, (byte) 0x32, (byte) 0x31, (byte) 0x1E, (byte) 0xB0, (byte) 0x49, //
      (byte) 0xE1, (byte) 0x74, (byte) 0x8C, (byte) 0x1D, (byte) 0x37, (byte) 0xCC, (byte) 0x47, (byte) 0x59 };
  public static final byte[] SAMPLE_HASH_BYTES_EMPTY = { //
      (byte) 0xE3, (byte) 0xB0, (byte) 0xC4, (byte) 0x42, (byte) 0x98, (byte) 0xFC, (byte) 0x1C, (byte) 0x14, //
      (byte) 0x9A, (byte) 0xFB, (byte) 0xF4, (byte) 0xC8, (byte) 0x99, (byte) 0x6F, (byte) 0xB9, (byte) 0x24, //
      (byte) 0x27, (byte) 0xAE, (byte) 0x41, (byte) 0xE4, (byte) 0x64, (byte) 0x9B, (byte) 0x93, (byte) 0x4C, //
      (byte) 0xA4, (byte) 0x95, (byte) 0x99, (byte) 0x1B, (byte) 0x78, (byte) 0x52, (byte) 0xB8, (byte) 0x55 };
  
  @Test
  public void test_hex2byte_upper_and_lower_case() {
    // when
    final byte[] result = HashUtils.hex2byte(SAMPLE_HEX_UPPER_AND_LOWER_CASE);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_BYTES);
  }
  
  @Test
  public void test_hex2byte_upper_case() {
    // when
    final byte[] result = HashUtils.hex2byte(SAMPLE_HEX_UPPER_CASE);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_BYTES);
  }
  
  @Test
  public void test_hex2byte_lower_case() {
    // when
    final byte[] result = HashUtils.hex2byte(SAMPLE_HEX_LOWER_CASE);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_BYTES);
  }
  
  @Test
  public void test_hex2byte_empty() {
    // when
    final byte[] result4 = HashUtils.hex2byte(SAMPLE_HEX_EMPTY);
    
    // then
    assertThat(result4).isNotNull()
                       .isEqualTo(SAMPLE_BYTES_EMPTY);
  }
  
  @Test
  public void test_hex2byte_blank() {
    // when
    final byte[] result4 = HashUtils.hex2byte(SAMPLE_HEX_BLANK);
    
    // then
    assertThat(result4).isNull();
  }
  
  @Test
  public void test_hex2byte_non_hex_string() {
    // when
    final byte[] result5 = HashUtils.hex2byte(SAMPLE_NON_HEX_STRING);
    
    // then
    assertThat(result5).isNull();
  }
  
  @Test
  public void test_hex2byte_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> HashUtils.hex2byte(null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_byte2HEX() {
    // when
    final String result = HashUtils.byte2HEX(SAMPLE_BYTES);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_HEX_UPPER_CASE);
  }
  
  @Test
  public void test_byte2HEX_empty() {
    // when
    final String result = HashUtils.byte2HEX(SAMPLE_BYTES_EMPTY);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_HEX_EMPTY);
  }
  
  @Test
  public void test_byte2HEX_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> HashUtils.byte2HEX(null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_byte2hex() {
    // when
    final String result = HashUtils.byte2hex(SAMPLE_BYTES);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_HEX_LOWER_CASE);
  }
  
  @Test
  public void test_byte2hex_empty() {
    // when
    final String result = HashUtils.byte2hex(SAMPLE_BYTES_EMPTY);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_HEX_EMPTY);
  }
  
  @Test
  public void test_byte2hex_non_hex_string() {
    // when
    final String result = HashUtils.byte2hex(SAMPLE_NON_HEX_STRING);
    
    // then
    assertThat(result).isNotNull()
                      .isEqualTo(SAMPLE_NON_HEX_STRING_HEX);
  }
  
  @Test
  public void test_byte2hex_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> HashUtils.byte2hex((String) null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_Collections() {
    // when
    final byte[] hash = HashUtils.sha256(Arrays.asList(SAMPLE_TO_SHA256_BYTES_PART1, SAMPLE_TO_SHA256_BYTES_PART2,
        SAMPLE_TO_SHA256_BYTES_PART3, SAMPLE_TO_SHA256_BYTES_PART4));
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES);
  }
  
  @Test
  public void test_sha256_Collections_empty() {
    // when
    final byte[] hash = HashUtils.sha256(Collections.emptyList());
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES_EMPTY);
  }
  
  @Test
  public void test_sha256_Collections_contains_null() {
    // noinspection CodeBlock2Expr
    assertThatThrownBy(() -> {
      HashUtils.sha256(Arrays.asList(null, SAMPLE_TO_SHA256_BYTES_PART1, SAMPLE_TO_SHA256_BYTES_PART2,
          SAMPLE_TO_SHA256_BYTES_PART3, SAMPLE_TO_SHA256_BYTES_PART4));
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_Collections_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> HashUtils.sha256((Collection<byte[]>) null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_varargs() {
    // when
    final byte[] hash = HashUtils.sha256(SAMPLE_TO_SHA256_BYTES_PART1, SAMPLE_TO_SHA256_BYTES_PART2,
        SAMPLE_TO_SHA256_BYTES_PART3, SAMPLE_TO_SHA256_BYTES_PART4);
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES);
  }
  
  @Test
  public void test_sha256_varargs_empty() {
    // when
    final byte[] hash = HashUtils.sha256();
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES_EMPTY);
  }
  
  @Test
  public void test_sha256_varargs_contains_null() {
    // noinspection CodeBlock2Expr
    assertThatThrownBy(() -> {
      HashUtils.sha256(null, SAMPLE_TO_SHA256_BYTES_PART1, SAMPLE_TO_SHA256_BYTES_PART2, SAMPLE_TO_SHA256_BYTES_PART3,
          SAMPLE_TO_SHA256_BYTES_PART4);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_varargs_null() {
    assertThatThrownBy(() -> HashUtils.sha256((byte[]) null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_InputStream() {
    // when
    final byte[] hash = HashUtils.sha256(new ByteArrayInputStream(SAMPLE_TO_SHA256_BYTES));
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES);
  }
  
  @Test
  public void test_sha256_InputStream_empty() {
    // when
    final byte[] hash = HashUtils.sha256(new ByteArrayInputStream(new byte[] {}));
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualTo(SAMPLE_HASH_BYTES_EMPTY);
  }
  
  @Test
  public void test_sha256_InputStream_with_IOException() {
    // given
    final InputStream inputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("test_sha256_InputStream_with_IOException");
      }
    };
    
    // when
    final byte[] hash = HashUtils.sha256(inputStream);
    
    // then
    assertThat(hash).isNull();
  }
  
  @Test
  public void test_sha256_InputStream_null() {
    assertThatThrownBy(() -> HashUtils.sha256((InputStream) null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_file() throws Exception {
    // given
    final URL url = getClass().getClassLoader()
                              .getResource("sample.txt");
    assertThat(url).isNotNull();
    final File file = Paths.get(url.toURI())
                           .toFile();
    
    // when
    final String hash = HashUtils.sha256(file);
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualToIgnoringCase(SAMPLE_HASH_STRING);
  }
  
  @Test
  public void test_sha256_file_empty() throws Exception {
    // given
    final URL url = getClass().getClassLoader()
                              .getResource("empty.txt");
    assertThat(url).isNotNull();
    final File file = Paths.get(url.toURI())
                           .toFile();
    
    // when
    final String hash = HashUtils.sha256(file);
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualToIgnoringCase(HashUtils.byte2hex(SAMPLE_HASH_BYTES_EMPTY));
  }
  
  @Test
  public void test_sha256_file_not_found() {
    // given
    final File file = new File("");
    
    // when
    final String hash = HashUtils.sha256(file);
    
    // then
    assertThat(hash).isNull();
  }
  
  @Test
  public void test_sha256_file_null() {
    assertThatThrownBy(() -> HashUtils.sha256((File) null)).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_sha256_string() {
    // when
    final String hash = HashUtils.sha256(SAMPLE_TO_SHA256_STRING);
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualToIgnoringCase(SAMPLE_HASH_STRING);
  }
  
  @Test
  public void test_sha256_string_empty() {
    // when
    final String hash = HashUtils.sha256("");
    
    // then
    assertThat(hash).isNotNull()
                    .isEqualToIgnoringCase(HashUtils.byte2hex(SAMPLE_HASH_BYTES_EMPTY));
  }
  
  @Test
  public void test_sha256_string_null() {
    // noinspection ConstantConditions
    assertThatThrownBy(() -> HashUtils.sha256((String) null)).isInstanceOf(NullPointerException.class);
  }
  
}
