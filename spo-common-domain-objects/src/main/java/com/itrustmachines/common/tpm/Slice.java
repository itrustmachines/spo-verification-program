package com.itrustmachines.common.tpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Slice implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  int index;
  List<String> interNodes;
  
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.index + ".");
    for (int i = 0; i < this.interNodes.size() - 1; i++) {
      sb.append(interNodes.get(i) + ".");
    }
    sb.append(this.interNodes.get(this.interNodes.size() - 1));
    return sb.toString();
  }
  
  public static String getRootHash(Slice slice) {
    return slice.getInterNodes()
                .get(slice.getInterNodes()
                          .size()
                    - 1);
  }
  
  public static Slice fromString(String input) {
    final String[] tokens = input.split("\\.");
    int index = Integer.parseInt(tokens[0]);
    final List<String> list = new ArrayList<>();
    for (int i = 1; i < tokens.length; i++) {
      list.add(tokens[i]);
    }
    return Slice.builder()
                .index(index)
                .interNodes(list)
                .build();
  }
  
}
