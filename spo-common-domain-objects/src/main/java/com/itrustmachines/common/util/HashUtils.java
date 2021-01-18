package com.itrustmachines.common.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HashUtils {
  
  private final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  
  public byte[] hex2byte(String input) {
    if (input == null) {
      return null;
    }
    try {
      byte[] b = new byte[input.length() / 2];
      for (int i = 0; i < b.length; i++) {
        int index = i * 2;
        int v = Integer.parseInt(input.substring(index, index + 2), 16);
        b[i] = (byte) v;
      }
      return b;
    } catch (NumberFormatException e) {
      log.warn("hex2byte() error, input={}", input, e);
    }
    return null;
  }
  
  public String byte2HEX(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
  
  public String byte2hex(byte[] bytes) {
    return byte2HEX(bytes).toLowerCase();
  }
  
  public String byte2hex(String data) {
    return byte2hex(data.getBytes(StandardCharsets.UTF_8));
  }
  
  public byte[] sha256(Collection<byte[]> bytesCollection) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      
      for (byte[] bytes : bytesCollection) {
        md.update(bytes);
      }
      
      return md.digest();
    } catch (NoSuchAlgorithmException e) {
      log.warn("sha256() Collection<byte[]> error", e);
    }
    return null;
  }
  
  public byte[] sha256(byte[]... bytesArr) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      
      for (byte[] bytes : bytesArr) {
        md.update(bytes);
      }
      
      return md.digest();
    } catch (NoSuchAlgorithmException ex) {
      log.warn("sha256() byte[]... error", ex);
    }
    return null;
  }
  
  public byte[] sha256(@NonNull final InputStream is) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      BufferedInputStream bis = new BufferedInputStream(is);
      
      try (DigestInputStream dis = new DigestInputStream(bis, md)) {
        // noinspection StatementWithEmptyBody
        while (dis.read() != -1)
          ;
      }
      
      return md.digest();
    } catch (NoSuchAlgorithmException | IOException e) {
      log.warn("sha256() InputStream error", e);
    }
    return null;
  }
  
  public String sha256(File file) {
    try {
      return byte2hex(sha256(new FileInputStream(file)));
    } catch (FileNotFoundException e) {
      log.warn("sha256() File error, file={}", file, e);
    }
    return null;
  }
  
  public String sha256(String data) {
    return byte2hex(sha256(data.getBytes(StandardCharsets.UTF_8)));
  }
  
}