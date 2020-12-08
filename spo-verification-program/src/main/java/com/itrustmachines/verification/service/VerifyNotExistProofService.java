package com.itrustmachines.verification.service;

import java.nio.charset.StandardCharsets;

import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.constants.VerifyStatus;
import com.itrustmachines.verification.util.ClearanceRecordVerifyUtil;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyNotExistProofService {
  
  public VerifyReceiptAndMerkleProofResult verifyNotExistProof(@NonNull final ExistenceProof proof,
      @NonNull final String serverWalletAddress, @NonNull final ClearanceRecord contractClearanceRecord,
      final long fromCO, final long toCO) {
    
    final boolean isMerkleProofSignature = verifyMerkleProofSignature(proof.getMerkleProof(), serverWalletAddress);
    
    VerifyNotExistProofStatus verifyNotExistProofStatus;
    if (isMerkleProofSignature) {
      verifyNotExistProofStatus = verifyNotExistMerkleProofAndReceipt(proof, fromCO, toCO, contractClearanceRecord);
    } else {
      verifyNotExistProofStatus = VerifyNotExistProofStatus.ERROR_SIGNATURE;
    }
    
    boolean pass = !proof.isExist();
    if (!VerifyNotExistProofStatus.OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME.equals(verifyNotExistProofStatus)
        && !VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND.equals(verifyNotExistProofStatus)) {
      pass = false;
    }
    
    return VerifyReceiptAndMerkleProofResult.builder()
                                            .pass(pass)
                                            .verifyStatus(pass ? VerifyStatus.PASS : VerifyStatus.MODIFIED)
                                            .status(pass ? StatusConstantsString.OK : StatusConstantsString.ERROR)
                                            .existenceType(ExistenceType.NOT_EXIST)
                                            .clearanceOrder(proof.getClearanceOrder())
                                            .indexValue(proof.getIndexValue())
                                            .verifyNotExistProofResult(verifyNotExistProofStatus)
                                            .merkleproofSignatureOk(isMerkleProofSignature)
                                            .txHash(contractClearanceRecord.getTxHash())
                                            .build();
  }
  
  boolean verifyMerkleProofSignature(@NonNull final MerkleProof merkleProof,
      @NonNull final String serverWalletAddress) {
    log.debug("verifyMerkleProofSignature() start, merkleProof={}", merkleProof);
    final SpoSignature sigServer = merkleProof.getSigServer();
    // verify isMerkleproofSignatureOk
    final boolean result = SignatureUtil.verifySignature(serverWalletAddress, sigServer, merkleProof.toSignDataSha3());
    log.debug("verifyMerkleProofSignature() end, result={}", result);
    return result;
    
  }
  
  VerifyNotExistProofStatus verifyNotExistMerkleProofAndReceipt(@NonNull final ExistenceProof proof, final long fromCO,
      final long toCO, final ClearanceRecord cr) {
    log.debug("verifyNotExistMerkleProofAndReceipt start(), proof={}, fromCO={}, toCO={}, cr={}", proof, fromCO, toCO,
        cr);
    // verify isInPbPair
    final MerkleProof merkleProof = proof.getMerkleProof();
    final String indexValue = proof.getIndexValue();
    final Receipt receipt = proof.getReceipt();
    
    boolean isInPbPair = false;
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      
      final String indexValueHash = HashUtils.byte2hex(HashUtils.sha256(indexValue.getBytes(StandardCharsets.UTF_8)));
      
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
    
    VerifyNotExistProofStatus status = null;
    
    // if isInPbPair : check timestamp
    if (isInPbPair) {
      if (receipt != null) {
        if (proof.getClearanceOrder() > toCO || proof.getClearanceOrder() < fromCO) {
          status = VerifyNotExistProofStatus.OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME;
        } else {
          status = VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
        }
      } else {
        status = VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
      }
    } else {
      status = VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND;
    }
    if (!isRootHash) {
      status = VerifyNotExistProofStatus.ERROR_ROOT_HASH_ERROR;
    }
    if (!isSlice) {
      status = VerifyNotExistProofStatus.ERROR_SLICE_ERROR;
    }
    
    log.debug("status={}", status);
    return status;
  }
  
}
