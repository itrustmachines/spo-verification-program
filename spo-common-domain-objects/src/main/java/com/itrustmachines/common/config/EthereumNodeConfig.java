package com.itrustmachines.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EthereumNodeConfig {
  
  private String privateKeyEnv;
  private String nodeUrl;
  private String infuraProjectIdEnv;
  private String blockchainExplorerUrl;
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Authentication {
    
    Boolean needAuth;
    String username;
    String password;
    
  }
  
  private Authentication authentication;
  
}
