package org.example;

import java.security.SecureRandom;

import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceCipher;
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceKeyGenerationParameters;
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceKeyPairGenerator;
import org.bouncycastle.pqc.legacy.crypto.mceliece.McElieceParameters;
import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePrivateKeyParameters;
import org.bouncycastle.pqc.legacy.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.legacy.math.linearalgebra.GF2Vector;
import org.bouncycastle.pqc.legacy.math.linearalgebra.Vector;

public class McEliece {

  McEliecePublicKeyParameters publicKey;
  McEliecePrivateKeyParameters privateKey;
  McElieceCipher mcEliece;

  SecureRandom secureRandom;


  private static McEliece instance;

  private McEliece(int m, int t, SecureRandom secureRandom) {
    this.secureRandom = secureRandom;

    var generator = new McElieceKeyPairGenerator();

    generator.init(new McElieceKeyGenerationParameters(secureRandom, new McElieceParameters(m, t)));
    var keyPair = generator.generateKeyPair();


    this.publicKey = (McEliecePublicKeyParameters) keyPair.getPublic();
    this.privateKey = (McEliecePrivateKeyParameters) keyPair.getPrivate();

    this.mcEliece = new McElieceCipher();

    mcEliece.init(true, this.publicKey);

  }

  public static McEliece getInstance(int m, int t, SecureRandom secureRandom) {
    if (instance == null) {
      instance = new McEliece(m, t, secureRandom);
    }
    return instance;
  }

  public Vector encrypt(Vector message) {
    return this.encrypt(message, new GF2Vector(this.publicKey.getN(), this.publicKey.getT(), secureRandom));
  }

  public Vector encrypt(Vector message, Vector error) {
    var encoded = this.publicKey.getG().leftMultiply(message);

    return encoded.add(error);
  }
}
