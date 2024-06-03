package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.example.utils.Utils;

import java.util.*;

public class MessageResendIndependant extends MessageResend {

  public MessageResendIndependant(McEliecePublicKeyParameters publicKey) {
    super(publicKey);
  }

  public String attack() {

    var L0 = new java.util.Vector<Integer>();

    var c_sum = message1.add(message2);

    var cstring = c_sum.toString().replace(" ", "");

    for (int i = 0; i < cstring.length(); i++) {
      if (cstring.charAt(i) == '0') {
        L0.add(i);
      }
    }

    HashSet<Set> s = new HashSet<>();

    while(true) {
      Collections.shuffle(L0);
      var sub = L0.subList(0, k);
      var hmm = new HashSet(sub);

      if (s.contains(hmm)) {
        continue;
      }
      s.add(hmm);

      var gt = ((GF2Matrix) g.computeTranspose());
      int[][] abc = new int[sub.size()][];
      for (int i = 0; i < sub.size(); i++) {
        abc[i] = gt.getRow(sub.get(i));
      }
      var two = new GF2Matrix(gt.getNumColumns(), abc);
      var seq = two.computeTranspose();
      GF2Matrix inv;
      try {
        inv = (GF2Matrix) seq.computeInverse();

      } catch (Exception ignored) {
        continue;
      }

      var c1prime = Utils.createGF2VectorFromColumns(message1, sub);
      return inv.leftMultiply(c1prime).toString().replace(" ", "");
      }
  }
}
