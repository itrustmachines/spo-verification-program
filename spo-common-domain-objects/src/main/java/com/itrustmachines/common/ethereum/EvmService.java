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
  public static EvmService getInstance(final @NonNull EvmEnv evmEnv) {
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
  
  public BigInteger getBalance(final @NonNull String walletAddress) {
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
  
  public BigInteger getBalanceInEther(final @NonNull String walletAddress) {
    log.debug("getBalanceInEther() start, walletAddress={}", walletAddress);
    final BigInteger balance = getBalance(walletAddress);
    final BigInteger balanceInEther = Convert.fromWei("" + balance, Convert.Unit.ETHER)
                                             .toBigInteger();
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
  
  public Transaction getTransaction(final @NonNull String txhash) {
    log.debug("getTransaction() txHash={}, evmEnv={}", txhash, evmEnv);
    
    Transaction result = null;
    lock.lock();
    try {
      final Optional<Transaction> _tx = this.evmEnv.getWeb3j()
                                                   .ethGetTransactionByHash(txhash)
                                                   .send()
                                                   .getTransaction();
      if (_tx.isPresent()) {
        result = _tx.get();
      }
    } catch (Exception e) {
      final String errMsg = String.format("getTransaction() error, txhash=%s, Web3jService=%s", txhash,
          this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getTransaction() result={}", result);
    return result;
  }
  
  public TransactionReceipt getTransactionReceipt(@NonNull final String txhash) {
    log.debug("getTransactionReceipt() txHash={}, evmEnv={}", txhash, evmEnv);
    
    TransactionReceipt result = null;
    lock.lock();
    try {
      final Optional<TransactionReceipt> _txReceipt = this.evmEnv.getWeb3j()
                                                                 .ethGetTransactionReceipt(txhash)
                                                                 .send()
                                                                 .getTransactionReceipt();
      if (_txReceipt.isPresent()) {
        result = _txReceipt.get();
      }
    } catch (Exception e) {
      final String errMsg = String.format("getTransactionReceipt() error, txhash=%s, Web3jService=%s", txhash,
          this.toString());
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
    log.debug("getTransaction() result={}", result);
    return result;
  }
  
  public BigInteger getTxGasPrice(final @NonNull String txhash) {
    return getTransaction(txhash).getGasPrice();
  }
  
  public BigInteger getTxGasUsed(final @NonNull String txhash) {
    return getTransaction(txhash).getGas();
  }
  
  public TransactionReceipt sendEther(final @NonNull Credentials credentials, final @NonNull String toAddress,
      final @NonNull BigDecimal transferBalance) {
    log.debug("sendBalance() transferBalance={}", transferBalance);
    lock.lock();
    TransactionReceipt receipt = null;
    try {
      receipt = Transfer.sendFunds(this.evmEnv.getWeb3j(), credentials, toAddress, transferBalance, Convert.Unit.ETHER)
                        .send();
      return receipt;
    } catch (Exception e) {
      final String errMsg = String.format("sendBalance() error, toAddress=%s, transferBalance=%s", toAddress,
          transferBalance);
      log.error(errMsg, e);
      throw new RuntimeException(errMsg);
    } finally {
      lock.unlock();
    }
  }
  
}
