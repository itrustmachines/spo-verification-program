package com.itrustmachines.verification.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.itrustmachines.verification.vo.VerificationProof;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class VerificationProofParser {
  
  final private Gson gson = new Gson();
  
  @SneakyThrows
  public VerificationProof parse(@NonNull final String filePath) {
    final Path path = Paths.get(filePath);
    return parse(path);
  }
  
  @SneakyThrows
  public VerificationProof parse(@NonNull final Path path) {
    if (Files.isRegularFile(path)) {
      final StringBuilder sb = new StringBuilder();
      for (String line : Files.readAllLines(path)) {
        sb.append(line);
      }
      return parseJsonString(sb.toString());
    } else {
      return null;
    }
  }
  
  @SneakyThrows
  public VerificationProof parseJsonString(@NonNull final String jsonString) {
    final VerificationProof result = gson.fromJson(jsonString, VerificationProof.class);
    log.debug("parseJsonString() result={}", result);
    return result;
  }
  
}
