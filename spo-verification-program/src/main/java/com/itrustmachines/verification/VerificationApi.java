package com.itrustmachines.verification;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.*;

import com.google.gson.Gson;
import com.itrustmachines.verification.service.VerifyVerificationProofService;
import com.itrustmachines.verification.util.VerificationProofParser;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerificationApi {
  
  public static final String INFURA_PROJECT_ID_OPT = "infuraProjectId";
  public static final String PROOF_OPT = "proof";
  public static final String RESULT_OPT = "result";
  
  private final VerifyVerificationProofService service;
  
  private VerificationApi() {
    this.service = new VerifyVerificationProofService();
    log.info("new instance: {}", this);
  }
  
  private static VerificationApi instance;
  
  @Synchronized
  public static VerificationApi getInstance() {
    if (VerificationApi.instance == null) {
      VerificationApi.instance = new VerificationApi();
    }
    return VerificationApi.instance;
  }
  
  public VerifyVerificationProofResult verify(@NonNull final String filePath, final String infuraProjectId) {
    final VerificationProof verificationProof = VerificationProofParser.parse(filePath);
    return verify(verificationProof, infuraProjectId);
  }
  
  public VerifyVerificationProofResult verifyJsonString(@NonNull final String jsonString,
      final String infuraProjectId) {
    final VerificationProof verificationProof = VerificationProofParser.parseJsonString(jsonString);
    return verify(verificationProof, infuraProjectId);
  }
  
  public VerifyVerificationProofResult verify(final VerificationProof proof, final String infuraProjectId) {
    log.debug("verify() proof={}", proof);
    VerifyVerificationProofResult result = null;
    if (proof != null) {
      result = service.verify(proof, infuraProjectId);
    }
    return result;
  }
  
  public static void main(String[] args) {
    final Options options = new Options();
    final Option filePathOption = Option.builder()
                                        .argName("filePath")
                                        .longOpt(PROOF_OPT)
                                        .hasArg()
                                        .desc("input verification proof file path (sample/queryByCO.json)")
                                        .optionalArg(false)
                                        .required()
                                        .build();
    final Option resultPathOption = Option.builder()
                                          .argName("filePath")
                                          .longOpt(RESULT_OPT)
                                          .hasArg()
                                          .desc("output verify result file path (result.json)")
                                          .optionalArg(false)
                                          .required()
                                          .build();
    final Option infuraProjectIdOption = Option.builder()
                                               .argName("infuraProjectId")
                                               .longOpt(INFURA_PROJECT_ID_OPT)
                                               .hasArg()
                                               .desc("required if env is MAINNET, KOVAN, GOERLI, RINKEBY, ROPSTEN")
                                               .optionalArg(true)
                                               .build();
    options.addOption(filePathOption);
    options.addOption(resultPathOption);
    options.addOption(infuraProjectIdOption);
    
    final CommandLineParser parser = new DefaultParser();
    try {
      final CommandLine line = parser.parse(options, args);
      
      String infuraProjectId = line.getOptionValue(INFURA_PROJECT_ID_OPT, null);
      log.debug("infuraProjectId={}", infuraProjectId);
      final String filePath = line.getOptionValue(PROOF_OPT);
      log.debug("filePath={}", filePath);
      final String resultPath = line.getOptionValue(RESULT_OPT);
      log.debug("resultPath={}", resultPath);
      
      final VerificationApi verificationApi = VerificationApi.getInstance();
      
      final VerifyVerificationProofResult result = verificationApi.verify(filePath, infuraProjectId);
      log.debug("result={}", result);
      
      final FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
      final Writer out = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
      out.write(new Gson().toJson(result));
      out.close();
    } catch (ParseException e) {
      log.error("error", e);
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("verification-api", options);
    } catch (Exception e) {
      log.error("verifiy error", e);
    }
  }
  
}