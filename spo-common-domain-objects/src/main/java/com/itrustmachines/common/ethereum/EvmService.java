package com.itrustmachines.common.ethereum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class EvmService {
  
  @Getter
  final private EvmEnv evmEnv;
  
  final private ReentrantLock lock;
  
  private EvmService(final EvmEnv evmEnv) {
    this.evmEnv = evmEnv;
    this.lock = new ReentrantLock();
    log.info("new instance={}", this);
  }
  
  // key: nodeUrl
  private static final Map<String, EvmService> INSTANCE_MAP = new HashMap<>();
  
  @Synchronized
  public static EvmService getInstance(@NonNull final EvmEnv evmEnv) {
    final String nodeUrl = evmEnv.getNodeUrl();
    if (!INSTANCE_MAP.containsKey(nodeUrl)) {
      INSTANCE_MAP.put(nodeUrl, new EvmService(evmEnv));
    }
    return INSTANCE_MAP.get(nodeUrl);
  }
  
  public BigInteger getGasPrice() {
    log.debug("getGasPrice() start");
    
    lock.lock();
    BigInteger result;
    try {
      result = this.evmEnv.getWeb3j()
                          .ethGasPrice()
                          .send()
                          .getGasPrice();
    } catch (Exception e) {
      final String errMsg = String.format("getGasPrice() error, evmEnv=%s", evmEnv.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getGasPrice() result={}", result);
    return result;
  }
  
  public BigInteger getGasPrice(final double multiply) {
    return BigInteger.valueOf((long) (getGasPrice().doubleValue() * multiply));
  }
  
  /**
   * @return balance in wei
   */
  public BigInteger getBalance(@NonNull final String walletAddress) {
    log.debug("getBalance() start, walletAddress={}", walletAddress);
    
    BigInteger result = BigInteger.ZERO;
    EthGetBalance ethGetBalance;
    lock.lock();
    try {
      ethGetBalance = this.evmEnv.getWeb3j()
                                 .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                                 .send();
      if (ethGetBalance != null) {
        result = ethGetBalance.getBalance();
      }
    } catch (Exception e) {
      final String errMsg = String.format("getBalance() error, walletAddress=%s, Web3jService=%s", walletAddress,
          this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    
    log.debug("getBalance() walletAddress={}, balance={}", walletAddress, result);
    return result;
  }
  
  public BigDecimal getBalanceInEther(@NonNull final String walletAddress) {
    log.debug("getBalanceInEther() start, walletAddress={}", walletAddress);
    final BigInteger balance = getBalance(walletAddress);
    final BigDecimal balanceInEther = Convert.fromWei("" + balance, Convert.Unit.ETHER);
    log.debug("getBalanceInEther() walletAddress={}, balanceInEther={}", walletAddress, balanceInEther);
    return balanceInEther;
  }
  
  public EthBlock.Block getLatestBlock() {
    log.debug("getLatestBlock() start");
    
    EthBlock.Block result;
    lock.lock();
    try {
      result = this.evmEnv.getWeb3j()
                          .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                          .send()
                          .getBlock();
    } catch (Exception e) {
      final String errMsg = String.format("getLatestBlock() error, Web3jService=%s", this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getLatestBlock() result={}", result);
    return result;
  }
  
  public Optional<BigDecimal> getTransactionFeeInEther(@NonNull final String txHash) {
    log.debug("getTransactionFeeInEther() txHash={}", txHash);
    final TransactionReceipt txReceipt = getTransactionReceipt(txHash);
    final Transaction tx = getTransaction(txHash);
    
    BigDecimal result = null;
    if (txReceipt != null && tx != null && txReceipt.getGasUsed() != null && tx.getGasPrice() != null) {
      result = Convert.fromWei("" + txReceipt.getGasUsed()
                                             .multiply(tx.getGasPrice())
                                             .longValue(),
          Convert.Unit.ETHER);
    }
    log.debug("getTransactionFeeInEther() txHash={}, result={}", txHash, result);
    return Optional.ofNullable(result);
  }
  
  public TransactionReceipt getTransactionReceipt(@NonNull final String txHash) {
    log.debug("getTransactionReceipt() txHash={}, evmEnv={}", txHash, evmEnv);
    
    TransactionReceipt result = null;
    lock.lock();
    try {
      final Optional<TransactionReceipt> _txReceipt = this.evmEnv.getWeb3j()
                                                                 .ethGetTransactionReceipt(txHash)
                                                                 .send()
                                                                 .getTransactionReceipt();
      if (_txReceipt.isPresent()) {
        result = _txReceipt.get();
      }
    } catch (Exception e) {
      final String errMsg = String.format("getTransactionReceipt() error, txHash=%s, Web3jService=%s", txHash,
          this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getTransactionReceipt() txHash={}, result={}", txHash, result);
    return result;
  }
  
  public Transaction getTransaction(@NonNull final String txHash) {
    log.debug("getTransaction() txHash={}, evmEnv={}", txHash, evmEnv);
    
    Transaction result = null;
    lock.lock();
    try {
      final Optional<Transaction> _tx = this.evmEnv.getWeb3j()
                                                   .ethGetTransactionByHash(txHash)
                                                   .send()
                                                   .getTransaction();
      if (_tx.isPresent()) {
        result = _tx.get();
      }
    } catch (Exception e) {
      final String errMsg = String.format("getTransaction() error, txHash=%s, Web3jService=%s", txHash,
          this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getTransaction() txHash={}, result={}", txHash, result);
    return result;
  }
  
  public TransactionReceipt sendEther(@NonNull final Credentials credentials, @NonNull final String toAddress,
      @NonNull final BigDecimal transferBalance) {
    log.debug("sendEther() toAddress={}, transferBalance={}", toAddress, transferBalance);
    lock.lock();
    TransactionReceipt receipt = null;
    try {
      receipt = Transfer.sendFunds(this.evmEnv.getWeb3j(), credentials, toAddress, transferBalance, Convert.Unit.ETHER)
                        .send();
      return receipt;
    } catch (Exception e) {
      final String errMsg = String.format("sendEther() error, toAddress=%s, transferBalance=%s", toAddress,
          transferBalance);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
  }
  
}
