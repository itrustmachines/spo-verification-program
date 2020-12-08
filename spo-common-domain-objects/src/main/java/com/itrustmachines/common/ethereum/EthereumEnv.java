package com.itrustmachines.common.ethereum;

public enum EthereumEnv {
  
  MAINNET,
  
  // ref: https://ethereum.org/en/developers/docs/networks/
  ROPSTEN, RINKEBY, KOVAN, GOERLI,
  
  // auzre quorum
  AZURE_QUORUM,
  
  // custom geth
  PRIVATE_GETH,
  
  ERROR_ENV

}
