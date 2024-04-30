package org.example;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;

import java.util.*;
import java.util.stream.Stream;

public class MessageResend implements IAttack {

  private final McEliecePublicKeyParameters publicKey;
  private final GF2Vector message1;
  private final GF2Vector message2;

  private final int t;
  private final GF2Matrix g;
  private final int n;
  private final int k;

  public MessageResend(McEliecePublicKeyParameters publicKey, GF2Vector message1, GF2Vector message2) {

    this.publicKey = publicKey;
    this.message1 = message1;
    this.message2 = message2;

    this.t = publicKey.getT();
    this.n = publicKey.getN();
    this.g = publicKey.getG();
    this.k = publicKey.getK();

  }

  public Tuple<String, Integer> attack() {

    var L1 = new java.util.Vector<Integer>();
    var L0 = new java.util.Vector<Integer>();

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

  public Tuple<String, Integer> attack2() {

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
//      var seq = Utils.createGF2MatrixFromColumns(g, sub);
      var seq = two.computeTranspose();
      GF2Matrix inv;
      try {
        inv = (GF2Matrix) seq.computeInverse();

//        return new Tuple<>(((GF2Vector) inv.leftMultiply(c1prime)).toString().replace(" ", ""), 0);
      } catch (Exception ignored) {
        continue;
      }

      var c1prime = Utils.createGF2VectorFromColumns(message1, sub);
      return new Tuple<>(((GF2Vector) inv.leftMultiply(c1prime)).toString().replace(" ", ""), 0);
//      return new Tuple<>(((GF2Vector) inv.rightMultiply(message1.add(message2))).toString().replace(" ", ""), 0);
      }





  }

  public Tuple<String, Integer> exhaustiveSearch(
          java.util.Vector<Integer> zerosList,
          int zeroPositionCount,
          java.util.Vector<Integer> onesList,
          int onePositionCount
  ) {
    var zeroPositions = Utils.generateCombinations(zerosList, zeroPositionCount);
    var onePositions = Utils.generateCombinations(onesList, onePositionCount);

    var tries = 0;
    for (List<Integer> comb1 : zeroPositions) {
      for (List<Integer> comb2 : onePositions) {
        List<Integer> combo = Stream.concat(comb1.stream(), comb2.stream()).toList();

        tries += 1;
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
          return new Tuple<>(lastColumnString.substring(0, k), tries);
        }
      }
    }
    return new Tuple<>("", null);
  }
}
