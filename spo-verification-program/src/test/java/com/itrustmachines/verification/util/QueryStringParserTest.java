package com.itrustmachines.verification.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryStringParserTest {
  
  @Test
  public void test_parse() {
    String queryStr = "IV_Key=Test,FromTS=123,ToTS=456,FromCO=87,ToCO=88";
    final QueryStringParser.Query query = QueryStringParser.parse(queryStr);
    
    assertThat(query.getIndexValueKey()).isEqualTo("Test");
    assertThat(query.getFromTS()).isEqualTo(123L);
    assertThat(query.getToTS()).isEqualTo(456L);
    assertThat(query.getFromCO()).isEqualTo(87L);
    assertThat(query.getToCO()).isEqualTo(88L);
  }
  
}