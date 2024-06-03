package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.example.utils.CombinationGenerator;
import org.example.McEliece;
import org.example.utils.Utils;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.IntStream;

public abstract class LeeBrickell implements IAttack {

  protected GF2Vector message;

  protected final int t;
  protected final GF2Matrix g;
  protected final int n;
  protected final int k;
  protected final int p;

  @Override
  public void prepare(McEliece mcEliece, SecureRandom secureRandom, GF2Vector message) {
    var e = new GF2Vector(n, t, secureRandom);
    this.message = (GF2Vector) mcEliece.encrypt(message, e);
  }

  public LeeBrickell(McEliecePublicKeyParameters publicKey, int p) {
    this.t = publicKey.getT();
    this.n = publicKey.getN();
    this.g = publicKey.getG();
    this.k = publicKey.getK();
    this.p = p;
  }

  protected String innerAttack(List<Integer> combination) {
    try {
      var ck = Utils.createGF2VectorFromColumns(message, combination);
      var gk = Utils.createGF2MatrixFromColumns(g, combination);

      var gkinverse = (GF2Matrix) gk.computeInverse();

      var gkinverseg = (GF2Matrix) gkinverse.rightMultiply(g);
      var cckgkinverseg = message.add(gkinverseg.leftMultiply(ck));

      var jIter = IntStream.rangeClosed(0, p).iterator();
      while (jIter.hasNext()) {
        var j = jIter.next();

        CombinationGenerator generator2 = new CombinationGenerator(k, j);
        while (generator2.hasNext()) {
          var combination2 = generator2.next();
          var ek = new GF2Vector(k);
          combination2.forEach(ek::setBit);

          var hw = ((GF2Vector) cckgkinverseg.add(gkinverseg.leftMultiply(ek))).getHammingWeight();
          if (hw <= t) {
            return gkinverse.leftMultiply(ck.add(ek)).toString().replace(" ", "");
          }
        }
      }

    } catch (Exception exception) {
      // Do nothing
    }

    return "";
  }
}
