package com.itrustmachines.common.ethereum.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class BlockchainExplorerUtil {
  
  public String contractUrl(final String explorerType, @NonNull String explorerUrl, @NonNull final String address) {
    if (!explorerUrl.endsWith("/")) {
      explorerUrl = explorerUrl + "/";
    }
    String url = null;
    switch (explorerType) {
      case BlockchainExplorerTypes.EPIRUS: {
        url = String.format("%scontracts/%s", explorerUrl, address);
      }
        break;
      case BlockchainExplorerTypes.ETHERSCAN:
      case BlockchainExplorerTypes.BLOCKSCOUT:
      default: {
        url = String.format("%saddress/%s", explorerUrl, address);
      }
        break;
    }
    log.debug("contractUrl() url={}, explorerType={}, explorerUrl={}, address={}", url, explorerType, explorerUrl,
        address);
    return url;
  }
  
  public String addressUrl(final String explorerType, @NonNull String explorerUrl, @NonNull final String address) {
    if (!explorerUrl.endsWith("/")) {
      explorerUrl = explorerUrl + "/";
    }
    String url = null;
    switch (explorerType) {
      case BlockchainExplorerTypes.EPIRUS: {
        url = String.format("%saccounts/%s", explorerUrl, address);
      }
        break;
      case BlockchainExplorerTypes.ETHERSCAN:
      case BlockchainExplorerTypes.BLOCKSCOUT:
      default: {
        url = String.format("%saddress/%s", explorerUrl, address);
      }
        break;
    }
    log.debug("addressUrl() url={}, explorerType={}, explorerUrl={}, address={}", url, explorerType, explorerUrl,
        address);
    return url;
  }
  
  public String txHashUrl(final String explorerType, @NonNull String explorerUrl, @NonNull final String txHash) {
    if (!explorerUrl.endsWith("/")) {
      explorerUrl = explorerUrl + "/";
    }
    String url = null;
    switch (explorerType) {
      case BlockchainExplorerTypes.EPIRUS: {
        url = String.format("%stransactions/%s", explorerUrl, txHash);
      }
        break;
      case BlockchainExplorerTypes.ETHERSCAN:
      case BlockchainExplorerTypes.BLOCKSCOUT:
      default: {
        url = String.format("%stx/%s", explorerUrl, txHash);
      }
        break;
    }
    log.debug("txHashUrl() url={}, explorerType={}, explorerUrl={}, txHash={}", url, explorerType, explorerUrl, txHash);
    return url;
  }
  
}
