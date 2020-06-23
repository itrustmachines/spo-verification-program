package com.itrustmachines.verification.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import com.itrustmachines.common.contract.ClearanceRecordService;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.util.ClearanceRecordVerifyUtil;
import com.itrustmachines.verification.util.QueryStringParser;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.vo.*;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyVerificationProofService {
  
  private final String serverWalletAddress;
  private final ClearanceRecordService clearanceRecordService;
  private final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService;
  
  public VerifyVerificationProofService(final @NonNull String serverWalletAddress,
      final @NonNull ClearanceRecordService clearanceRecordService) {
    this.serverWalletAddress = serverWalletAddress;
    this.clearanceRecordService = clearanceRecordService;
    this.verifyReceiptAndMerkleProofService = new VerifyReceiptAndMerkleProofService(serverWalletAddress,
        clearanceRecordService);
    log.info("new instance={}", this);
  }
  
  public VerifyVerificationProofResult verify(final @NonNull VerificationProof proof) {
    log.debug("verify() begin, proof={}", proof);
    if (!serverWalletAddress.equalsIgnoreCase(proof.getServerWalletAddress())) {
      final String errMsg = String.format(
          "ServerWalletAddress not the same, serverWalletAddress=%s, proof's serverWalletAddress=%s",
          serverWalletAddress, proof.getServerWalletAddress());
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
    
    final ClearanceRecord latestCR = clearanceRecordService.obtainClearanceRecord(findLatestCO(proof));
    log.debug("latestCR={}", latestCR);
    return verify(proof, latestCR);
  }
  
  long findLatestCO(final @NonNull VerificationProof proof) {
    long latestCO = -1;
    for (ClearanceRecord cr : proof.getClearanceRecords()) {
      if (cr.getClearanceOrder() > latestCO) {
        latestCO = cr.getClearanceOrder();
      }
    }
    return latestCO;
  }
  
  VerifyVerificationProofResult verify(final @NonNull VerificationProof proof,
      final @NonNull ClearanceRecord latestCR) {
    log.debug("verify() proof={}, latestCR={}", proof, latestCR);
    
    final boolean crListPass = verifyClearanceRecordsByLatestClearanceRecord(proof.getClearanceRecords(), latestCR);
    log.debug("clearanceRecordPass={}", crListPass);
    
    final List<VerifyReceiptAndMerkleProofResult> existProofResults = new ArrayList<>();
    
    final QueryStringParser.Query query = QueryStringParser.parse(proof.getQuery());
    final List<VerifyNotExistProofResult> notExistProofResults = new ArrayList<>();
    final List<String> rawDataList = new ArrayList<>();
    
    boolean pass = crListPass;
    for (final ExistenceProof existenceProof : proof.getExistenceProofs()) {
      ClearanceRecord contractClearanceRecord = null;
      final long co = existenceProof.getClearanceOrder();
      for (ClearanceRecord cr : proof.getClearanceRecords()) {
        if (NumberUtils.compare(cr.getClearanceOrder(), co) == 0) {
          contractClearanceRecord = cr;
          break;
        }
      }
      
      if (contractClearanceRecord == null) {
        log.error("no contractClearanceRecord for CO={}", co);
        continue;
      }
      
      if (existenceProof.isExist()) {
        // verify exist proof
        final VerifyReceiptAndMerkleProofResult result = verifyReceiptAndMerkleProofService.verify(
            existenceProof.getReceipt(), existenceProof.getMerkleProof(), contractClearanceRecord);
        rawDataList.add(existenceProof.getReceipt()
                                      .getCmd());
        pass &= result.isPass();
        log.debug("verify pass={}, result={}", pass, result.isPass());
        existProofResults.add(result);
      } else {
        // verify not exist proof
        final VerifyNotExistProofResult verifyNotExistProofResult = verifyNotExistProof(existenceProof,
            contractClearanceRecord, query.getFromCO(), query.getToCO());
        notExistProofResults.add(verifyNotExistProofResult);
      }
    }
    
    final VerifyVerificationProofResult result = VerifyVerificationProofResult.builder()
                                                                              .query(proof.getQuery())
                                                                              .pass(pass)
                                                                              .clearanceRecordPass(crListPass)
                                                                              .verifyReceiptResults(existProofResults)
                                                                              .verifyNotExistProofResults(
                                                                                  notExistProofResults)
                                                                              .rawDataList(rawDataList)
                                                                              .build();
    log.debug("verify() result={}", result);
    return result;
  }
  
  VerifyNotExistProofResult verifyNotExistProof(ExistenceProof proof, ClearanceRecord contractClearanceRecord,
      long fromCO, long toCO) {
    final boolean isMerkleProofSignature = verifyMerkleProofSignature(proof.getMerkleProof());
    
    VerifyNotExistProofResult.VerifyNotExistProofStatus verifyNotExistProofStatus;
    if (isMerkleProofSignature) {
      verifyNotExistProofStatus = verifyNotExistMerkleProofAndReceipt(proof, fromCO, toCO, contractClearanceRecord);
    } else {
      verifyNotExistProofStatus = VerifyNotExistProofResult.VerifyNotExistProofStatus.ERROR_SIGNATURE;
    }
    
    return VerifyNotExistProofResult.builder()
                                    .signatureOk(isMerkleProofSignature)
                                    .clearanceOrder(proof.getClearanceOrder())
                                    .indexValue(proof.getIndexValue())
                                    .result(verifyNotExistProofStatus)
                                    .build();
  }
  
  boolean verifyMerkleProofSignature(final @NonNull MerkleProof merkleProof) {
    log.debug("verifyMerkleProofSignature() start, merkleProof={}", merkleProof);
    final SpoSignature sigServer = merkleProof.getSigServer();
    // verify isMerkleproofSignatureOk
    final ECDSASignature merkleproofSignature = SignatureUtil.transferToECDSASignature(sigServer);
    final boolean result = verifySignature(merkleproofSignature, merkleProof.toSignDataSha3());
    log.debug("result={}", result);
    return result;
    
  }
  
  boolean verifySignature(final @NonNull ECDSASignature sig, final @NonNull byte[] message) {
    log.debug("verifySignature()  sig={}", sig);
    boolean match = false;
    String addressRecovered = null;
    for (int i = 0; i < 4; i++) {
      BigInteger publicKey = null;
      try {
        publicKey = Sign.recoverFromSignature((byte) i, sig, message);
      } catch (Exception e) {
        log.error("verifySignature error, serverWalletAddress={}, sig={}", serverWalletAddress, sig, e);
      }
      if (publicKey != null) {
        addressRecovered = "0x" + Keys.getAddress(publicKey);
        if (serverWalletAddress.equalsIgnoreCase(addressRecovered)) {
          match = true;
          break;
        }
      }
    }
    log.debug("verifySignature() result={}", match);
    return match;
  }
  
  VerifyNotExistProofResult.VerifyNotExistProofStatus verifyNotExistMerkleProofAndReceipt(
      final @NonNull ExistenceProof proof, final long fromCO, final long toCO, ClearanceRecord cr) {
    log.debug("verifyNotExistMerkleProofAndReceipt start(), proof={}, fromCO={}, toCO={}, cr={}", proof, fromCO, toCO,
        cr);
    // verify isInPbPair
    final MerkleProof merkleProof = proof.getMerkleProof();
    final String indexValue = proof.getIndexValue();
    final Receipt receipt = proof.getReceipt();
    
    boolean isInPbPair = false;
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      
      final String indexValueHash = HashUtils.byte2hex(HashUtils.sha256(indexValue.getBytes()));
      
      final String key = pbPairValue.getKeyHash();
      isInPbPair = indexValueHash.equalsIgnoreCase(key);
      if (isInPbPair) {
        break;
      }
    }
    
    final boolean isSlice = SliceValidationUtil.evalRootHashFromSlice(merkleProof.getSlice());
    
    boolean isRootHash = false;
    if (cr != null) {
      isRootHash = ClearanceRecordVerifyUtil.isRootHashEqual(cr,
          SliceValidationUtil.getRootHash(merkleProof.getSlice()));
    }
    
    VerifyNotExistProofResult.VerifyNotExistProofStatus status = null;
    
    // if isInPbPair : check timestamp
    if (isInPbPair) {
      if (receipt != null) {
        if (proof.getClearanceOrder() > toCO || proof.getClearanceOrder() < fromCO) {
          status = VerifyNotExistProofResult.VerifyNotExistProofStatus.OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME;
        } else {
          status = VerifyNotExistProofResult.VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
        }
      } else {
        status = VerifyNotExistProofResult.VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
      }
    } else {
      status = VerifyNotExistProofResult.VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND;
    }
    if (!isRootHash) {
      status = VerifyNotExistProofResult.VerifyNotExistProofStatus.ERROR_ROOT_HASH_ERROR;
    }
    if (!isSlice) {
      status = VerifyNotExistProofResult.VerifyNotExistProofStatus.ERROR_SLICE_ERROR;
    }
    
    log.debug("status={}", status);
    return status;
  }
  
  boolean verifyClearanceRecordsByLatestClearanceRecord(final @NonNull List<ClearanceRecord> crList,
      final @NonNull ClearanceRecord latestCR) {
    log.debug("verifyClearanceRecordsByLatestClearanceRecord() crList={}, latestCR={}", crList, latestCR);
    
    if (crList.isEmpty()) {
      log.debug("crList is empty, return true");
      return true;
    }
    
    byte[] clearanceRecordHash = new byte[32];
    boolean result = true;
    
    if (crList.size() == 1) {
      clearanceRecordHash = HashUtils.hex2byte(crList.get(0)
                                                     .getChainHash());
    } else {
      for (int i = 1; i < crList.size(); i++) {
        final ClearanceRecord record = crList.get(i);
        byte[] concatByteArray = concatByteArray(HashUtils.hex2byte(record.getRootHash()),
            Numeric.toBytesPadded(BigInteger.valueOf(record.getClearanceOrder()), 32));
        
        concatByteArray = concatByteArray(concatByteArray, HashUtils.hex2byte(crList.get(i - 1)
                                                                                    .getChainHash()));
        clearanceRecordHash = Hash.sha3(concatByteArray);
        result &= Arrays.equals(clearanceRecordHash, HashUtils.hex2byte(crList.get(i)
                                                                              .getChainHash()));
      }
    }
    result &= Arrays.equals(clearanceRecordHash, HashUtils.hex2byte(latestCR.getChainHash()));
    log.debug("verifyClearanceRecordsByLatestClearanceRecord() result={}", result);
    return result;
  }
  
  byte[] concatByteArray(@NonNull byte[] previousByte, @NonNull byte[] currentByte) {
    byte[] result = new byte[previousByte.length + currentByte.length];
    System.arraycopy(previousByte, 0, result, 0, previousByte.length);
    System.arraycopy(currentByte, 0, result, previousByte.length, currentByte.length);
    return result;
  }
  
}
