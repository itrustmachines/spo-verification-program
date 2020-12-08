package com.itrustmachines.common.ethereum.config;

import lombok.*;

@Data
@ToString(exclude = { "privateKeyEnv" })
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EthereumNodeConfig {
  
  private String privateKeyEnv;
  private String nodeUrl;
  private String infuraProjectIdEnv;
  private Double gasPriceMultiply;
  
  // support different explorer type in verification
  private String blockchainExplorerUrl;
  
  /**
   * @see com.itrustmachines.common.ethereum.util.BlockchainExplorerTypes
   */
  private String explorerType;
  
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
