package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.example.utils.Utils;

import java.util.*;
import java.util.stream.Stream;

public class MessageResendErrorGeneration extends MessageResend {

  public MessageResendErrorGeneration(McEliecePublicKeyParameters publicKey) {
    super(publicKey);
  }

  public String attack() {

    var L1 = new Vector<Integer>();
    var L0 = new Vector<Integer>();

    var c_sum = message1.add(message2);

    var cstring = c_sum.toString().replace(" ", "");

    for (int i = 0; i < cstring.length(); i++) {
      if (cstring.charAt(i) == '1') {
        L1.add(i);
      } else if (cstring.charAt(i) == '0') {
        L0.add(i);
      }
    }

    var x = (2 * t - L1.size()) / 2;
    var other = t - x;

    return exhaustiveSearch(L0, x, L1, other);
  }

  public String exhaustiveSearch(
          Vector<Integer> zerosList,
          int zeroPositionCount,
          Vector<Integer> onesList,
          int onePositionCount
  ) {
    var zeroPositions = Utils.generateCombinations(zerosList, zeroPositionCount);
    var onePositions = Utils.generateCombinations(onesList, onePositionCount);

    for (List<Integer> comb1 : zeroPositions) {
      for (List<Integer> comb2 : onePositions) {
        List<Integer> combo = Stream.concat(comb1.stream(), comb2.stream()).toList();

        var c = new GF2Vector(n);
        combo.forEach(c::setBit);
        GF2Vector d = (GF2Vector) c.add(this.message1);

        var gIntArray = this.g.getIntArray();
        var withAdditionalRow = Arrays.copyOf(gIntArray, gIntArray.length + 1);
        withAdditionalRow[withAdditionalRow.length - 1] = d.getVecArray();
        var toSolve = ((GF2Matrix) new GF2Matrix(withAdditionalRow[0].length * 32, withAdditionalRow).computeTranspose()).getIntArray();

        var lastColumn = Utils.solve(toSolve, k);

        var lastColumnString = new GF2Vector(n, lastColumn).toString().replace(" ", "");
        if (lastColumnString.charAt(k) == '0') {
          return lastColumnString.substring(0, k);
        }
      }
    }
    return "";
  }
}
