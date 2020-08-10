package com.itrustmachines.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class HashUtilsTest {
  
  @Test
  public void testSha256String() throws Exception {
    // given
    String data = "test";
    
    // when
    String result = HashUtils.sha256(data);
    
    // then
    System.out.println(result);
    
    assertThat(result).isNotNull()
                      .isEqualTo(HashUtils.sha256("test"));
  }
  
}
