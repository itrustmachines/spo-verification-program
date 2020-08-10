package com.itrustmachines.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HashUtils {
  
  private final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  
  public byte[] hex2byte(String input) {
    byte[] b = new byte[input.length() / 2];
    try {
      for (int i = 0; i < b.length; i++) {
        int index = i * 2;
        int v = Integer.parseInt(input.substring(index, index + 2), 16);
        b[i] = (byte) v;
      }
    } catch (Exception e) {
      log.warn("hex2byte() error, input={}", input, e);
    }
    return b;
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
  
  public byte[] sha256(Collection<byte[]> bytesCollection) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      
      for (byte[] bytes : bytesCollection) {
        md.update(bytes);
      }
      
      return md.digest();
    } catch (Exception e) {
      log.warn("sha256 Collection<byte[]> error", e);
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
      log.warn("sha256 byte[]... error", ex);
    }
    return null;
  }
  
  public String sha256(File file) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      InputStream is = new FileInputStream(file);
      byte[] buffer = new byte[8192];
      
      try (DigestInputStream dis = new DigestInputStream(is, md)) {
        while (dis.read(buffer) != -1)
          ;
      }
      
      return byte2hex(md.digest());
    } catch (Exception e) {
      log.warn("sha256 error, file={}", file, e);
    }
    return null;
  }
  
  public String sha256(String data) {
    return byte2hex(data.getBytes());
  }
  
}