package com.itrustmachines.common.ethereum.embedded_evm.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.evm.Configuration;
import org.web3j.evm.EmbeddedWeb3jService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.contract.LedgerBooster;
import com.itrustmachines.common.util.KeyGeneratorUtil;

import lombok.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class EmbeddedEvmEnvUtil {
  
  public String NODE_URL = "http://embedded.evm";
  
  public final KeyGeneratorUtil.KeyInfo itmKeyInfo = KeyGeneratorUtil.generateKeyWithPassword("ITM");
  public final KeyGeneratorUtil.KeyInfo spoServerKeyInfo = KeyGeneratorUtil.generateKeyWithPassword("SPO_SERVER");
  public final long maxTxCount = 100_0000L;
  
  public EvmEnv evmEnv() throws Exception {
    return evmEnv(itmKeyInfo.getPrivateKey(), spoServerKeyInfo.getAddress());
  }
  
  public EvmEnv evmEnv(@NonNull final String itmPrivateKey, @NonNull final String spoWalletAddress) throws Exception {
    final Credentials credentials = Credentials.create(itmPrivateKey);
    final Configuration configuration = new Configuration(new Address(credentials.getAddress()), 9487);
    
    final Web3j web3j = Web3j.build(new EmbeddedWeb3jService(configuration));
    final ContractGasProvider gasProvider = new StaticGasProvider(DefaultGasProvider.GAS_PRICE,
        DefaultGasProvider.GAS_LIMIT);
    
    TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, spoWalletAddress,
        BigDecimal.valueOf(87L), Convert.Unit.ETHER)
                                                    .send();
    assertThat(transactionReceipt.isStatusOK()).isTrue();
    
    return EvmEnv.builder()
                 .nodeUrl(NODE_URL)
                 .web3j(web3j)
                 .contractGasProvider(gasProvider)
                 .build();
  }
  
  public String deployContract(@NonNull final EvmEnv evmEnv) {
    return deployContract(EmbeddedEvmEnvUtil.DeployContractInput.builder()
                                                                .nodeUrl(evmEnv.getNodeUrl())
                                                                .itmPrivateKey(itmKeyInfo.getPrivateKey())
                                                                .spoServerWalletAddress(spoServerKeyInfo.getAddress())
                                                                .maxTxCount(maxTxCount)
                                                                .build(),
        evmEnv);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DeployContractInput {
    
    String nodeUrl;
    String itmPrivateKey;
    String spoServerWalletAddress;
    Long maxTxCount;
    
  }
  
  public String deployContract(@NonNull final DeployContractInput input, @NonNull final EvmEnv evmEnv) {
    log.debug("deployContract() input={}, evmEnv={}", input, evmEnv);
    
    final Web3j web3j = evmEnv.getWeb3j();
    final Credentials itmCredentials = Credentials.create(input.itmPrivateKey);
    ContractGasProvider gasProvider = evmEnv.getContractGasProvider();
    
    String contractAddress = null;
    try {
      final LedgerBooster deployedContract = LedgerBooster.deploy(web3j, itmCredentials, gasProvider,
          itmCredentials.getAddress(), input.spoServerWalletAddress, BigInteger.valueOf(input.maxTxCount))
                                                          .send();
      log.debug("deployedContract={}",
          ReflectionToStringBuilder.toString(deployedContract, ToStringStyle.SHORT_PREFIX_STYLE));
      contractAddress = deployedContract.getContractAddress();
    } catch (Exception e) {
      log.error("deployContractWithEvmEnv() error, input={}", input, e);
    }
    log.debug("deployContractWithEvmEnv() result contractAddress={}", contractAddress);
    return contractAddress;
  }
  
}
