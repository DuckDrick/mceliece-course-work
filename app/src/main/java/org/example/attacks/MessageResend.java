package org.example.attacks;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Matrix;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.example.McEliece;

import java.security.SecureRandom;

public abstract class MessageResend implements IAttack {

  protected GF2Vector message1;
  protected GF2Vector message2;
  protected final int t;
  protected final GF2Matrix g;
  protected final int n;
  protected final int k;


  public MessageResend(McEliecePublicKeyParameters publicKey) {
    this.t = publicKey.getT();
    this.n = publicKey.getN();
    this.g = publicKey.getG();
    this.k = publicKey.getK();

  }

  @Override
  public void prepare(McEliece mcEliece, SecureRandom secureRandom, GF2Vector message) {
    var e1 = new GF2Vector(n, t, secureRandom);
    var e2 = new GF2Vector(n, t, secureRandom);
    this.message1 = (GF2Vector) mcEliece.encrypt(message, e1);
    this.message2 = (GF2Vector) mcEliece.encrypt(message, e2);
  }
}
